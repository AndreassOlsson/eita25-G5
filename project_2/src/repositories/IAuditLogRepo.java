package src.repositories;

import java.io.IOException;

import src.models.AuditLogEntry;

public interface IAuditLogRepo {
    void log(AuditLogEntry entry) throws IOException;
}
