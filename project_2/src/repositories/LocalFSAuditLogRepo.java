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
        this("localDB/audit_log.txt");
    }

    public LocalFSAuditLogRepo(String logPath) {
        this.logPath = logPath;
        initDB();
    }

    private void initDB() {
        try {
            ensureLogFileExists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureLogFileExists() throws IOException {
        Path path = Paths.get(logPath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    @Override
    public void log(User user, String action, String recordId, String details) throws IOException {
        ensureLogFileExists();
        String entry = String.format("%s | User: %s | Action: %s | Record: %s | Details: %s",
                LocalDateTime.now(), user.getUsername(), action, recordId, details);
        
        Files.write(Paths.get(logPath), Collections.singletonList(entry), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
