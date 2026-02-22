package src.networking;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import src.models.MedicalRecord;
import src.models.PermissionDeniedException;
import src.models.Role;
import src.models.User;
import src.repositories.IRecordRepo;
import src.repositories.LocalFSRecordRepo;

public class server {
    private static final String KEYSTORE_PATH = "keystores/server_keystore.jks";
    private static final String TRUSTSTORE_PATH = "keystores/server_truststore.jks";
    private static final char[] PASSWORD = "password".toCharArray();
    
    private static IRecordRepo recordRepo;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java src.networking.server <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        recordRepo = new LocalFSRecordRepo();

        try {
            SSLServerSocketFactory ssf = getServerSocketFactory();
            SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(port, 0, InetAddress.getByName(null));
            ss.setNeedClientAuth(true);

            System.out.println("Server started on port " + port);
            System.out.println("Waiting for connections...");

            while (true) {
                try {
                    SSLSocket socket = (SSLSocket) ss.accept();
                    new Thread(new ClientHandler(socket)).start();
                } catch (IOException e) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLServerSocketFactory getServerSocketFactory() throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            ks.load(fis, PASSWORD);
        }
        kmf.init(ks, PASSWORD);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ts = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(TRUSTSTORE_PATH)) {
            ts.load(fis, PASSWORD);
        }
        tmf.init(ts);

        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx.getServerSocketFactory();
    }

    private static class ClientHandler implements Runnable {
        private final SSLSocket socket;

        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                socket.startHandshake();
                SSLSession session = socket.getSession();
                X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];
                User user = extractUserFromCert(cert);
                
                System.out.println("Authenticated User: " + user);

                String requestLine;
                while ((requestLine = in.readLine()) != null) {
                    System.out.println("Request from " + user.getUsername() + ": " + requestLine);
                    String response = handleRequest(user, requestLine);
                    out.println(response);
                }

            } catch (Exception e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private User extractUserFromCert(X509Certificate cert) throws InvalidNameException {
            String subjectDN = cert.getSubjectX500Principal().getName();
            LdapName ldapDN = new LdapName(subjectDN);
            
            String username = null;
            Role role = null;
            String division = null;

            for (Rdn rdn : ldapDN.getRdns()) {
                String type = rdn.getType();
                String value = rdn.getValue().toString();
                
                if (type.equalsIgnoreCase("CN")) {
                    username = value;
                } else if (type.equalsIgnoreCase("OU")) {
                    division = value;
                } else if (type.equalsIgnoreCase("O")) {
                    role = Role.fromString(value);
                }
            }
            
            return new User(username, role, division);
        }

        private String handleRequest(User user, String requestLine) {
            String[] parts = requestLine.split(" ", 3);
            if (parts.length == 0) return "ERROR Empty request";
            String command = parts[0];

            try {
                if ("READ".equalsIgnoreCase(command) && parts.length >= 2) {
                    String recordId = parts[1];
                    MedicalRecord record = recordRepo.read(user, recordId);
                    return "OK " + record.toString();
                } 
                else if ("WRITE".equalsIgnoreCase(command) && parts.length >= 3) {
                    String recordId = parts[1];
                    String recordData = parts[2]; 
                    String[] dataParts = recordData.split(";", 5);
                    if (dataParts.length < 5) return "ERROR Invalid data format";

                    MedicalRecord record = new MedicalRecord(
                        recordId, 
                        dataParts[0], 
                        dataParts[1], 
                        dataParts[2], 
                        dataParts[3], 
                        dataParts[4]
                    );
                    
                    recordRepo.write(user, record);
                    return "OK Record written";
                }
                else {
                    return "ERROR Unknown command";
                }
            } catch (PermissionDeniedException e) {
                return "DENIED " + e.getMessage();
            } catch (IOException e) {
                return "ERROR " + e.getMessage();
            }
        }
    }
}
