package repository;

import domain.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileUserRepository implements IUserRepository {

    private static final String DATA_FILE = "users.dat";

    @Override
    public Map<String, User> loadUsers() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("Файл '" + DATA_FILE + "' не найден. Начинаем с пустой базы.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                return (Map<String, User>) obj;
            } else {
                System.out.println("Некорректная структура файла: " + DATA_FILE);
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }
}
