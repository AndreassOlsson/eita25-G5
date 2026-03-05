package src.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import src.models.MedicalRecord;

public class LocalFSRecordRepo implements IRecordRepo {
    private final String dbPath;

    public LocalFSRecordRepo() {
        this("localDB/records"); 
    }
    
    public LocalFSRecordRepo(String dbPath) {
        this.dbPath = dbPath;
        initDB();
    }

    private void initDB() {
        try {
            ensureStorageDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureStorageDirectory() throws IOException {
        Files.createDirectories(Paths.get(dbPath));
    }

    @Override
    public MedicalRecord read(String recordId) throws IOException {
        ensureStorageDirectory();
        Path file = Paths.get(dbPath, recordId + ".txt");
        if (!Files.exists(file)) {
            throw new IOException("Record not found: " + recordId);
        }
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) {
            throw new IOException("Record file is empty: " + recordId);
        }
        MedicalRecord record = MedicalRecord.fromString(lines.get(0));
        if (record == null) {
            throw new IOException("Invalid record format: " + recordId);
        }
        return record;
    }

    @Override
    public void write(MedicalRecord record) throws IOException {
        ensureStorageDirectory();
        Path file = Paths.get(dbPath, record.getId() + ".txt");
        Files.write(file, Collections.singletonList(record.toString()),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void delete(String recordId) throws IOException {
        ensureStorageDirectory();
        Path file = Paths.get(dbPath, recordId + ".txt");
        if (!Files.exists(file)) {
            throw new IOException("Record not found: " + recordId);
        }
        Files.delete(file);
    }

    @Override
    public List<String> list() throws IOException {
        ensureStorageDirectory();
        try (Stream<Path> stream = Files.list(Paths.get(dbPath))) {
            return stream
                .filter(file -> !Files.isDirectory(file))
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.endsWith(".txt"))
                .map(name -> name.substring(0, name.length() - 4))
                .collect(Collectors.toList());
        }
    }
}