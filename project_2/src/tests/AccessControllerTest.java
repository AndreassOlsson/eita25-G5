package src.tests;

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
        User alice = new User("alice", Role.PATIENT, null);
        MedicalRecord own = record("rec1", "alice", "doc1", "nurse1", "Cardiology");
        MedicalRecord foreign = record("rec2", "bob", "doc2", "nurse2", "Cardiology");

        assertTrue(AccessController.canRead(alice, own));
        assertFalse(AccessController.canRead(alice, foreign));
        assertFalse(AccessController.canWrite(alice, own));
        assertFalse(AccessController.canCreate(alice));
        assertFalse(AccessController.canDelete(alice));
    }

    @Test
    void nurseHasDivisionReadButOnlyAssignedWrite() {
        User nina = new User("nina", Role.NURSE, "Cardiology");
        MedicalRecord assigned = record("rec1", "p1", "doc1", "nina", "Cardiology");
        MedicalRecord sameDivision = record("rec2", "p2", "doc2", "nurse2", "Cardiology");
        MedicalRecord otherDivision = record("rec3", "p3", "doc3", "nurse3", "Radiology");

        assertTrue(AccessController.canRead(nina, assigned));
        assertTrue(AccessController.canRead(nina, sameDivision));
        assertFalse(AccessController.canRead(nina, otherDivision));
        assertTrue(AccessController.canWrite(nina, assigned));
        assertFalse(AccessController.canWrite(nina, sameDivision));
    }

    @Test
    void doctorCreatesAndWritesAssignedRecordsOnly() {
        User doc = new User("doc1", Role.DOCTOR, "Cardiology");
        MedicalRecord assigned = record("rec1", "p1", "doc1", "nurse1", "Cardiology");
        MedicalRecord otherDoctor = record("rec2", "p2", "doc2", "nurse2", "Cardiology");

        assertTrue(AccessController.canCreate(doc));
        assertTrue(AccessController.canRead(doc, assigned));
        assertTrue(AccessController.canRead(doc, otherDoctor));
        assertTrue(AccessController.canWrite(doc, assigned));
        assertFalse(AccessController.canWrite(doc, otherDoctor));
        assertFalse(AccessController.canDelete(doc));
    }

    @Test
    void governmentReadsEverythingButCannotWrite() {
        User gov = new User("gov", Role.GOVERNMENT, null);
        MedicalRecord record = record("rec1", "p1", "doc1", "nurse1", "Cardiology");

        assertTrue(AccessController.canRead(gov, record));
        assertTrue(AccessController.canDelete(gov));
        assertFalse(AccessController.canWrite(gov, record));
    }

    private MedicalRecord record(String id, String patient, String doctor, String nurse, String division) {
        return new MedicalRecord(id, patient, doctor, nurse, division, "sensitive");
    }
}