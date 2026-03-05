package src.repositories;

import java.io.IOException;
import java.util.List;

import src.models.MedicalRecord;

public interface IRecordRepo {
    MedicalRecord read(String recordId) throws IOException;
    void write(MedicalRecord record) throws IOException;
    void delete(String recordId) throws IOException;
    List<String> list() throws IOException;
}
