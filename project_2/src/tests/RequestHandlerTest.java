package src.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import src.models.MedicalRecord;
import src.models.Role;
import src.models.User;
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

    @Test
    void readReturnsRecordPayloadAndIsAudited() throws Exception {
        MedicalRecord record = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(doctorUser(), "READ rec_charlie_bp");

        assertTrue(response.startsWith("OK "), "READ should succeed for Alice treating Charlie");
        assertTrue(response.contains(record.toString()), "Payload should contain serialized record data");
        assertAuditContains("Action: READ");
    }

    @Test
    void readIsDeniedWhenAccessControllerRejectsUser() throws Exception {
        MedicalRecord record = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(oncologyDoctor(), "READ rec_charlie_bp");

        assertTrue(response.startsWith("DENIED"), "Mallory from Oncology should be denied");
        assertAuditContains("Denied");
    }

    @Test
    void writePersistsRecordWithRealRepository() throws Exception {
        String cmd = "WRITE rec_charlie_followup Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Cardiology progress note";

        String response = handler.handle(doctorUser(), cmd);

        assertTrue(response.startsWith("OK"), "WRITE should succeed for Alice");
        Path recordFile = recordsDir.resolve("rec_charlie_followup.txt");
        assertTrue(Files.exists(recordFile), "Record file should exist on disk");
        List<String> lines = Files.readAllLines(recordFile);
        assertFalse(lines.isEmpty(), "Record file must contain data");
        assertEquals("rec_charlie_followup;Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Cardiology progress note", lines.get(0));
        assertAuditContains("Action: WRITE");
    }

    @Test
    void writeRejectsMalformedPayload() {
        String response = handler.handle(doctorUser(), "WRITE rec_charlie_malformed Charlie Charles;Alice Alison;Cardiology;Stress test results");

        assertTrue(response.startsWith("ERROR"), "Handler should reject payload without nurse assignment");
        assertTrue(response.contains("Invalid data format"), "Response should describe the missing nurse field");
    }

    @Test
    void deleteRemovesRecordForGovernmentUser() throws Exception {
        MedicalRecord record = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(governmentUser(), "DELETE rec_charlie_bp");

        assertTrue(response.startsWith("OK"), "Government user should be able to delete");
        assertFalse(Files.exists(recordsDir.resolve("rec_charlie_bp.txt")), "Record file should be gone");
        assertAuditContains("Action: DELETE");
    }

    @Test
    void listOnlyReturnsRecordsUserCanRead() throws Exception {
        MedicalRecord cardiologyRecord = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord oncologyRecord = new MedicalRecord("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");
        recordRepo.write(doctorUser(), cardiologyRecord);
        recordRepo.write(oncologyDoctor(), oncologyRecord);

        String response = handler.handle(doctorUser(), "LIST");

        assertTrue(response.startsWith("OK"), "LIST should succeed");
        assertTrue(response.contains("rec_charlie_bp"), "Alice should see Cardiology records");
        assertFalse(response.contains("rec_eve_oncology"), "Alice must not see Oncology records");
        assertAuditContains("Action: LIST");
    }

    @Test
    void patientListReturnsOwnRecords() throws Exception {
        MedicalRecord charlieRecord = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        MedicalRecord eveRecord = new MedicalRecord("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");
        recordRepo.write(doctorUser(), charlieRecord);
        recordRepo.write(oncologyDoctor(), eveRecord);

        String response = handler.handle(patientUser(), "LIST");

        assertTrue(response.startsWith("OK"), "LIST should succeed for Charlie");
        assertTrue(response.contains("rec_charlie_bp"), "Charlie should receive his own record ID");
        assertFalse(response.contains("rec_eve_oncology"), "Charlie must not see Eve's record");
        assertAuditContains("Action: LIST");
    }

    @Test
    void doctorCanModifyAssignedRecord() throws Exception {
        MedicalRecord existing = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Initial plan");
        recordRepo.write(doctorUser(), existing);

        String response = handler.handle(doctorUser(), "WRITE rec_charlie_bp Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Updated care plan");

        assertTrue(response.startsWith("OK"), "Doctor should be allowed to modify their own record");
        List<String> lines = Files.readAllLines(recordsDir.resolve("rec_charlie_bp.txt"));
        assertTrue(lines.get(0).endsWith("Updated care plan"), "Modified record should be persisted");
    }

    @Test
    void nurseCanModifyAssignedRecord() throws Exception {
        MedicalRecord existing = new MedicalRecord("rec_charlie_nurse", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Nurse intake");
        recordRepo.write(doctorUser(), existing);

        String response = handler.handle(nurseUser(), "WRITE rec_charlie_nurse Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Nurse follow-up");

        assertTrue(response.startsWith("OK"), "Assigned nurse should be able to update the record");
        List<String> lines = Files.readAllLines(recordsDir.resolve("rec_charlie_nurse.txt"));
        assertTrue(lines.get(0).endsWith("Nurse follow-up"), "Updated nurse payload should be stored");
    }

    @Test
    void patientCanReadOwnRecord() throws Exception {
        MedicalRecord record = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(patientUser(), "READ rec_charlie_bp");

        assertTrue(response.startsWith("OK"), "Patient should be able to read their own record");
        assertTrue(response.contains("rec_charlie_bp"));
    }

    @Test
    void doctorCannotOverwriteUnassignedRecord() throws Exception {
        MedicalRecord malloryRecord = new MedicalRecord("rec_eve_oncology", "Eve Evans", "Mallory Mallet", "Carol Carlson", "Oncology", "Oncology intake");
        recordRepo.write(oncologyDoctor(), malloryRecord);

        String response = handler.handle(doctorUser(), "WRITE rec_eve_oncology Eve Evans;Alice Alison;Bob Bobson;Cardiology;Unauthorized update");

        assertTrue(response.startsWith("DENIED"), "Alice should not be able to overwrite Mallory's record");
        List<String> lines = Files.readAllLines(recordsDir.resolve("rec_eve_oncology.txt"));
        assertTrue(lines.get(0).contains("Mallory Mallet"), "Record must remain assigned to Mallory");
        assertFalse(lines.get(0).contains("Unauthorized update"), "Payload must remain unchanged");
    }

    @Test
    void nurseCannotOverwriteUnassignedRecord() throws Exception {
        MedicalRecord carolRecord = new MedicalRecord("rec_charlie_followup", "Charlie Charles", "Alice Alison", "Carol Carlson", "Cardiology", "Follow-up consultation");
        recordRepo.write(doctorUser(), carolRecord);

        String response = handler.handle(nurseUser(), "WRITE rec_charlie_followup Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Bob overwrite attempt");

        assertTrue(response.startsWith("DENIED"), "Bob cannot modify Carol's assigned record");
        List<String> lines = Files.readAllLines(recordsDir.resolve("rec_charlie_followup.txt"));
        assertTrue(lines.get(0).contains("Carol Carlson"), "Assignment must remain with Carol");
        assertFalse(lines.get(0).contains("Bob overwrite attempt"), "Payload must remain unchanged");
    }

    @Test
    void patientCannotOverwriteRecord() throws Exception {
        MedicalRecord record = new MedicalRecord("rec_charlie_bp", "Charlie Charles", "Alice Alison", "Bob Bobson", "Cardiology", "Blood Pressure 120/80");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(patientUser(), "WRITE rec_charlie_bp Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Patient overwrite attempt");

        assertTrue(response.startsWith("DENIED"), "Patients must not be able to modify records");
        List<String> lines = Files.readAllLines(recordsDir.resolve("rec_charlie_bp.txt"));
        assertFalse(lines.get(0).contains("Patient overwrite attempt"), "Record should remain untouched");
    }

    @Test
    void governmentCannotWriteRecords() {
        String response = handler.handle(governmentUser(), "WRITE rec_charlie_bp Charlie Charles;Alice Alison;Bob Bobson;Cardiology;Government note");

        assertTrue(response.startsWith("DENIED"), "Government agencies must not be able to write records");
    }

    private User doctorUser() {
        return new User("Alice Alison", Role.DOCTOR, "Cardiology");
    }

    private User oncologyDoctor() {
        return new User("Mallory Mallet", Role.DOCTOR, "Oncology");
    }

    private User nurseUser() {
        return new User("Bob Bobson", Role.NURSE, "Cardiology");
    }

    private User patientUser() {
        return new User("Charlie Charles", Role.PATIENT, null);
    }

    private User governmentUser() {
        return new User("Dave Davidson", Role.GOVERNMENT, null);
    }

    private void assertAuditContains(String fragment) throws IOException {
        String contents = Files.readString(auditLogFile);
        assertTrue(contents.contains(fragment), "Audit log should contain " + fragment);
    }
}