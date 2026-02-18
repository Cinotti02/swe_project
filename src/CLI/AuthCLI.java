package CLI;

import Controller.AuthController;
import DomainModel.user.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

public class AuthCLI {

    private final AuthController authController;
    private final Scanner scanner;

    public AuthCLI(AuthController authController, Scanner scanner) {
        this.authController = authController;
        this.scanner = scanner;
    }

    public Optional<User> handleLogin() {
        try {
            System.out.println("\n--- Login ---");
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            Optional<User> result = authController.login(email, password);
            if (result.isEmpty()) {
                System.out.println("Credenziali errate.\n");
                return Optional.empty();
            }

            User user = result.get();
            System.out.println("Login effettuato! Benvenuto, " + user.getUsername() + " (" + user.getRole() + ")\n");
            return Optional.of(user);

        } catch (IllegalArgumentException e) {
            System.out.println("Errore di input: " + e.getMessage() + "\n");
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.\n");
            return Optional.empty();
        }
    }

    public Optional<User> handleRegister() {
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
            return Optional.of(newUser);

        } catch (IllegalArgumentException e) {
            System.out.println("Errore: " + e.getMessage() + "\n");
            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Errore DB: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.\n");
            return Optional.empty();
        }
    }
}