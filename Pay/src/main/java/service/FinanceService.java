package service;

import domain.Transaction;
import domain.TransactionType;
import domain.User;
import repository.IUserRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class FinanceService {
    private IUserRepository userRepository;
    private Map<String, User> users;
    private User currentUser;

    public FinanceService(IUserRepository userRepository) {
        this.userRepository = userRepository;
        this.users = new HashMap<>();

        loadUsersFromRepository();
    }

    public String register(String username, String password) {
        if (users.containsKey(username)) {
            return "Пользователь с логином '" + username + "' уже существует.";
        }
        User user = new User(username, password);
        users.put(username, user);
        saveUsersToRepository();
        return "Регистрация успешна. Можете авторизоваться.";
    }

    public String login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            return "Пользователь '" + username + "' не найден.";
        }
        if (!user.getPassword().equals(password)) {
            return "Неверный пароль.";
        }
        currentUser = user;
        return "Авторизация прошла успешно.";
    }

    public String logout() {
        currentUser = null;
        return "Вы вышли из учётной записи.";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String addIncome(String category, double amount) {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        if (amount <= 0) {
            return "Сумма дохода должна быть > 0.";
        }
        Transaction t = new Transaction(TransactionType.INCOME, category, amount);
        currentUser.getWallet().addTransaction(t);

        String warn = checkExpenseExceedIncome();
        saveUsersToRepository();
        return "Доход добавлен." + (warn.isEmpty() ? "" : "\n" + warn);
    }

    public String addExpense(String category, double amount) {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        if (amount <= 0) {
            return "Сумма расхода должна быть > 0.";
        }
        Transaction t = new Transaction(TransactionType.EXPENSE, category, amount);
        currentUser.getWallet().addTransaction(t);

        double budget = currentUser.getWallet().getCategoryBudget(category);
        double spent = currentUser.getWallet().getCategoryExpense(category);
        StringBuilder sb = new StringBuilder("Расход добавлен.");
        if (budget > 0 && spent > budget) {
            sb.append(" Внимание! Превышен лимит бюджета для категории '").append(category).append("'.");
        }

        String warn = checkExpenseExceedIncome();
        if (!warn.isEmpty()) {
            sb.append("\n").append(warn);
        }
        saveUsersToRepository();
        return sb.toString();
    }

    public String setBudget(String category, double amount) {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        if (amount < 0) {
            return "Сумма бюджета не может быть отрицательной.";
        }
        currentUser.getWallet().setCategoryBudget(category, amount);
        saveUsersToRepository();
        return "Бюджет для категории '" + category + "' установлен: " + amount;
    }

    public String showSummary() {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        double totalInc = currentUser.getWallet().getTotalIncome();
        double totalExp = currentUser.getWallet().getTotalExpense();
        StringBuilder sb = new StringBuilder("=== Общая сводка ===\n");
        sb.append("Общий доход: ").append(totalInc).append("\n");
        sb.append("Общие расходы: ").append(totalExp).append("\n\n");

        sb.append("Бюджет по категориям:\n");
        for (Map.Entry<String, Double> entry : currentUser.getWallet().getAllCategoryBudgets().entrySet()) {
            String cat = entry.getKey();
            double bud = entry.getValue();
            double spent = currentUser.getWallet().getCategoryExpense(cat);
            double remaining = bud - spent;
            sb.append(cat)
                    .append(": Бюджет=").append(bud)
                    .append(", Израсходовано=").append(spent)
                    .append(", Остаток=").append(remaining)
                    .append("\n");
        }
        return sb.toString();
    }

    public String showSummaryForCategories(String[] categories) {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        StringBuilder sb = new StringBuilder("=== Сводка по выбранным категориям ===\n");
        double totalIncAll = 0.0;
        double totalExpAll = 0.0;
        for (String cat : categories) {
            double inc = currentUser.getWallet().getCategoryIncome(cat);
            double exp = currentUser.getWallet().getCategoryExpense(cat);
            totalIncAll += inc;
            totalExpAll += exp;

            sb.append("Категория: ").append(cat)
                    .append(" | Доход: ").append(inc)
                    .append(" | Расход: ").append(exp)
                    .append("\n");
        }
        sb.append("\nСумма доходов (выбранные категории): ").append(totalIncAll).append("\n");
        sb.append("Сумма расходов (выбранные категории): ").append(totalExpAll).append("\n");
        return sb.toString();
    }

    public String transfer(String toUser, double amount) {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        if (amount <= 0) {
            return "Сумма перевода должна быть > 0.";
        }
        User receiver = users.get(toUser);
        if (receiver == null) {
            return "Пользователь '" + toUser + "' не найден.";
        }
        Transaction exp = new Transaction(TransactionType.EXPENSE,
                "Перевод пользователю: " + toUser, amount);
        currentUser.getWallet().addTransaction(exp);

        Transaction inc = new Transaction(TransactionType.INCOME,
                "Перевод от: " + currentUser.getUsername(), amount);
        receiver.getWallet().addTransaction(inc);

        String warn = checkExpenseExceedIncome();
        saveUsersToRepository();
        return "Перевод выполнен. " + (warn.isEmpty() ? "" : warn);
    }

    public String exportSummaryToFile(String filename) {
        if (!isLoggedIn()) {
            return "Ошибка: Вы не авторизованы.";
        }
        String summary = showSummary();
        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println(summary);
            return "Сводка успешно экспортирована в файл: " + filename;
        } catch (FileNotFoundException e) {
            return "Ошибка при записи в файл: " + e.getMessage();
        }
    }

    public void exit() {
        saveUsersToRepository();
    }

    private String checkExpenseExceedIncome() {
        if (!isLoggedIn()) {
            return "";
        }
        double inc = currentUser.getWallet().getTotalIncome();
        double exp = currentUser.getWallet().getTotalExpense();
        if (exp > inc) {
            return "Внимание! Общие расходы (" + exp + ") превысили общий доход (" + inc + ").";
        }
        return "";
    }

    private void loadUsersFromRepository() {
        this.users = userRepository.loadUsers();
        if (this.users == null) {
            this.users = new HashMap<>();
        }
    }

    private void saveUsersToRepository() {
        userRepository.saveUsers(this.users);
    }
}
