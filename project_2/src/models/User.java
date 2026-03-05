package src.models;

public class User {
    private String id; // TODO: Should become what the "normalizedUsername" is currently attempting.
    private String username;
    private Role role;
    private String division;

    public User(String username, Role role, String division) {
        this.username = username;
        this.role = role;
        this.division = division;
    }

    /**
     * Factory method to create a User from certificate data.
     * Normalizes the CN (common name) by extracting the first name and 
     * prepending the role prefix to match the standard format used in records.
     * 
     * Example: CN="Alice Alison", Role=DOCTOR becomes username="doctor_alice"
     * 
     * @param cn The CN field from the certificate (e.g., "Alice Alison")
     * @param role The user's role
     * @param division The user's division (OU field)
     * @return A new User instance with normalized username
     */
    public static User fromCertificate(String cn, Role role, String division) {
        String normalizedUsername = normalizeUsername(cn, role);
        return new User(normalizedUsername, role, division);
    }

    /**
     * Normalizes a full name and role into the standard username format.
     * Extracts the first name and prepends the role prefix in lowercase with underscore.
     * 
     * Example: "Alice Alison" + DOCTOR -> "doctor_alice"
     * 
     * @param fullName The full name from a certificate CN field
     * @param role The user's role
     * @return Normalized username in format "role_firstname"
     */
    public static String normalizeUsername(String fullName, Role role) {
        if (fullName == null || role == null) {
            return fullName;
        }

        // Extract first name (first word of the full name)
        String firstName = fullName.split("\\s+")[0].toLowerCase();

        // Map role to prefix
        String rolePrefix = switch (role) {
            case PATIENT -> "patient";
            case NURSE -> "nurse";
            case DOCTOR -> "doctor";
            case GOVERNMENT -> "gov";
        };

        return rolePrefix + "_" + firstName;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getDivision() {
        return division;
    }

    /**
     * Fool-proof username matching that handles both normalized and non-normalized identifiers.
     * This method compares against the user's username directly, and also attempts to
     * normalize the provided identifier if it matches the user's role.
     * 
     * Examples:
     * - User("doctor_alice", DOCTOR, ...).matches("doctor_alice") -> true
     * - User("doctor_alice", DOCTOR, ...).matches("Alice Alison") -> true (normalized to doctor_alice)
     * 
     * @param identifier The identifier to check (could be normalized or full name)
     * @return true if the identifier matches this user's username in any form
     */
    public boolean matches(String identifier) {
        if (identifier == null) {
            return false;
        }
        
        // Direct match
        if (this.username.equals(identifier)) {
            return true;
        }
        
        // Try normalizing the identifier with this user's role
        String normalized = normalizeUsername(identifier, this.role);
        return this.username.equals(normalized);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role=" + role +
                ", division='" + division + '\'' +
                '}';
    }
}
