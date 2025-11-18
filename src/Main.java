import ServiceLayer.AuthService;
import ORM.UserDAO;
import DomainModel.user.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();
        AuthService authService = new AuthService(userDAO);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=== DINEUP ===");
            System.out.println("1) Login");
            System.out.println("2) Register");
            System.out.println("0) Exit");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleLogin(scanner, authService);
                case "2" -> handleRegister(scanner, authService);
                case "0" -> {
                    System.out.println("Ciao!");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    // -------------------- LOGIN --------------------

    private static void handleLogin(Scanner scanner, AuthService authService) {
        try {
            System.out.println("\n--- Login ---");
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            Optional<User> result = authService.authenticate(email, password);

            if (result.isEmpty()) {
                System.out.println("Credenziali errate.\n");
                return;
            }

            User user = result.get();
            System.out.println("Login effettuato! Benvenuto, " + user.getUsername() + " (" + user.getRole() + ")\n");

            // Dopo il login, vai al menu del cliente/staff/owner
            userMainMenu(scanner, user);

        } catch (IllegalArgumentException e) {
            System.out.println("Errore di input: " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.\n");
        }
    }

    // -------------------- REGISTRAZIONE --------------------

    private static void handleRegister(Scanner scanner, AuthService authService) {
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

            User newUser = authService.registerClient(username, email, password, name, surname);

            System.out.println("Registrazione completata! ID utente: " + newUser.getId() + "\n");

        } catch (IllegalArgumentException e) {
            System.out.println("Errore: " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.\n");
        }
    }

    // -------------------- MENU UTENTE DOPO LOGIN --------------------

    private static void userMainMenu(Scanner scanner, User user) {
        while (true) {
            System.out.println("=== Menu utente (" + user.getRole() + ") ===");
            System.out.println("1) (TODO) Vedi menu ristorante");
            System.out.println("2) (TODO) Vedi carrello");
            System.out.println("3) (TODO) Effettua ordine d'asporto");
            System.out.println("4) (TODO) Prenota un tavolo");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> System.out.println("[TODO] Qui chiameremo DishDAO e mostreremo il menu.\n");
                case "2" -> System.out.println("[TODO] Qui useremo CartService per mostrare il carrello.\n");
                case "3" -> System.out.println("[TODO] Qui collegheremo CartService + OrderService.\n");
                case "4" -> System.out.println("[TODO] Qui useremo ReservationService + TableAllocationService.\n");
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }
}