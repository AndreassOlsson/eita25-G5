package src.models;

public class User {
    private String id;
    private String username;
    private Role role;
    private String division;

    public User(String id, String username, Role role, String division) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.division = division;
    }

    /**
     * Factory method to create a User from certificate data.
     * Derives the stable id from the CN and role, keeps CN as the human-readable username.
     * 
     * Example: CN="Alice Alison", Role=DOCTOR -> id="doctor_alice", username="Alice Alison"
     */
    public static User fromCertificate(String cn, Role role, String division) {
        String id = normalizeUsername(cn, role);
        return new User(id, cn, role, division);
    }

    /**
     * Normalizes a full name and role into the standard id format.
     * Example: "Alice Alison" + DOCTOR -> "doctor_alice"
     */
    public static String normalizeUsername(String fullName, Role role) {
        if (fullName == null || role == null) {
            return fullName;
        }

        String firstName = fullName.split("\\s+")[0].toLowerCase();

        String rolePrefix = switch (role) {
            case PATIENT -> "patient";
            case NURSE -> "nurse";
            case DOCTOR -> "doctor";
            case GOVERNMENT -> "gov";
        };

        return rolePrefix + "_" + firstName;
    }

    public String getId() {
        return id;
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

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", division='" + division + '\'' +
                '}';
    }
}
