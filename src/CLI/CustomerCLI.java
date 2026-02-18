package CLI;

import Controller.CustomerController;
import Controller.CustomerProfileController;
import DomainModel.order.PaymentMethod;
import DomainModel.user.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CustomerCLI {

    private final CustomerController customerController;
    private final CustomerProfileController profileController;
    private final Scanner scanner;

    public CustomerCLI(CustomerController customerController,
                       CustomerProfileController profileController,
                       Scanner scanner) {
        this.customerController = customerController;
        this.profileController = profileController;
        this.scanner = scanner;
    }

    public void run(User user) {
        while (true) {
            System.out.println("=== Menu cliente ===");
            System.out.println("1) Visualizza menu");
            System.out.println("2) Aggiungi piatto al carrello");
            System.out.println("3) Mostra carrello");
            System.out.println("4) Rimuovi piatto dal carrello");
            System.out.println("5) Conferma ordine d'asporto");
            System.out.println("6) Prenota un tavolo");
            System.out.println("7) Le mie prenotazioni");
            System.out.println("8) Cancella prenotazione");
            System.out.println("9) Profilo");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> customerController.showMenu();
                case "2" -> handleAddDish(user);
                case "3" -> customerController.showCart(user);
                case "4" -> handleRemoveDish(user);
                case "5" -> handleCheckout(user);
                case "6" -> handleReservation(user);
                case "7" -> customerController.listReservations(user);
                case "8" -> handleCancelReservation(user);
                case "9" -> profileMenu(user);
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleAddDish(User user) {
        customerController.showMenu();
        System.out.print("\n--- Aggiungi piatto al carrello ---\n");
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        Integer qty = readInt("Quantità: ");
        if (qty == null) return;
        customerController.addDishToCart(user, dishId, qty);
    }

    private void handleRemoveDish(User user) {
        Integer dishId = readInt("ID piatto da rimuovere: ");
        if (dishId == null) return;
        customerController.removeItem(user, dishId);
    }

    private void handleCheckout(User user) {
        PaymentMethod method = readPaymentMethod();
        if (method == null) return;
        System.out.print("Note (facoltative): ");
        String notes = scanner.nextLine().trim();
        customerController.checkoutTakeAway(user, method, notes.isBlank() ? null : notes);
    }

    private void handleReservation(User user) {
        customerController.showAvailableSlots();
        LocalDate date = readDate("Data (YYYY-MM-DD): ");
        if (date == null) return;
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        Integer guests = readInt("Numero ospiti: ");
        if (guests == null) return;
        System.out.print("Note (facoltative): ");
        String notes = scanner.nextLine().trim();
        try {
            customerController.createReservation(user, date, slotId, guests, notes.isBlank() ? null : notes);
        }
        catch (IllegalStateException e) {
            System.out.println("⚠️  Mi dispiace, non ci sono tavoli disponibili per questo orario.\n");
        }
    }

    private void handleCancelReservation(User user) {
        Integer reservationId = readInt("ID prenotazione da cancellare: ");
        if (reservationId == null) return;
        customerController.cancelReservation(user, reservationId);
    }

    private void profileMenu(User user) {
        while (true) {
            System.out.println("--- Profilo ---");
            System.out.println("1) Visualizza profilo");
            System.out.println("2) Aggiorna dati personali");
            System.out.println("3) Cambia email");
            System.out.println("0) Indietro");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> profileController.showProfile(user.getId());
                case "2" -> handleProfileUpdate(user);
                case "3" -> handleEmailChange(user);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleProfileUpdate(User user) {
        System.out.print("Nuovo username (lascia vuoto per non cambiare): ");
        String username = scanner.nextLine().trim();
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();
        System.out.print("Cognome: ");
        String surname = scanner.nextLine().trim();
        profileController.updateProfile(user,
                username.isBlank() ? null : username,
                name.isBlank() ? null : name,
                surname.isBlank() ? null : surname);
    }

    private void handleEmailChange(User user) {
        System.out.print("Nuova email: ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) {
            System.out.println("Email obbligatoria");
            return;
        }
        profileController.changeEmail(user, email);
    }

    private Integer readInt(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            System.out.println("Valore obbligatorio\n");
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            System.out.println("Inserire un numero valido\n");
            return null;
        }
    }

    private LocalDate readDate(String data) {
        System.out.print(data);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            System.out.println("Data obbligatoria\n");
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            System.out.println("Formato data non valido\n");
            return null;
        }
    }

    private PaymentMethod readPaymentMethod() {
        System.out.println("Metodo di pagamento:");
        for (PaymentMethod method : PaymentMethod.values()) {
            System.out.println("- " + method.name());
        }
        System.out.print("Scelta: ");
        String raw = scanner.nextLine().trim().toUpperCase();
        if (raw.isBlank()) {
            System.out.println("Metodo obbligatorio\n");
            return null;
        }
        try {
            return PaymentMethod.valueOf(raw);
        } catch (IllegalArgumentException e) {
            System.out.println("Metodo non valido\n");
            return null;
        }
    }
}