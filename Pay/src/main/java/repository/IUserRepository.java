package repository;

import domain.User;
import java.util.Map;

public interface IUserRepository {
    Map<String, User> loadUsers();

    void saveUsers(Map<String, User> users);
}
