package Application;

import BusinessLogic.AuthService;
import DomainModel.User;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public void handleLogin(String email, String password) {
        try {
            Optional<User> result = authService.authenticate(email, password);
            if (result.isPresent()) {
                User user = result.get();

                switch (user.getRole()) {
                    case CUSTOMER -> System.out.println("Benvenuto cliente!");
                    case STAFF -> System.out.println("Benvenuto staff!");
                    case OWNER -> System.out.println("Benvenuto owner!");
                }
            }
            else {
                System.out.println("Credenziali errate. Riprova.");
            }
        } catch (IllegalArgumentException e) {
            // input non valido → messaggio chiaro
            System.out.println("Errore: " + e.getMessage());


        } catch (SQLException e) {
            // errore DB → messaggio per l'utente + log interno
            System.err.println("Database error: " + e.getMessage());
            System.out.println("Si è verificato un errore interno. Riprova più tardi.");
        }
    }
}

