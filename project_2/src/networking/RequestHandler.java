package src.networking;

import java.io.IOException;
import java.util.List;

import src.exceptions.BadRequestException;
import src.exceptions.PermissionDeniedException;
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
        
        MedicalRecord record = recordRepo.read(user, recordId);
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
        
        recordRepo.write(user, record);
        log(user, "WRITE", recordId, "Success");
        return "OK Record written";
    }

    private String handleDelete(User user, String[] parts) throws BadRequestException, PermissionDeniedException, IOException {
        if (parts.length < 2) throw new BadRequestException("Missing record ID");
        String recordId = parts[1];
        
        recordRepo.delete(user, recordId);
        log(user, "DELETE", recordId, "Success");
        return "OK Record deleted";
    }

    private String handleList(User user) throws IOException {
        List<String> records = recordRepo.listRecords(user);
        // Logging for LIST might be verbose if done per-request, but we can do it.
        // Or skip logging for LIST to reduce noise? The requirements say "read/write/access". LIST is access.
        log(user, "LIST", "ALL", "Success (" + records.size() + " records)");
        return "OK " + String.join(",", records);
    }
    
    private String handleHelp() {
        return "OK Available commands:\n" +
               "  READ <recordId>\n" +
               "  WRITE <recordId> <patientId>;<doctorId>;<nurseId>;<division>;<data>\n" +
               "  DELETE <recordId>\n" +
               "  LIST\n" +
               "  HELP";
    }

    private void log(User user, String action, String recordId, String details) {
        try {
            auditLogRepo.log(user, action, recordId, details);
        } catch (IOException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    private String getRecordId(String[] parts) {
        return (parts.length > 1) ? parts[1] : "N/A";
    }
}
