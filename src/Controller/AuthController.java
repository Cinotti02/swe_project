package Controller;

import DomainModel.user.User;
import ServiceLayer.AuthService;

import java.sql.SQLException;
import java.util.Optional;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public Optional<User> login(String email, String password) throws SQLException {
        return authService.authenticate(email, password);
    }

    public User registerCustomer(String username,
                                 String email,
                                 String password,
                                 String name,
                                 String surname) throws SQLException {
        return authService.registerClient(username, email, password, name, surname);
    }

    public void resetForgottenPassword(String email, String newPassword) throws SQLException {
        authService.resetPasswordByEmail(email, newPassword);
    }
}