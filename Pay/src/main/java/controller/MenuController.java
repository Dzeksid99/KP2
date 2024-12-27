package controller;

import service.FinanceService;

import java.util.Scanner;

public class MenuController {
    private FinanceService financeService;
    private Scanner scanner;

    public MenuController(FinanceService financeService) {
        this.financeService = financeService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Добро пожаловать в приложение для управления личными финансами!");

        boolean running = true;
        while (running) {
            printMainMenu(financeService.isLoggedIn());
            System.out.print("Выберите пункт меню: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": // register
                    doRegister();
                    break;
                case "2": // login
                    doLogin();
                    break;
                case "3": // logout
                    doLogout();
                    break;
                case "4": // addIncome
                    doAddIncome();
                    break;
                case "5": // addExpense
                    doAddExpense();
                    break;
                case "6": // setBudget
                    doSetBudget();
                    break;
                case "7": // summary
                    doSummary();
                    break;
                case "8": // summaryCats
                    doSummaryCats();
                    break;
                case "9": // transfer
                    doTransfer();
                    break;
                case "10": // export
                    doExport();
                    break;
                case "0": // exit
                    running = false;
                    financeService.exit();
                    System.out.println("Завершение работы приложения...");
                    break;
                default:
                    System.out.println("Неизвестный пункт меню. Повторите ввод.");
                    break;
            }
        }

        System.out.println("Приложение завершено.");
    }

    private void doRegister() {
        if (financeService.isLoggedIn()) {
            System.out.println("Вы уже авторизованы. Сначала выйдите, чтобы зарегистрировать нового пользователя.");
            return;
        }
        System.out.print("Введите логин: ");
        String username = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();
        System.out.println(financeService.register(username, password));
    }

    private void doLogin() {
        if (financeService.isLoggedIn()) {
            System.out.println("Сначала выйдите из учётной записи, прежде чем входить под другим пользователем.");
            return;
        }
        System.out.print("Введите логин: ");
        String username = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();
        System.out.println(financeService.login(username, password));
    }

    private void doLogout() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Вы не авторизованы.");
            return;
        }
        System.out.println(financeService.logout());
    }

    private void doAddIncome() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.print("Введите категорию дохода: ");
        String cat = scanner.nextLine().trim();
        System.out.print("Введите сумму дохода: ");
        double amt = safeReadDouble();
        System.out.println(financeService.addIncome(cat, amt));
    }

    private void doAddExpense() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.print("Введите категорию расхода: ");
        String cat = scanner.nextLine().trim();
        System.out.print("Введите сумму расхода: ");
        double amt = safeReadDouble();
        System.out.println(financeService.addExpense(cat, amt));
    }

    private void doSetBudget() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.print("Введите категорию: ");
        String cat = scanner.nextLine().trim();
        System.out.print("Введите бюджет: ");
        double budget = safeReadDouble();
        System.out.println(financeService.setBudget(cat, budget));
    }

    private void doSummary() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.println(financeService.showSummary());
    }

    private void doSummaryCats() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.print("Введите категории через запятую (например: food, salary): ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            System.out.println("Список категорий не может быть пуст.");
            return;
        }
        String[] cats = line.split(",");
        for (int i = 0; i < cats.length; i++) {
            cats[i] = cats[i].trim();
        }
        System.out.println(financeService.showSummaryForCategories(cats));
    }

    private void doTransfer() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.print("Введите логин пользователя-получателя: ");
        String toUser = scanner.nextLine().trim();
        System.out.print("Введите сумму перевода: ");
        double amt = safeReadDouble();
        System.out.println(financeService.transfer(toUser, amt));
    }

    private void doExport() {
        if (!financeService.isLoggedIn()) {
            System.out.println("Сначала авторизуйтесь.");
            return;
        }
        System.out.print("Введите имя файла (например, summary.txt): ");
        String filename = scanner.nextLine().trim();
        System.out.println(financeService.exportSummaryToFile(filename));
    }

    private void printMainMenu(boolean loggedIn) {
        System.out.println("\n--------- ГЛАВНОЕ МЕНЮ ---------");
        if (!loggedIn) {
            System.out.println("[1] Регистрация");
            System.out.println("[2] Войти");
        } else {
            System.out.println("[3] Выйти из учётной записи");
            System.out.println("[4] Добавить доход");
            System.out.println("[5] Добавить расход");
            System.out.println("[6] Установить бюджет для категории");
            System.out.println("[7] Общая сводка");
            System.out.println("[8] Сводка по выбранным категориям");
            System.out.println("[9] Перевод средств другому пользователю");
            System.out.println("[10] Экспорт сводки в файл");
        }
        System.out.println("[0] Выход из приложения");
    }

    private double safeReadDouble() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Неверный ввод. Введите число: ");
            }
        }
    }
}
