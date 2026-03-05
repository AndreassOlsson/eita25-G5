package src.networking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import src.access.AccessController;
import src.exceptions.BadRequestException;
import src.exceptions.PermissionDeniedException;
import src.models.AuditLogEntry;
import src.models.MedicalRecord;
import src.models.User;
import src.repositories.IAuditLogRepo;
import src.repositories.IRecordRepo;

public class RequestHandler {
    private final IRecordRepo recordRepo;
    private final IAuditLogRepo auditLogRepo;

    public RequestHandler(IRecordRepo recordRepo, IAuditLogRepo auditLogRepo) {
        this.recordRepo = recordRepo;
        this.auditLogRepo = auditLogRepo;
    }

    public String handle(User user, String requestLine) {
        String[] parts = requestLine.split(" ", 3);
        if (parts.length == 0) return "ERROR Empty request";
        String command = parts[0].toUpperCase();

        try {
            switch (command) {
                case "READ":
                    return handleRead(user, parts);
                case "WRITE":
                    return handleWrite(user, parts);
                case "DELETE":
                    return handleDelete(user, parts);
                case "LIST":
                    return handleList(user);
                case "HELP":
                    return handleHelp();
                default:
                    throw new BadRequestException("Unknown command: " + command);
            }
        } catch (PermissionDeniedException e) {
            log(user, command, getRecordId(parts), "Denied: " + e.getMessage());
            return "DENIED " + e.getMessage();
        } catch (BadRequestException e) {
            log(user, command, "N/A", "Bad Request: " + e.getMessage());
            return "ERROR " + e.getMessage();
        } catch (IOException e) {
            log(user, command, getRecordId(parts), "System Error: " + e.getMessage());
            return "ERROR System failure";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR Internal Server Error";
        }
    }

    private String handleRead(User user, String[] parts) throws BadRequestException, PermissionDeniedException, IOException {
        if (parts.length < 2) throw new BadRequestException("Missing record ID");
        String recordId = parts[1];
        
        MedicalRecord record = recordRepo.read(recordId);

        if (!AccessController.canRead(user, record)) {
            logAccess(user, "DENIED", "Read", recordId);
            throw new PermissionDeniedException("Access denied for user: " + user.getId());
        }

        logAccess(user, "GRANTED", "Read", recordId);
        log(user, "READ", recordId, "Success");
        return "OK " + record.toString();
    }

    private String handleWrite(User user, String[] parts) throws BadRequestException, PermissionDeniedException, IOException {
        if (parts.length < 3) throw new BadRequestException("Missing data");
        String recordId = parts[1];
        String recordData = parts[2];
        
        String[] dataParts = recordData.split(";", 5);
        if (dataParts.length < 5) throw new BadRequestException("Invalid data format. Expected: patientId;doctorId;nurseId;division;data");

        MedicalRecord record = new MedicalRecord(
            recordId, 
            dataParts[0], 
            dataParts[1], 
            dataParts[2], 
            dataParts[3], 
            dataParts[4]
        );

        MedicalRecord existing = null;
        try {
            existing = recordRepo.read(recordId);
        } catch (IOException e) {
            // Record does not exist — this is a creation
        }

        if (existing == null) {
            if (!AccessController.canCreate(user)) {
                logAccess(user, "DENIED", "Create", recordId);
                throw new PermissionDeniedException("User " + user.getId() + " not allowed to create records.");
            }
            logAccess(user, "GRANTED", "Create", recordId);
        } else {
            if (!AccessController.canWrite(user, existing)) {
                logAccess(user, "DENIED", "Write", recordId);
                throw new PermissionDeniedException("User " + user.getId() + " not allowed to modify this record.");
            }
            logAccess(user, "GRANTED", "Write", recordId);
        }
        
        recordRepo.write(record);
        log(user, "WRITE", recordId, "Success");
        return "OK Record written";
    }

    private String handleDelete(User user, String[] parts) throws BadRequestException, PermissionDeniedException, IOException {
        if (parts.length < 2) throw new BadRequestException("Missing record ID");
        String recordId = parts[1];

        // Verify record exists (read will throw if not found)
        recordRepo.read(recordId);

        if (!AccessController.canDelete(user)) {
            logAccess(user, "DENIED", "Delete", recordId);
            throw new PermissionDeniedException("User " + user.getId() + " not allowed to delete records.");
        }
        
        logAccess(user, "GRANTED", "Delete", recordId);
        recordRepo.delete(recordId);
        log(user, "DELETE", recordId, "Success");
        return "OK Record deleted";
    }

    private String handleList(User user) throws IOException {
        List<String> allIds = recordRepo.list();
        List<String> accessible = new ArrayList<>();
        for (String id : allIds) {
            try {
                MedicalRecord r = recordRepo.read(id);
                if (AccessController.canRead(user, r)) {
                    accessible.add(id);
                }
            } catch (IOException e) {
                // Skip unreadable records
            }
        }
        logAccess(user, "GRANTED", "List", accessible.size() + " records");
        log(user, "LIST", "ALL", "Success (" + accessible.size() + " records)");
        return "OK " + String.join(",", accessible);
    }
    
    private String handleHelp() {
        return "OK Available commands:\n" +
               "  READ <recordId>\n" +
               "  WRITE <recordId> <patientId>;<doctorId>;<nurseId>;<division>;<data>\n" +
               "  DELETE <recordId>\n" +
               "  LIST\n" +
               "  HELP";
    }

    /**
     * Prints a structured, non-sensitive access decision to stdout.
     * Format: "<userId> <GRANTED|DENIED> <action> Record <recordId>"
     */
    private void logAccess(User user, String decision, String action, String recordId) {
        System.out.println(user.getId() + " " + decision + " " + action + " Record " + recordId);
    }

    private void log(User user, String action, String recordId, String details) {
        try {
            AuditLogEntry entry = new AuditLogEntry(user.getId(), action, recordId, details);
            auditLogRepo.log(entry);
        } catch (IOException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    private String getRecordId(String[] parts) {
        return (parts.length > 1) ? parts[1] : "N/A";
    }
}
