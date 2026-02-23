package src.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Collections;

import src.models.User;

public class LocalFSAuditLogRepo implements IAuditLogRepo {
    private final String logPath;

    public LocalFSAuditLogRepo() {
        this.logPath = "localDB/audit_log.txt";
        initDB();
    }

    private void initDB() {
        try {
            Path path = Paths.get(logPath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(User user, String action, String recordId, String details) throws IOException {
        String entry = String.format("%s | User: %s | Action: %s | Record: %s | Details: %s",
                LocalDateTime.now(), user.getUsername(), action, recordId, details);
        
        Files.write(Paths.get(logPath), Collections.singletonList(entry), StandardOpenOption.APPEND);
    }
}
