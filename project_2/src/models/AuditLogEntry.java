package src.models;

import java.time.LocalDateTime;

public class AuditLogEntry {
    private final String userId;
    private final String action;
    private final String recordId;
    private final String details;
    private final LocalDateTime timestamp;

    public AuditLogEntry(String userId, String action, String recordId, String details) {
        this.userId = userId;
        this.action = action;
        this.recordId = recordId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getRecordId() { return recordId; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | User: %s | Action: %s | Record: %s | Details: %s",
                timestamp, userId, action, recordId, details);
    }
}
