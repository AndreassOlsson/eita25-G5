package src.networking;

import java.io.*;
import java.security.KeyStore;
import javax.net.ssl.*;

/*
 * Client for Hospital System
 * Usage: java src.networking.client <host> <port> <user_prefix> [password]
 * Example: java src.networking.client localhost 9876 doctor_alice
 */
public class client {
    
    private static final String KEYSTORE_DIR = "keystores/";
    private static final String TRUSTSTORE_PATH = "keystores/client_truststore.jks"; // Or server_truststore if shared? setup_pki makes *_truststore.jks for each user.
    // setup_pki.sh: "create_entity ... 1. Create Truststore (Contains only the CA cert) ... _truststore.jks"
    // So each user has their own truststore. specific to them? No, it just imports CA cert.
    // So any truststore with CA cert works. We can use the user's specific truststore.

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("USAGE: java src.networking.client <host> <port> <user_prefix> [password]");
            System.out.println("Example: java src.networking.client localhost 9876 doctor_alice");
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String userPrefix = args[2];
        char[] password = (args.length > 3) ? args[3].toCharArray() : "password".toCharArray();

        String keystorePath = KEYSTORE_DIR + userPrefix + "_keystore.jks";
        String truststorePath = KEYSTORE_DIR + userPrefix + "_truststore.jks";

        System.out.println("Connecting to " + host + ":" + port);
        System.out.println("User: " + userPrefix);
        System.out.println("Keystore: " + keystorePath);

        try {
            SSLSocketFactory factory = getSocketFactory(keystorePath, truststorePath, password);
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            
            socket.startHandshake();
            System.out.println("Secure connection established.");

            try (BufferedReader networkIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter networkOut = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("\n--- Commands ---");
                System.out.println("READ <recordId>");
                System.out.println("WRITE <recordId> <patientId>;<doctorId>;<nurseId>;<division>;<data>");
                System.out.println("QUIT to exit");
                System.out.print("> ");

                String userInput;
                while ((userInput = userIn.readLine()) != null) {
                    if (userInput.trim().equalsIgnoreCase("QUIT")) break;
                    
                    networkOut.println(userInput);
                    
                    String response = networkIn.readLine();
                    System.out.println("Server: " + response);
                    System.out.print("> ");
                }
            }

        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLSocketFactory getSocketFactory(String keystorePath, String truststorePath, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, password);
        }

        KeyStore ts = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            ts.load(fis, password);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        
        return ctx.getSocketFactory();
    }
}
