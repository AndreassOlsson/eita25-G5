package src.config;

public class Settings {
    // System props used to access the specific keystore and truststore for both client & server
    private static final String _KEYSTORE = "javax.net.ssl.keyStore";
    private static final String _KEYSTORE_PASS = "javax.net.ssl.keyStorePassword";
    private static final String _TRUSTSTORE = "javax.net.ssl.trustStore";
    private static final String _TRUSTSTORE_PASS = "javax.net.ssl.trustStorePassword";
    
    /**
     * Private helper method to fetch a required system property.
     */
    private static String getProp(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Missing required system property: " + key);
        }
        return value;
    }

    /**
     * Gets the configured Keystore path.
     */
    public static String getKeystorePath() {
        return getProp(_KEYSTORE);
    }

    /**
     * Gets the configured Keystore password.
     */
    public static char[] getKeystorePassword() {
        return getProp(_KEYSTORE_PASS).toCharArray();
    }

    /**
     * Gets the configured Truststore path.
     */
    public static String getTruststorePath() {
        return getProp(_TRUSTSTORE);
    }

    /**
     * Gets the configured Truststore password.
     */
    public static char[] getTruststorePassword() {
        return getProp(_TRUSTSTORE_PASS).toCharArray();
    }
}
