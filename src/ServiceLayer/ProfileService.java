package ServiceLayer;

import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ORM.UserDAO;

import java.sql.SQLException;

public class ProfileService {

    private final UserDAO userDAO;

    public ProfileService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User getProfile(int userId) throws SQLException {
        return userDAO.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public void updatePersonalInfo(User user, String username, String name, String surname) throws SQLException {
        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }
        user.setName(name);
        user.setSurname(surname);
        userDAO.updateUser(user);
    }

    public void updateEmail(User user, String email) throws SQLException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        user.setEmail(new Email(email));
        userDAO.updateUser(user);
    }

    public void addFidelityPoints(User user, int delta) throws SQLException {
        int updated = Math.max(0, user.getFidelityPoints() + delta);
        user.setFidelityPoints(updated);
        userDAO.updateUser(user);
    }
}