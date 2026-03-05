package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import src.access.AccessController;
import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

class AccessControllerTest {

    @Test
    void patientCanOnlyReadOwnRecord() {
        User charlie = new User("Charlie Charles", Role.PATIENT, null);
        MedicalRecord charlieVitals = record("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord charlieFollowUp = record("rec_charlie_followup", "Charlie Charles", "Alice Alison", "Carol Carlson", "Cardiology", "Follow-up consultation");
        MedicalRecord eveOncology = record("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");

        assertTrue(AccessController.canRead(charlie, charlieVitals));
        assertTrue(AccessController.canRead(charlie, charlieFollowUp));
        assertFalse(AccessController.canRead(charlie, eveOncology));
        assertFalse(AccessController.canWrite(charlie, charlieVitals));
        assertFalse(AccessController.canCreate(charlie));
        assertFalse(AccessController.canDelete(charlie));
    }

    @Test
    void nurseHasDivisionReadButOnlyAssignedWrite() {
        User bob = new User("Bob Bobson", Role.NURSE, "Cardiology");
        MedicalRecord bobAssignment = record("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord carolAssignment = record("rec_charlie_followup", "Charlie Charles", "Alice Alison", "Carol Carlson", "Cardiology", "Follow-up consultation");
        MedicalRecord oncologyRecord = record("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");

        assertTrue(AccessController.canRead(bob, bobAssignment));
        assertTrue(AccessController.canWrite(bob, bobAssignment));
        assertTrue(AccessController.canRead(bob, carolAssignment));
        assertFalse(AccessController.canWrite(bob, carolAssignment));
        assertFalse(AccessController.canRead(bob, oncologyRecord));
    }

    @Test
    void doctorCreatesAndWritesAssignedRecordsOnly() {
        User alice = new User("Alice Alison", Role.DOCTOR, "Cardiology");
        User mallory = new User("Mallory Mallet", Role.DOCTOR, "Oncology");
        MedicalRecord aliceAssignment = record("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord cardiologyTeamRecord = record("rec_charlie_followup", "Charlie Charles", "Mallory Mallet", "Carol Carlson", "Cardiology", "Shared cardiology consult");
        MedicalRecord oncologyRecord = record("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");

        assertTrue(AccessController.canCreate(alice));
        assertTrue(AccessController.canRead(alice, aliceAssignment));
        assertTrue(AccessController.canWrite(alice, aliceAssignment));
        assertTrue(AccessController.canRead(alice, cardiologyTeamRecord));
        assertFalse(AccessController.canWrite(alice, cardiologyTeamRecord));
        assertFalse(AccessController.canRead(alice, oncologyRecord));
        assertFalse(AccessController.canWrite(alice, oncologyRecord));
        assertFalse(AccessController.canDelete(alice));

        assertTrue(AccessController.canRead(mallory, oncologyRecord));
    }

    @Test
    void doctorRecordCreationRequiresAssignedPatientAndNurse() {
        User alice = new User("Alice Alison", Role.DOCTOR, "Cardiology");
        User bob = new User("Bob Bobson", Role.NURSE, "Cardiology");
        MedicalRecord charlieWithBob = record("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord eveWithMallory = record("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");
        MedicalRecord missingNurse = new MedicalRecord("rec_pending", "Charlie Charles", "Alice Alison", "", "Cardiology", "Pending nurse assignment");

        assertTrue(AccessController.canCreate(alice));
        assertTrue(AccessController.canWrite(alice, charlieWithBob));
        assertFalse(AccessController.canWrite(alice, eveWithMallory));
        assertTrue(AccessController.canWrite(bob, charlieWithBob));
        assertFalse(AccessController.canWrite(bob, missingNurse));
    }

    @Test
    void governmentReadsEverythingButCannotWrite() {
        User dave = new User("Dave Davidson", Role.GOVERNMENT, null);
        MedicalRecord cardiologyRecord = record("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord oncologyRecord = record("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");

        assertTrue(AccessController.canRead(dave, cardiologyRecord));
        assertTrue(AccessController.canRead(dave, oncologyRecord));
        assertTrue(AccessController.canDelete(dave));
        assertFalse(AccessController.canWrite(dave, cardiologyRecord));
        assertFalse(AccessController.canWrite(dave, oncologyRecord));
    }

    @Test
    void governmentCannotCreateRecords() {
        User dave = new User("gov_dave", Role.GOVERNMENT, null);
        assertFalse(AccessController.canCreate(dave));
    }

    private MedicalRecord record(String id, String patient, String doctor, String nurse, String division, String data) {
        return new MedicalRecord(id, patient, doctor, nurse, division, data);
    }
}