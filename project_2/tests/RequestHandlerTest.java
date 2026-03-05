package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.TestFixtures.DIV_CARDIOLOGY;
import static tests.TestFixtures.ID_DOCTOR_ALICE;
import static tests.TestFixtures.ID_DOCTOR_MALLORY;
import static tests.TestFixtures.ID_NURSE_BOB;
import static tests.TestFixtures.ID_NURSE_CAROL;
import static tests.TestFixtures.ID_PATIENT_CHARLIE;
import static tests.TestFixtures.ID_PATIENT_EVE;
import static tests.TestFixtures.REC_CHARLIE_BP;
import static tests.TestFixtures.REC_CHARLIE_FOLLOWUP;
import static tests.TestFixtures.REC_CHARLIE_NURSE;
import static tests.TestFixtures.REC_EVE_ONCOLOGY;
import static tests.TestFixtures.charlieBpRecord;
import static tests.TestFixtures.charlieFollowUpRecord;
import static tests.TestFixtures.charlieNurseRecord;
import static tests.TestFixtures.doctorAlice;
import static tests.TestFixtures.doctorMallory;
import static tests.TestFixtures.eveOncologyRecord;
import static tests.TestFixtures.govDave;
import static tests.TestFixtures.nurseBob;
import static tests.TestFixtures.patientCharlie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import src.models.MedicalRecord;
import src.networking.RequestHandler;
import src.repositories.LocalFSAuditLogRepo;
import src.repositories.LocalFSRecordRepo;

class RequestHandlerTest {

    @TempDir
    Path tempDir;

    private Path recordsDir;
    private Path auditLogFile;
    private LocalFSRecordRepo recordRepo;
    private LocalFSAuditLogRepo auditLogRepo;
    private RequestHandler handler;

    @BeforeEach
    void setUp() {
        recordsDir = tempDir.resolve("records");
        auditLogFile = tempDir.resolve("audit.log");
        recordRepo = new LocalFSRecordRepo(recordsDir.toString());
        auditLogRepo = new LocalFSAuditLogRepo(auditLogFile.toString());
        handler = new RequestHandler(recordRepo, auditLogRepo);
    }

    // ── READ ──

    @Test
    void readReturnsRecordPayloadAndIsAudited() throws Exception {
        seedRecord(charlieBpRecord());

        String response = handler.handle(doctorAlice(), "READ " + REC_CHARLIE_BP);

        assertTrue(response.startsWith("OK "));
        assertTrue(response.contains(charlieBpRecord().toString()));
        assertAuditContains("Action: READ");
    }

    @Test
    void readIsDeniedForCrossDivisionDoctor() throws Exception {
        seedRecord(charlieBpRecord());

        String response = handler.handle(doctorMallory(), "READ " + REC_CHARLIE_BP);

        assertTrue(response.startsWith("DENIED"));
        assertAuditContains("Denied");
    }

    @Test
    void patientCanReadOwnRecord() throws Exception {
        seedRecord(charlieBpRecord());

        String response = handler.handle(patientCharlie(), "READ " + REC_CHARLIE_BP);

        assertTrue(response.startsWith("OK "));
        assertTrue(response.contains(REC_CHARLIE_BP));
    }

    // ── WRITE (create new) ──

