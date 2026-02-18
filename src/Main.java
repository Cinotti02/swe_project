import CLI.CustomerCLI;
import CLI.OwnerCLI;
import CLI.StaffCLI;
import  Controller.AuthController;
import Controller.CustomerController;
import Controller.CustomerProfileController;
import Controller.OwnerController;
import Controller.StaffController;
import DomainModel.user.User;
import ORM.*;
import ServiceLayer.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private final Scanner scanner;
    private final AuthController authController;
    private final CustomerCLI customerCLI;
    private final OwnerCLI ownerCLI;
    private final StaffCLI staffCLI;

    private Main() {
        this.scanner = new Scanner(System.in);

        UserDAO userDAO = new UserDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        DishDAO dishDAO = new DishDAO();
        TableDAO tableDAO = new TableDAO();
        SlotDAO slotDAO = new SlotDAO();
        ReservationDAO reservationDAO = new ReservationDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        OrderDAO orderDAO = new OrderDAO();
        OrderItemDAO orderItemDAO = new OrderItemDAO();

        AuthService authService = new AuthService(userDAO);
        this.authController = new AuthController(authService);
        MenuQueryService menuQueryService = new MenuQueryService(dishDAO, categoryDAO);
        CartService cartService = new CartService();
        OrderService orderService = new OrderService(orderDAO, orderItemDAO);
        TableAllocationService tableAllocationService = new TableAllocationService();
        ReservationService reservationService = new ReservationService(
                reservationDAO,
                tableDAO,
                slotDAO,
                notificationDAO,
                tableAllocationService);
        OwnerAdminService ownerAdminService = new OwnerAdminService(dishDAO, categoryDAO, tableDAO, slotDAO);
        ProfileService profileService = new ProfileService(userDAO);
        StaffOperationService staffOperationService = new StaffOperationService(orderDAO, reservationService, notificationDAO);

        CustomerController customerController = new CustomerController(
                menuQueryService,
                cartService,
                orderService,
                reservationService);
        CustomerProfileController customerProfileController = new CustomerProfileController(profileService);
        OwnerController ownerController = new OwnerController(ownerAdminService, menuQueryService);
        StaffController staffController = new StaffController(staffOperationService);

        this.customerCLI = new CustomerCLI(customerController, customerProfileController, scanner);
        this.ownerCLI = new OwnerCLI(ownerController, scanner);
        this.staffCLI = new StaffCLI(staffController, scanner);

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
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "0" -> {
                    System.out.println("Ciao!");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleLogin() {
        try {
            System.out.println("\n--- Login ---");
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            Optional<User> result = authController.login(email, password);

            if (result.isEmpty()) {
                System.out.println("Credenziali errate.\n");
                return;
            }

            User user = result.get();
            System.out.println("Login effettuato! Benvenuto, " + user.getUsername() + " (" + user.getRole() + ")\n");
            dispatchUser(user);

        } catch (IllegalArgumentException e) {
            System.out.println("Errore di input: " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.\n");
        }
    }

    private void handleRegister() {
        try {
            System.out.println("\n--- Registrazione nuovo cliente ---");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Nome: ");
            String name = scanner.nextLine().trim();

            System.out.print("Cognome: ");
            String surname = scanner.nextLine().trim();

            User newUser = authController.registerCustomer(username, email, password, name, surname);

            System.out.println("Registrazione completata! ID utente: " + newUser.getId() + "\n");

        } catch (IllegalArgumentException e) {
            System.out.println("Errore: " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.\n");
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