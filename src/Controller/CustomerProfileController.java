package Controller;

import DomainModel.user.User;
import ServiceLayer.ProfileService;

import java.sql.SQLException;

public class CustomerProfileController {

    private final ProfileService profileService;

    public CustomerProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    public void showProfile(int userId) {
        try {
            User user = profileService.getProfile(userId);
            System.out.println("=== Profilo ===");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmailValue());
            System.out.println("Nome: " + user.getName());
            System.out.println("Cognome: " + user.getSurname());
            System.out.println("Fidelity points: " + user.getFidelityPoints());
        } catch (SQLException e) {
            System.err.println("Impossibile caricare il profilo: " + e.getMessage());
        }
    }

    public void updateProfile(User user, String username, String name, String surname) {
        try {
            profileService.updatePersonalInfo(user, username, name, surname);
            System.out.println("Profilo aggiornato");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore durante l'aggiornamento: " + e.getMessage());
        }
    }

    public void changeEmail(User user, String email) {
        try {
            profileService.updateEmail(user, email);
            System.out.println("Email aggiornata");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile aggiornare l'email: " + e.getMessage());
        }
    }

    public void addFidelityPoints(User user, int points) {
        try {
            profileService.addFidelityPoints(user, points);
        } catch (SQLException e) {
            System.err.println("Impossibile aggiornare i punti: " + e.getMessage());
        }
    }
}