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

import src.config.Settings;
import src.models.Role;
import src.models.User;
import src.repositories.IAuditLogRepo;
import src.repositories.IRecordRepo;
import src.repositories.LocalFSAuditLogRepo;
import src.repositories.LocalFSRecordRepo;

public class Server {
    
    private static IRecordRepo recordRepo;
    private static IAuditLogRepo auditLogRepo;

    public static void main(String[] args) {
        System.out.println("=== SERVER STARTUP ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));

        if (args.length < 1) {
            System.err.println("Usage: java ... src.networking.server <port>");
            System.exit(1);
        }

        // Fetch settings from System Properties to check functionality early
        try {
            System.out.println("Using Keystore: " + Settings.getKeystorePath());
            System.out.println("Using Truststore: " + Settings.getTruststorePath());
        } catch (RuntimeException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        recordRepo = new LocalFSRecordRepo();
        auditLogRepo = new LocalFSAuditLogRepo();

        try {
            SSLServerSocketFactory ssf = getServerSocketFactory();
            SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(port, 0, InetAddress.getByName(null));
            ss.setNeedClientAuth(true);

            System.out.println("\n=== TLS CONFIGURATION ===");
            System.out.println("[TLS] Protocol: " + Settings.TLS_PROTOCOL);
            System.out.println("[TLS] Mutual Authentication (Client Cert Required): " + ss.getNeedClientAuth());
            System.out.println("[TLS] Keystore Type: " + Settings.KEYSTORE_TYPE);
            System.out.println("[TLS] Key Manager Algorithm: " + Settings.KEY_MANAGER_ALGORITHM);
            System.out.println("[TLS] Trust Manager Algorithm: " + Settings.TRUST_MANAGER_ALGORITHM);
            
            String[] enabled = ss.getEnabledCipherSuites();
            System.out.println("[TLS] Enabled Cipher Suites: " + enabled.length);
            for (int i = 0; i < Math.min(5, enabled.length); i++) {
                System.out.println("  [" + i + "] " + enabled[i]);
            }

            System.out.println("\n=== SERVER READY ===");
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
        SSLContext ctx = SSLContext.getInstance(Settings.TLS_PROTOCOL);
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(Settings.KEY_MANAGER_ALGORITHM);
        KeyStore ks = KeyStore.getInstance(Settings.KEYSTORE_TYPE);
        
        String keystorePath = Settings.getKeystorePath();
        char[] keystorePass = Settings.getKeystorePassword();
        
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, keystorePass);
        }
        kmf.init(ks, keystorePass);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(Settings.TRUST_MANAGER_ALGORITHM);
        KeyStore ts = KeyStore.getInstance(Settings.KEYSTORE_TYPE);
        
        String truststorePath = Settings.getTruststorePath();
        char[] truststorePass = Settings.getTruststorePassword();

        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            ts.load(fis, truststorePass);
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
                
                System.out.println("\n=== NEW CLIENT CONNECTION ===");
                System.out.println("[AUTH] Client Certificate Subject: " + cert.getSubjectX500Principal().getName());
                System.out.println("[AUTH] Client Certificate Issuer: " + cert.getIssuerX500Principal().getName());
                System.out.println("[AUTH] Certificate Serial: " + cert.getSerialNumber());
                System.out.println("[AUTH] Certificate Valid: " + cert.getNotBefore() + " to " + cert.getNotAfter());
                System.out.println("[AUTH] Resolved Identity: " + user);
                System.out.println("[TLS] Session Protocol: " + session.getProtocol());
                System.out.println("[TLS] Session Cipher Suite: " + session.getCipherSuite());

                RequestHandler handler = new RequestHandler(recordRepo, auditLogRepo);

                String requestLine;
                while ((requestLine = in.readLine()) != null) {
                    String response = handler.handle(user, requestLine);
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

            String cn = null;
            Role role = null;
            String division = null;

            for (Rdn rdn : ldapDN.getRdns()) {
                String type = rdn.getType();
                String value = rdn.getValue().toString();

                if (type.equalsIgnoreCase("CN")) {
                    cn = value;
                } else if (type.equalsIgnoreCase("OU")) {
                    division = value;
                } else if (type.equalsIgnoreCase("O")) {
                    role = Role.fromString(value);
                }
            }

            return User.fromCertificate(cn, role, division);
        }

    }
}
