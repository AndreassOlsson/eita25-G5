package src.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import src.access.AccessController;
import src.models.MedicalRecord;
import src.models.PermissionDeniedException;
import src.models.User;

public class LocalFSRecordRepo implements IRecordRepo {
    private final String dbPath;

    public LocalFSRecordRepo() {
        this.dbPath = "localDB/records"; 
        initDB();
    }
    
    public LocalFSRecordRepo(String dbPath) {
        this.dbPath = dbPath;
        initDB();
    }

    private void initDB() {
        try {
            Files.createDirectories(Paths.get(dbPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MedicalRecord read(User user, String recordId) throws PermissionDeniedException, IOException {
        MedicalRecord record = readFromFile(recordId);
        
        if (record == null) {
            throw new IOException("Record not found: " + recordId);
        }

        if (!AccessController.canRead(user, record)) {
            // Log the denied access attempt here if needed
            System.err.println("Access Denied: User " + user.getUsername() + " tried to read record " + recordId);
            throw new PermissionDeniedException("Access denied for user: " + user.getUsername());
        }
        
        return record;
    }

    @Override
    public void write(User user, MedicalRecord record) throws PermissionDeniedException, IOException {
        // If the record exists, check if user can write (modify) it.
        // If it's new, check if user can create.
        
        MedicalRecord existing = readFromFile(record.getId());
        
        if (existing == null) {
            // Creation
            if (!AccessController.canCreate(user)) {
                 throw new PermissionDeniedException("User " + user.getUsername() + " not allowed to create records.");
            }
        } else {
            // Modification
            if (!AccessController.canWrite(user, existing)) {
                 throw new PermissionDeniedException("User " + user.getUsername() + " not allowed to modify this record.");
            }
        }

        writeToFile(record);
    }

    @Override
    public void delete(User user, String recordId) throws PermissionDeniedException, IOException {
        MedicalRecord record = readFromFile(recordId);
        if (record == null) {
             throw new IOException("Record not found: " + recordId);
        }

        if (!AccessController.canDelete(user)) {
             throw new PermissionDeniedException("User " + user.getUsername() + " not allowed to delete records.");
        }
        
        Path file = Paths.get(dbPath, recordId + ".txt");
        Files.deleteIfExists(file);
    }

    private MedicalRecord readFromFile(String id) throws IOException {
        Path file = Paths.get(dbPath, id + ".txt");
        if (!Files.exists(file)) return null;
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) return null;
        // Assuming first line contains the data
        return MedicalRecord.fromString(lines.get(0));
    }

    private void writeToFile(MedicalRecord record) throws IOException {
        Path file = Paths.get(dbPath, record.getId() + ".txt");
        Files.write(file, Collections.singletonList(record.toString()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