    @Test
    void doctorCanCreateNewRecord() throws Exception {
        String cmd = writeCommand(REC_CHARLIE_FOLLOWUP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Cardiology progress note");

        String response = handler.handle(doctorAlice(), cmd);

        assertTrue(response.startsWith("OK"));
        Path recordFile = recordsDir.resolve(REC_CHARLIE_FOLLOWUP + ".txt");
        assertTrue(Files.exists(recordFile));
        List<String> lines = Files.readAllLines(recordFile);
        assertFalse(lines.isEmpty());
        assertEquals(REC_CHARLIE_FOLLOWUP + ";" + ID_PATIENT_CHARLIE + ";" + ID_DOCTOR_ALICE + ";" + ID_NURSE_BOB + ";" + DIV_CARDIOLOGY + ";Cardiology progress note", lines.get(0));
        assertAuditContains("Action: WRITE");
    }

    @Test
    void writeRejectsMalformedPayload() {
        String response = handler.handle(doctorAlice(), "WRITE " + REC_CHARLIE_BP + " " + ID_PATIENT_CHARLIE + ";" + ID_DOCTOR_ALICE + ";" + DIV_CARDIOLOGY + ";Stress test results");

        assertTrue(response.startsWith("ERROR"));
        assertTrue(response.contains("Invalid data format"));
    }

    // ── WRITE (modify existing) ──

    @Test
    void doctorCanModifyAssignedRecord() throws Exception {
        seedRecord(new MedicalRecord(REC_CHARLIE_BP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Initial plan"));

        String cmd = writeCommand(REC_CHARLIE_BP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Updated care plan");
        String response = handler.handle(doctorAlice(), cmd);

        assertTrue(response.startsWith("OK"));
        List<String> lines = Files.readAllLines(recordsDir.resolve(REC_CHARLIE_BP + ".txt"));
        assertTrue(lines.get(0).endsWith("Updated care plan"));
    }

    @Test
    void nurseCanModifyAssignedRecord() throws Exception {
        seedRecord(charlieNurseRecord());

        String cmd = writeCommand(REC_CHARLIE_NURSE, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Nurse follow-up");
        String response = handler.handle(nurseBob(), cmd);

        assertTrue(response.startsWith("OK"));
        List<String> lines = Files.readAllLines(recordsDir.resolve(REC_CHARLIE_NURSE + ".txt"));
        assertTrue(lines.get(0).endsWith("Nurse follow-up"));
    }

    @Test
    void doctorCannotOverwriteUnassignedRecord() throws Exception {
        seedRecord(eveOncologyRecord());

        String cmd = writeCommand(REC_EVE_ONCOLOGY, ID_PATIENT_EVE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Unauthorized update");
        String response = handler.handle(doctorAlice(), cmd);

        assertTrue(response.startsWith("DENIED"));
        List<String> lines = Files.readAllLines(recordsDir.resolve(REC_EVE_ONCOLOGY + ".txt"));
        assertTrue(lines.get(0).contains(ID_DOCTOR_MALLORY));
        assertFalse(lines.get(0).contains("Unauthorized update"));
    }

    @Test
    void nurseCannotOverwriteUnassignedRecord() throws Exception {
        seedRecord(charlieFollowUpRecord()); // Carol is assigned, not Bob

        String cmd = writeCommand(REC_CHARLIE_FOLLOWUP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Bob overwrite attempt");
        String response = handler.handle(nurseBob(), cmd);

        assertTrue(response.startsWith("DENIED"));
        List<String> lines = Files.readAllLines(recordsDir.resolve(REC_CHARLIE_FOLLOWUP + ".txt"));
        assertTrue(lines.get(0).contains(ID_NURSE_CAROL));
        assertFalse(lines.get(0).contains("Bob overwrite attempt"));
    }

    @Test
    void patientCannotWriteRecord() throws Exception {
        seedRecord(charlieBpRecord());

        String cmd = writeCommand(REC_CHARLIE_BP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Patient overwrite attempt");
        String response = handler.handle(patientCharlie(), cmd);

        assertTrue(response.startsWith("DENIED"));
        List<String> lines = Files.readAllLines(recordsDir.resolve(REC_CHARLIE_BP + ".txt"));
        assertFalse(lines.get(0).contains("Patient overwrite attempt"));
    }

    @Test
    void governmentCannotWriteRecords() {
        String cmd = writeCommand(REC_CHARLIE_BP, ID_PATIENT_CHARLIE, ID_DOCTOR_ALICE, ID_NURSE_BOB, DIV_CARDIOLOGY, "Government note");
        String response = handler.handle(govDave(), cmd);

        assertTrue(response.startsWith("DENIED"));
    }

    // ── DELETE ──

    @Test
    void governmentCanDeleteRecord() throws Exception {
        seedRecord(charlieBpRecord());

        String response = handler.handle(govDave(), "DELETE " + REC_CHARLIE_BP);

        assertTrue(response.startsWith("OK"));
        assertFalse(Files.exists(recordsDir.resolve(REC_CHARLIE_BP + ".txt")));
        assertAuditContains("Action: DELETE");
    }

    // ── LIST ──

    @Test
    void listFiltersRecordsByAccess() throws Exception {
        seedRecord(charlieBpRecord());
        seedRecord(eveOncologyRecord());

        String response = handler.handle(doctorAlice(), "LIST");

        assertTrue(response.startsWith("OK"));
        assertTrue(response.contains(REC_CHARLIE_BP));
        assertFalse(response.contains(REC_EVE_ONCOLOGY));
        assertAuditContains("Action: LIST");
    }

    @Test
    void patientListReturnsOnlyOwnRecords() throws Exception {
        seedRecord(charlieBpRecord());
        seedRecord(eveOncologyRecord());

        String response = handler.handle(patientCharlie(), "LIST");

        assertTrue(response.startsWith("OK"));
        assertTrue(response.contains(REC_CHARLIE_BP));
        assertFalse(response.contains(REC_EVE_ONCOLOGY));
        assertAuditContains("Action: LIST");
    }

    // ── Helpers ──

    private void seedRecord(MedicalRecord record) throws IOException {
        recordRepo.write(record);
    }

    private String writeCommand(String recId, String patientId, String doctorId, String nurseId, String division, String data) {
        return "WRITE " + recId + " " + patientId + ";" + doctorId + ";" + nurseId + ";" + division + ";" + data;
    }

    private void assertAuditContains(String fragment) throws IOException {
        String contents = Files.readString(auditLogFile);
        assertTrue(contents.contains(fragment), "Audit log should contain: " + fragment);
    }
}