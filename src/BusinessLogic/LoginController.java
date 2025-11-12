package BusinessLogic;

import DomainModel.User;
import ORM.UserDAO;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginController {

    public User login(String email, String password) {
        UserDAO userDAO = new UserDAO();
        try {
            if (email == null || email.isBlank() || email.trim().isBlank())
                throw new IllegalArgumentException("Email cannot be empty");
            if (password == null || password.isBlank())
                throw new IllegalArgumentException("Password cannot be empty");

            User match = userDAO.getUserByEmail(email);
            if (match == null)
                throw new IllegalArgumentException("User not found");
            if (!BCrypt.checkpw(password, match.getPassword()))
                throw new IllegalArgumentException("Invalid credentials");
            return match;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

    }
}
