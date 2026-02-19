package src.models;

public class User {
    String username;
    Role role;  // PATIENT, NURSE, DOCTOR, GOVERNMENT
    String division; // only for nurse/doctor

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getDivision() {
        return division;
    }
}
