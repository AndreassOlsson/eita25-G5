package src.repositories;

import java.io.IOException;

import src.models.MedicalRecord;
import src.models.PermissionDeniedException;
import src.models.User;

public interface IRecordRepo {
    MedicalRecord read(User user, String recordId) throws PermissionDeniedException, IOException;
    void write(User user, MedicalRecord record) throws PermissionDeniedException, IOException;
}
