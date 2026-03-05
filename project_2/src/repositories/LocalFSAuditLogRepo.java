package src.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import src.models.AuditLogEntry;

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
    public void log(AuditLogEntry entry) throws IOException {
        ensureLogFileExists();
        Files.write(Paths.get(logPath), Collections.singletonList(entry.toString()),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
