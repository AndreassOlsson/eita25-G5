package src.tests.mocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.exceptions.PermissionDeniedException;
import src.models.MedicalRecord;
import src.models.User;
import src.repositories.IRecordRepo;

public class MockRecordRepo implements IRecordRepo {
    private Map<String, MedicalRecord> db = new HashMap<>();

    public MockRecordRepo() {
        // Pre-populate with some data
        db.put("rec1", new MedicalRecord("rec1", "p1", "d1", "n1", "div1", "data1"));
    }

    @Override
    public MedicalRecord read(User user, String recordId) throws PermissionDeniedException, IOException {
        if (!db.containsKey(recordId)) {
            throw new IOException("Record not found");
        }
        return db.get(recordId);
    }

    @Override
    public void write(User user, MedicalRecord record) throws PermissionDeniedException, IOException {
        db.put(record.getId(), record);
    }

    @Override
    public void delete(User user, String recordId) throws PermissionDeniedException, IOException {
        if (!db.containsKey(recordId)) {
            throw new IOException("Record not found");
        }
        db.remove(recordId);
    }

    @Override
    public List<String> listRecords(User user) throws IOException {
        return new ArrayList<>(db.keySet());
    }
}
