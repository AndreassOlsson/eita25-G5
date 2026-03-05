package src.access;

import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

public class AccessController {
    public static boolean canRead(User user, MedicalRecord record) {
        return switch (user.getRole()) {
            case Role.PATIENT -> record.hasPatient(user.getId());
            case Role.NURSE, Role.DOCTOR -> record.isAssignedTo(user.getId())
                              || record.isInDivision(user.getDivision());
            case Role.GOVERNMENT -> true;
        };
    }

    public static boolean canWrite(User user, MedicalRecord record) {
        return switch (user.getRole()) {
            case Role.NURSE -> record.hasNurse(user.getId());
            case Role.DOCTOR -> record.hasDoctor(user.getId());
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