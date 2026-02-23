package src.repositories;

import java.io.IOException;
import java.util.List;

import src.exceptions.PermissionDeniedException;
import src.models.MedicalRecord;
import src.models.User;

public interface IRecordRepo {
    MedicalRecord read(User user, String recordId) throws PermissionDeniedException, IOException;
    void write(User user, MedicalRecord record) throws PermissionDeniedException, IOException;
    void delete(User user, String recordId) throws PermissionDeniedException, IOException;
    List<String> listRecords(User user) throws IOException;
}
