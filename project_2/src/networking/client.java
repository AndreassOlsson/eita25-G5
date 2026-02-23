package src.networking;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import src.config.Settings;

/*
 * Client for Hospital System
 * Usage: java -Djavax.net.ssl.keyStore=keystores/alice_keystore.jks \
 *             -Djavax.net.ssl.keyStorePassword=password \
 *             -Djavax.net.ssl.trustStore=keystores/alice_truststore.jks \
 *             -Djavax.net.ssl.trustStorePassword=password \
 *             src.networking.client <host> <port> <user_prefix>
 */
public class client {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("USAGE: java ... src.networking.client <host> <port> [user_prefix]");
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String userPrefix = (args.length > 2) ? args[2] : "Unknown";

        // Fetch settings from System Properties (Settings class enforces this)
        String keystorePath = Settings.getKeystorePath();
        String truststorePath = Settings.getTruststorePath();
        char[] keystorePass = Settings.getKeystorePassword();
        char[] truststorePass = Settings.getTruststorePassword();

        System.out.println("Connecting to " + host + ":" + port);
        System.out.println("User Context: " + userPrefix);
        System.out.println("Keystore: " + keystorePath);
        System.out.println("Truststore: " + truststorePath);

        try {
            SSLSocketFactory factory = getSocketFactory(keystorePath, truststorePath, keystorePass, truststorePass);
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            
            socket.startHandshake();
            SSLSession session = socket.getSession();
            System.out.println("Secure connection established.");
            System.out.println("  Protocol: " + session.getProtocol());
            System.out.println("  Cipher Suite: " + session.getCipherSuite());

            try (BufferedReader networkIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter networkOut = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("\n--- Commands ---");
                System.out.println("READ <recordId>");
                System.out.println("WRITE <recordId> <patientId>;<doctorId>;<nurseId>;<division>;<data>");
                System.out.println("DELETE <recordId>");
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

    private static SSLSocketFactory getSocketFactory(String keystorePath, String truststorePath, char[] keystorePass, char[] truststorePass) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, keystorePass);
        }

        KeyStore ts = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            ts.load(fis, truststorePass);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePass);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        
        return ctx.getSocketFactory();
    }
}
