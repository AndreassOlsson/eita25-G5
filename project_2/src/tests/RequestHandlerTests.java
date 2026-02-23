package src.tests;

import src.models.Role;
import src.models.User;
import src.networking.RequestHandler;
import src.tests.mocks.MockAuditLogRepo;
import src.tests.mocks.MockRecordRepo;

public class RequestHandlerTests {

    public static void run() {
        System.out.println("Running RequestHandlerTests...");
        
        testReadFlow();
        testWriteFlow();
        testDeleteFlow();
        
        System.out.println("RequestHandlerTests Passed!\n");
    }

    private static void testReadFlow() {
        MockRecordRepo recordRepo = new MockRecordRepo();
        MockAuditLogRepo auditLogRepo = new MockAuditLogRepo();
        RequestHandler handler = new RequestHandler(recordRepo, auditLogRepo);
        
        User user = new User("tester", Role.DOCTOR, "div1");

        // Test valid read
        String response = handler.handle(user, "READ rec1");
        assertTrue(response.startsWith("OK"), "Should allow reading existing record (Mock logic relies on AccessController which we stubbed or AccessController is static? Real AccessController is used here, so we must be careful with user roles match mock data)");
        
        // Actually, MockRepo doesn't enforce AccessController! 
        // Real LocalFSRecordRepo calls AccessController. MockRepo does passed-through.
        // Wait, the real LocalFSRecordRepo calls AccessController inside read(). 
        // My MockRecordRepo in previous step DOES NOT call AccessController.
        // This effectively tests the RequestHandler's parsing, not the permission logic (which is tested in AccessControlTests).
        
        // Test missing ID
        response = handler.handle(user, "READ");
        assertTrue(response.startsWith("ERROR"), "Should return ERROR for missing ID");
    }

    private static void testWriteFlow() {
        MockRecordRepo recordRepo = new MockRecordRepo();
        MockAuditLogRepo auditLogRepo = new MockAuditLogRepo();
        RequestHandler handler = new RequestHandler(recordRepo, auditLogRepo);
        
        User user = new User("tester", Role.DOCTOR, "div1");

        // Valid write
        String cmd = "WRITE rec2 patient;doc;nurse;div;data";
        String response = handler.handle(user, cmd);
        assertTrue(response.startsWith("OK"), "Should write successfully");
        assertTrue(auditLogRepo.logs.contains("tester:WRITE:rec2"), "Should audit log the write");
        
        // Invalid write format
        response = handler.handle(user, "WRITE rec3 garbage_data");
        assertTrue(response.startsWith("ERROR"), "Should fail on bad data format");
    }

    private static void testDeleteFlow() {
        MockRecordRepo recordRepo = new MockRecordRepo();
        MockAuditLogRepo auditLogRepo = new MockAuditLogRepo();
        RequestHandler handler = new RequestHandler(recordRepo, auditLogRepo);
        
        User user = new User("gov", Role.GOVERNMENT, null);

        // Valid delete
        String response = handler.handle(user, "DELETE rec1");
        assertTrue(response.startsWith("OK"), "Should delete successfully");
        assertTrue(auditLogRepo.logs.contains("gov:DELETE:rec1"), "Should audit log the delete");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            System.err.println("❌ FAILED: " + message);
            throw new RuntimeException("Test failed: " + message);
        } else {
            System.out.println("✅ " + message);
        }
    }
}
