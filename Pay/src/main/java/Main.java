import controller.MenuController;
import repository.FileUserRepository;
import service.FinanceService;

public class Main {
    public static void main(String[] args) {
        FileUserRepository userRepo = new FileUserRepository();
        FinanceService financeService = new FinanceService(userRepo);
        MenuController controller = new MenuController(financeService);

        controller.run();
    }
}
