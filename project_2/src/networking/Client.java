package src.networking;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

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
public class Client {
    
    public static void main(String[] args) {
        System.out.println("=== CLIENT STARTUP ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        if (args.length < 2) {
            System.out.println("USAGE: java ... src.networking.client <host> <port> [user_prefix]");
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String userPrefix = (args.length > 2) ? args[2] : "Unknown";

        // Fetch settings from System Properties 
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

            System.out.println("\n=== TLS CONNECTION ESTABLISHED ===");
            System.out.println("[TLS] Protocol: " + session.getProtocol());
            System.out.println("[TLS] Cipher Suite: " + session.getCipherSuite());

            // Verify the server's certificate
            X509Certificate serverCert = (X509Certificate) session.getPeerCertificates()[0];
            System.out.println("[AUTH] Server Certificate Subject: " + serverCert.getSubjectX500Principal().getName());
            System.out.println("[AUTH] Server Certificate Issuer: " + serverCert.getIssuerX500Principal().getName());
            System.out.println("[AUTH] Server Certificate Serial: " + serverCert.getSerialNumber());
            System.out.println("[AUTH] Server Certificate Valid: " + serverCert.getNotBefore() + " to " + serverCert.getNotAfter());
            System.out.println("=================================\n");

            try (BufferedReader networkIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter networkOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("Type 'HELP' for commands or 'QUIT' to exit.");
                System.out.print("> ");

                String userInput;
                while ((userInput = userIn.readLine()) != null) {
                    if (userInput.trim().equalsIgnoreCase("QUIT")) break;
                    
                    networkOut.println(userInput);
                    
                    // Read potentially multi-line response
                    String line;
                    while ((line = networkIn.readLine()) != null) {
                        System.out.println("Server: " + line);
                        if (!networkIn.ready()) break; // Simple check to stop if no more lines immediately available
                    }
                    System.out.print("> ");
                }
            }

        } catch (Exception e) {
            if (e instanceof java.io.IOException && e.getMessage().contains("password")) {
                System.err.println("\nAuthentication Error: Incorrect password for keystore or truststore.");
                System.err.println("Please try again with the correct password.");
            } else if (e.getCause() instanceof java.security.UnrecoverableKeyException) {
                System.err.println("\nAuthentication Error: Unable to recover key (Wrong password?)");
            } else {
                System.err.println("\nClient Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static SSLSocketFactory getSocketFactory(String keystorePath, String truststorePath, char[] keystorePass, char[] truststorePass) throws Exception {
        KeyStore ks = KeyStore.getInstance(Settings.KEYSTORE_TYPE);
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, keystorePass);
        }

        KeyStore ts = KeyStore.getInstance(Settings.KEYSTORE_TYPE);
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            ts.load(fis, truststorePass);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(Settings.KEY_MANAGER_ALGORITHM);
        kmf.init(ks, keystorePass);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(Settings.TRUST_MANAGER_ALGORITHM);
        tmf.init(ts);

        SSLContext ctx = SSLContext.getInstance(Settings.TLS_PROTOCOL);
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        
        return ctx.getSocketFactory();
    }
}
