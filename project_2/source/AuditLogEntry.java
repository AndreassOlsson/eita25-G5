package source;
import java.time.LocalDateTime;

public class AuditLogEntry {
    public String user;
    public String action;
    public String recordId;
    public LocalDateTime timestamp;

    public AuditLogEntry(String user, String action, String recordId) {
        this.user = user;
        this.action = action;
        this.recordId = recordId;
        this.timestamp = LocalDateTime.now();
    }
}
