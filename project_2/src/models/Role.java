package src.models;

public enum Role {
    PATIENT, NURSE, DOCTOR, GOVERNMENT;
    
    public static Role fromString(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
