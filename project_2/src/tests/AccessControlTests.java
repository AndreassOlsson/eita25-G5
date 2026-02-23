package src.tests;

import src.access.AccessController;
import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

public class AccessControlTests {

    public static void run() {
        System.out.println("Running AccessControlTests...");
        
        testPatientAccess();
        testNurseAccess();
        testDoctorAccess();
        testGovernmentAccess();
        
        System.out.println("AccessControlTests Passed!\n");
    }

    private static void testPatientAccess() {
        User patient = new User("alice", Role.PATIENT, null);
        MedicalRecord ownRecord = new MedicalRecord("1", "alice", "doc", "nurse", "div", "data");
        MedicalRecord otherRecord = new MedicalRecord("2", "bob", "doc", "nurse", "div", "data");

        assertAccess(AccessController.canRead(patient, ownRecord), "Patient should read own record");
        assertAccess(!AccessController.canRead(patient, otherRecord), "Patient shouldn't read others");
        
        assertAccess(!AccessController.canWrite(patient, ownRecord), "Patient cannot write");
        assertAccess(!AccessController.canDelete(patient), "Patient cannot delete");
    }

    private static void testNurseAccess() {
        User nurse = new User("nurse1", Role.NURSE, "Cardiology");
        // Record where nurse is the assigned nurse
        MedicalRecord assignedRecord = new MedicalRecord("1", "p", "d", "nurse1", "Cardiology", "data");
        // Record in same division but different nurse
        MedicalRecord divisionRecord = new MedicalRecord("2", "p", "d", "nurse2", "Cardiology", "data");
        // Record in different division
        MedicalRecord otherRecord = new MedicalRecord("3", "p", "d", "nurse3", "Radiology", "data");

        assertAccess(AccessController.canRead(nurse, assignedRecord), "Nurse reads assigned record");
        assertAccess(AccessController.canRead(nurse, divisionRecord), "Nurse reads division record");
        assertAccess(!AccessController.canRead(nurse, otherRecord), "Nurse shouldn't read other division");

        assertAccess(AccessController.canWrite(nurse, assignedRecord), "Nurse writes assigned record");
        assertAccess(!AccessController.canWrite(nurse, divisionRecord), "Nurse shouldn't write unassigned record");
    }

    private static void testDoctorAccess() {
        User doctor = new User("doc1", Role.DOCTOR, "Cardiology");
        MedicalRecord assignedRecord = new MedicalRecord("1", "p", "doc1", "n", "Cardiology", "data");
        
        assertAccess(AccessController.canCreate(doctor), "Doctor can create records");
        assertAccess(AccessController.canWrite(doctor, assignedRecord), "Doctor writes assigned record");
    }

    private static void testGovernmentAccess() {
        User gov = new User("gov", Role.GOVERNMENT, null);
        MedicalRecord r = new MedicalRecord("1", "p", "d", "n", "div", "data");

        assertAccess(AccessController.canRead(gov, r), "Gov reads everything");
        assertAccess(AccessController.canDelete(gov), "Gov keeps delete power");
        assertAccess(!AccessController.canWrite(gov, r), "Gov CANNOT write (integrity)");
    }

    private static void assertAccess(boolean condition, String message) {
        if (!condition) {
            System.err.println("❌ FAILED: " + message);
            throw new RuntimeException("Test failed: " + message);
        } else {
            System.out.println("✅ " + message);
        }
    }
}
