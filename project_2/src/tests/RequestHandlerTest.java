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
        MedicalRecord record = new MedicalRecord("rec1", "patient", "doc1", "nurse1", "Cardiology", "payload");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(doctorUser(), "READ rec1");

        assertTrue(response.startsWith("OK "), "READ should succeed for assigned doctor");
        assertTrue(response.contains(record.toString()), "Payload should contain serialized record data");
        assertAuditContains("Action: READ");
    }

    @Test
    void readIsDeniedWhenAccessControllerRejectsUser() throws Exception {
        MedicalRecord record = new MedicalRecord("rec1", "patient", "doc1", "nurse1", "Cardiology", "payload");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(new User("nurseX", Role.NURSE, "Radiology"), "READ rec1");

        assertTrue(response.startsWith("DENIED"), "Nurse from another division should be denied");
        assertAuditContains("Denied");
    }

    @Test
    void writePersistsRecordWithRealRepository() throws Exception {
        String cmd = "WRITE rec2 patient;doc1;nurse1;Cardiology;secure";

        String response = handler.handle(doctorUser(), cmd);

        assertTrue(response.startsWith("OK"), "WRITE should succeed for doctor");
        Path recordFile = recordsDir.resolve("rec2.txt");
        assertTrue(Files.exists(recordFile), "Record file should exist on disk");
        List<String> lines = Files.readAllLines(recordFile);
        assertFalse(lines.isEmpty(), "Record file must contain data");
        assertEquals("rec2;patient;doc1;nurse1;Cardiology;secure", lines.get(0));
        assertAuditContains("Action: WRITE");
    }

    @Test
    void writeRejectsMalformedPayload() {
        String response = handler.handle(doctorUser(), "WRITE rec2 bad_format");

        assertTrue(response.startsWith("ERROR"), "Handler should return ERROR for malformed payload");
    }

    @Test
    void deleteRemovesRecordForGovernmentUser() throws Exception {
        MedicalRecord record = new MedicalRecord("rec1", "patient", "doc1", "nurse1", "Cardiology", "payload");
        recordRepo.write(doctorUser(), record);

        String response = handler.handle(governmentUser(), "DELETE rec1");

        assertTrue(response.startsWith("OK"), "Government user should be able to delete");
        assertFalse(Files.exists(recordsDir.resolve("rec1.txt")), "Record file should be gone");
        assertAuditContains("Action: DELETE");
    }

    @Test
    void listOnlyReturnsRecordsUserCanRead() throws Exception {
        MedicalRecord visible = new MedicalRecord("rec1", "patient", "doc1", "nurse1", "Cardiology", "payload");
        MedicalRecord hidden = new MedicalRecord("rec2", "patient", "doc2", "nurse2", "Radiology", "payload");
        recordRepo.write(doctorUser(), visible);
        recordRepo.write(doctorUser(), hidden);

        String response = handler.handle(doctorUser(), "LIST");

        assertTrue(response.startsWith("OK"), "LIST should succeed");
        assertTrue(response.contains("rec1"), "Doctor should see records from their division");
        assertFalse(response.contains("rec2"), "Doctor should not see records they cannot read");
        assertAuditContains("Action: LIST");
    }

    private User doctorUser() {
        return new User("doc1", Role.DOCTOR, "Cardiology");
    }

    private User governmentUser() {
        return new User("gov", Role.GOVERNMENT, null);
    }

    private void assertAuditContains(String fragment) throws IOException {
        String contents = Files.readString(auditLogFile);
        assertTrue(contents.contains(fragment), "Audit log should contain " + fragment);
    }
}