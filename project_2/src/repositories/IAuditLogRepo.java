package src.repositories;

import java.io.IOException;

import src.models.User;

public interface IAuditLogRepo {
    void log(User user, String action, String recordId, String details) throws IOException;
}
