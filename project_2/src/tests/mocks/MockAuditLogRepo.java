package src.tests.mocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import src.models.User;
import src.repositories.IAuditLogRepo;

public class MockAuditLogRepo implements IAuditLogRepo {
    public List<String> logs = new ArrayList<>();

    @Override
    public void log(User user, String action, String recordId, String details) throws IOException {
        logs.add(user.getUsername() + ":" + action + ":" + recordId);
    }
}
