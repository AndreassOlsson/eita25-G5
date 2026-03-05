package tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.TestFixtures.DIV_CARDIOLOGY;
import static tests.TestFixtures.ID_DOCTOR_ALICE;
import static tests.TestFixtures.ID_DOCTOR_MALLORY;
import static tests.TestFixtures.ID_NURSE_CAROL;
import static tests.TestFixtures.ID_PATIENT_CHARLIE;
import static tests.TestFixtures.ID_PATIENT_EVE;
import static tests.TestFixtures.charlieBpRecord;
import static tests.TestFixtures.charlieFollowUpRecord;
import static tests.TestFixtures.doctorAlice;
import static tests.TestFixtures.eveOncologyRecord;
import static tests.TestFixtures.govDave;
import static tests.TestFixtures.nurseBob;
import static tests.TestFixtures.patientCharlie;
import static tests.TestFixtures.patientEve;

import org.junit.jupiter.api.Test;

import src.access.AccessController;
import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;

class AccessControllerTest {

    // ── Patient Access ──

    @Test
    void patientCanReadOwnRecord() {
        assertTrue(AccessController.canRead(patientCharlie(), charlieBpRecord()));
        assertTrue(AccessController.canRead(patientCharlie(), charlieFollowUpRecord()));
    }

    @Test
    void patientCannotReadOtherPatientsRecord() {
        assertFalse(AccessController.canRead(patientCharlie(), eveOncologyRecord()));
        assertFalse(AccessController.canRead(patientEve(), charlieBpRecord()));
    }

    @Test
    void patientCannotWriteOrDeleteOrCreate() {
        assertFalse(AccessController.canWrite(patientCharlie(), charlieBpRecord()));
        assertFalse(AccessController.canCreate(patientCharlie()));
        assertFalse(AccessController.canDelete(patientCharlie()));
    }

    // ── Nurse Access ──

    @Test
    void nurseCanReadAssignedRecord() {
        assertTrue(AccessController.canRead(nurseBob(), charlieBpRecord()));
    }

    @Test
    void nurseCanReadSameDivisionRecord() {
        // Carol's assignment, but Bob shares the Cardiology division
        assertTrue(AccessController.canRead(nurseBob(), charlieFollowUpRecord()));
    }

    @Test
    void nurseCannotReadCrossDivisionRecord() {
        assertFalse(AccessController.canRead(nurseBob(), eveOncologyRecord()));
    }

    @Test
    void nurseCanWriteAssignedRecord() {
        assertTrue(AccessController.canWrite(nurseBob(), charlieBpRecord()));
    }

    @Test
    void nurseCannotWriteUnassignedRecord() {
        // Same division, but Carol is assigned — not Bob
        assertFalse(AccessController.canWrite(nurseBob(), charlieFollowUpRecord()));
    }

    @Test
    void nurseCannotCreateOrDelete() {
        assertFalse(AccessController.canCreate(nurseBob()));
        assertFalse(AccessController.canDelete(nurseBob()));
    }

    // ── Doctor Access ──

    @Test
    void doctorCanReadAssignedRecord() {
        assertTrue(AccessController.canRead(doctorAlice(), charlieBpRecord()));
    }

    @Test
    void doctorCanReadSameDivisionRecord() {
        // Mallory's patient, but shared Cardiology division record
        MedicalRecord cardiologyByMallory = new MedicalRecord(
            "rec_shared", ID_PATIENT_EVE, ID_DOCTOR_MALLORY, ID_NURSE_CAROL, DIV_CARDIOLOGY, "Shared consult");
        assertTrue(AccessController.canRead(doctorAlice(), cardiologyByMallory));
    }

    @Test
    void doctorCannotReadCrossDivisionRecord() {
        assertFalse(AccessController.canRead(doctorAlice(), eveOncologyRecord()));
    }

    @Test
    void doctorCanWriteAssignedRecord() {
        assertTrue(AccessController.canWrite(doctorAlice(), charlieBpRecord()));
    }

    @Test
    void doctorCannotWriteUnassignedRecord() {
        assertFalse(AccessController.canWrite(doctorAlice(), eveOncologyRecord()));
    }

    @Test
    void doctorCanCreateRecords() {
        assertTrue(AccessController.canCreate(doctorAlice()));
    }

    @Test
    void doctorCannotDelete() {
        assertFalse(AccessController.canDelete(doctorAlice()));
    }

    // ── Government Access ──

    @Test
    void governmentCanReadAllRecords() {
        assertTrue(AccessController.canRead(govDave(), charlieBpRecord()));
        assertTrue(AccessController.canRead(govDave(), eveOncologyRecord()));
        assertTrue(AccessController.canRead(govDave(), charlieFollowUpRecord()));
    }

    @Test
    void governmentCanDelete() {
        assertTrue(AccessController.canDelete(govDave()));
    }

    @Test
    void governmentCannotWriteOrCreate() {
        assertFalse(AccessController.canWrite(govDave(), charlieBpRecord()));
        assertFalse(AccessController.canWrite(govDave(), eveOncologyRecord()));
        assertFalse(AccessController.canCreate(govDave()));
    }

    // ── Edge Cases ──

    @Test
    void nurseWithNullDivisionCannotAccessDivisionRecords() {
        User orphanNurse = new User("nurse_orphan", "Orphan", Role.NURSE, null);
        assertFalse(AccessController.canRead(orphanNurse, charlieBpRecord()));
    }

    @Test
    void doctorCanWriteOwnRecordButNotNurseField() {
        // Doctor is assigned as doctor, can write
        assertTrue(AccessController.canWrite(doctorAlice(), charlieBpRecord()));
        // But being listed as nurse doesn't grant doctor write access (nurse field != doctor field)
        MedicalRecord weirdRecord = new MedicalRecord(
            "rec_weird", ID_PATIENT_CHARLIE, ID_DOCTOR_MALLORY, ID_DOCTOR_ALICE, DIV_CARDIOLOGY, "Unusual");
        assertFalse(AccessController.canWrite(doctorAlice(), weirdRecord));
    }
}