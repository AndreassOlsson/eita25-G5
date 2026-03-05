package src.access;

import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

public class AccessController {
    public static boolean canRead(User user, MedicalRecord record) {
        return switch (user.getRole()) {
            case Role.PATIENT -> user.matches(record.getPatientId());
            case Role.NURSE, Role.DOCTOR -> user.matches(record.getNurseId())
                              || user.matches(record.getDoctorId())
                              || (user.getDivision() != null && user.getDivision().equals(record.getDivision()));
            case Role.GOVERNMENT -> true;
        };
    }

    public static boolean canWrite(User user, MedicalRecord record) {
        return switch (user.getRole()) {
            case Role.NURSE -> user.matches(record.getNurseId());
            case Role.DOCTOR -> user.matches(record.getDoctorId());
            default -> false;
        };
    }

    public static boolean canDelete(User user) {
        return user.getRole() == Role.GOVERNMENT;
    }

    public static boolean canCreate(User user) {
        return user.getRole() == Role.DOCTOR;
    }
}