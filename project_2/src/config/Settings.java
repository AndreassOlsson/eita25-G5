package src.config;

public class Settings {
    // Standardized System Property Keys
    public static final String PROP_KEYSTORE = "javax.net.ssl.keyStore";
    public static final String PROP_KEYSTORE_PASS = "javax.net.ssl.keyStorePassword";
    public static final String PROP_TRUSTSTORE = "javax.net.ssl.trustStore";
    public static final String PROP_TRUSTSTORE_PASS = "javax.net.ssl.trustStorePassword";
    
    // Application Specific Properties
    public static final String PROP_CA_CERT = "project.pki.ca.cert";

    /**
     * Gets the configured Keystore path.
     * Throws RuntimeException if not configured.
     */
    public static String getKeystorePath() {
        String path = System.getProperty(PROP_KEYSTORE);
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Missing required system property: " + PROP_KEYSTORE);
        }
        return path;
    }

    /**
     * Gets the configured Keystore password.
     * Throws RuntimeException if not configured.
     */
    public static char[] getKeystorePassword() {
        String pass = System.getProperty(PROP_KEYSTORE_PASS);
        if (pass == null || pass.trim().isEmpty()) {
            throw new RuntimeException("Missing required system property: " + PROP_KEYSTORE_PASS);
        }
        return pass.toCharArray();
    }

    /**
     * Gets the configured Truststore path.
     * Throws RuntimeException if not configured.
     */
    public static String getTruststorePath() {
        String path = System.getProperty(PROP_TRUSTSTORE);
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Missing required system property: " + PROP_TRUSTSTORE);
        }
        return path;
    }

    /**
     * Gets the configured Truststore password.
     * Throws RuntimeException if not configured.
     */
    public static char[] getTruststorePassword() {
        String pass = System.getProperty(PROP_TRUSTSTORE_PASS);
        if (pass == null || pass.trim().isEmpty()) {
            throw new RuntimeException("Missing required system property: " + PROP_TRUSTSTORE_PASS);
        }
        return pass.toCharArray();
    }
}
