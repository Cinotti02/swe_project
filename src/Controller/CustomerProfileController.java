package Controller;

import DomainModel.user.User;
import ServiceLayer.ProfileService;

import java.sql.SQLException;

public class CustomerProfileController {

    private final ProfileService profileService;

    public CustomerProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    public User getProfile(int userId) throws SQLException {
        return profileService.getProfile(userId);
    }

    public void updateProfile(User user,
                              String username,
                              String name,
                              String surname) throws SQLException {
        profileService.updatePersonalInfo(user, username, name, surname);
    }

    public void changeEmail(User user, String email) throws SQLException {
        profileService.updateEmail(user, email);
    }

    public void addFidelityPoints(User user, int points) throws SQLException {
        profileService.addFidelityPoints(user, points);
    }
}
