import CLI.AuthCLI;
import CLI.CustomerCLI;
import CLI.OwnerCLI;
import CLI.StaffCLI;
import DomainModel.user.User;

import java.util.Scanner;

public class Main {

    private final Scanner scanner;
    private final AuthCLI authCLI;
    private final CustomerCLI customerCLI;
    private final OwnerCLI ownerCLI;
    private final StaffCLI staffCLI;

    private Main() {
        this.scanner = new Scanner(System.in);

        AppBootstrap.Components components = AppBootstrap.build(scanner);
        this.authCLI = components.authCLI();
        this.customerCLI = components.customerCLI();
        this.ownerCLI = components.ownerCLI();
        this.staffCLI = components.staffCLI();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    private void start() {
        while (true) {
            System.out.println("=== DINEUP ===");
            System.out.println("1) Login");
            System.out.println("2) Register");
            System.out.println("0) Exit");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> authCLI.handleLogin().ifPresent(this::dispatchUser);
                case "2" -> authCLI.handleRegister();
                case "0" -> {
                    System.out.println("Ciao!");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void dispatchUser(User user) {
        if (user.isOwner()) {
            ownerCLI.run(user);
        } else if (user.isStaff()) {
            staffCLI.run(user);
        } else {
            customerCLI.run(user);
        }
    }

}