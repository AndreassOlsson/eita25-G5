package src.access;

import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

public class AccessController {
    
    public static boolean canRead(User u, MedicalRecord r) {
        return switch (u.getRole()) {
            case Role.PATIENT -> u.matches(r.getPatientId());
            case Role.NURSE, Role.DOCTOR -> u.matches(r.getNurseId())
                              || u.matches(r.getDoctorId())
                              || (u.getDivision() != null && u.getDivision().equals(r.getDivision()));
            case Role.GOVERNMENT -> true;
        };
    }

    public static boolean canWrite(User u, MedicalRecord r) {
        return switch (u.getRole()) {
            case Role.NURSE -> u.matches(r.getNurseId());
            case Role.DOCTOR -> u.matches(r.getDoctorId());
            default -> false;
        };
    }

    public static boolean canDelete(User u) {
        return u.getRole() == Role.GOVERNMENT;
    }

    public static boolean canCreate(User u) {
        return u.getRole() == Role.DOCTOR;
    }
}