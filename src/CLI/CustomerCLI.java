package CLI;

import Controller.CustomerController;
import Controller.CustomerProfileController;
import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.notification.Notification;
import DomainModel.order.Order;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.Slot;
import DomainModel.user.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
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
            System.out.println("2) Cerca piatto");
            System.out.println("3) Aggiungi piatto al carrello");
            System.out.println("4) Mostra carrello");
            System.out.println("5) Rimuovi piatto dal carrello");
            System.out.println("6) Conferma ordine d'asporto");
            System.out.println("7) Prenota un tavolo");
            System.out.println("8) Le mie prenotazioni");
            System.out.println("9) Cancella prenotazione");
            System.out.println("10) Profilo");
            System.out.println("11) Mostra notifiche");
            System.out.println("12) Mostra notifiche non lette");
            System.out.println("13) Segna notifica come letta");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showMenu();
                case "2" -> handleSearchDish();
                case "3" -> handleAddDish(user);
                case "4" -> System.out.println(customerController.getCartSummary(user));
                case "5" -> handleRemoveDish(user);
                case "6" -> handleCheckout(user);
                case "7" -> handleReservation(user);
                case "8" -> handleListReservations(user);
                case "9" -> handleCancelReservation(user);
                case "10" -> profileMenu(user);
                case "11" -> handleShowNotifications(user, false);
                case "12" -> handleShowNotifications(user, true);
                case "13" -> handleMarkNotificationAsRead();
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleAddDish(User user) {
        showMenu();
        System.out.print("\n--- Aggiungi piatto al carrello ---\n");
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        Integer qty = readInt("Quantità: ");
        if (qty == null) return;
        try {
            customerController.addDishToCart(user, dishId, qty);
            System.out.println("Piatto aggiunto al carrello");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    private void handleSearchDish() {
        System.out.print("Nome piatto (o parte del nome): ");
        String query = scanner.nextLine().trim();
        try {
            printDishes(customerController.searchDishes(query.isBlank() ? null : query));
        } catch (SQLException e) {
            System.err.println("Impossibile cercare i piatti: " + e.getMessage());
        }
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
        try {
            Order order = customerController.checkoutTakeAway(user, method, notes.isBlank() ? null : notes);
            System.out.println("Ordine creato con ID " + order.getId());
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            System.err.println("Errore durante la creazione dell'ordine: " + e.getMessage());
        }
    }

    private void handleReservation(User user) {
        showAvailableSlots();
        LocalDate date = readDate("Data (YYYY-MM-DD): ");
        if (date == null) return;
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        Integer guests = readInt("Numero ospiti: ");
        if (guests == null) return;
        System.out.print("Note (facoltative): ");
        String notes = scanner.nextLine().trim();
        try {
            Reservation reservation = customerController.createReservation(
                    user, date, slotId, guests, notes.isBlank() ? null : notes);
            System.out.println("Prenotazione creata con ID " + reservation.getId());
        }
        catch (IllegalStateException e) {
            System.out.println("Mi dispiace, non ci sono tavoli disponibili per questo orario.\n");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare la prenotazione: " + e.getMessage());
        }
    }

    private void handleCancelReservation(User user) {
        Integer reservationId = readInt("ID prenotazione da cancellare: ");
        if (reservationId == null) return;
        try {
            customerController.cancelReservation(user, reservationId);
            System.out.println("Prenotazione annullata");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore durante l'annullamento: " + e.getMessage());
        }
    }


    private void handleShowNotifications(User user, boolean unreadOnly) {
        try {
            var notifications = customerController.getNotifications(user, unreadOnly);
            System.out.println(unreadOnly ? "=== Notifiche non lette ===" : "=== Tutte le notifiche ===");
            if (notifications.isEmpty()) {
                System.out.println("(nessuna notifica)");
                return;
            }
            notifications.forEach(n -> System.out.println("#" + n.getId() + " | " + n.getType() + " | " + n.getStatus() + " | " + n.getMessage()));
        } catch (Exception e) {
            System.err.println("Impossibile caricare notifiche: " + e.getMessage());
        }
    }

    private void handleMarkNotificationAsRead() {
        Integer notificationId = readInt("ID notifica: ");
        if (notificationId == null) return;
        try {
            customerController.markNotificationAsRead(notificationId);
            System.out.println("Notifica segnata come letta");
        } catch (Exception e) {
            System.err.println("Errore aggiornamento notifica: " + e.getMessage());
        }
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
                case "1" -> handleShowProfile(user.getId());
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
        try {
            profileController.updateProfile(user,
                    username.isBlank() ? null : username,
                    name.isBlank() ? null : name,
                    surname.isBlank() ? null : surname);
            System.out.println("Profilo aggiornato");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore durante l'aggiornamento: " + e.getMessage());
        }
    }

    private void handleEmailChange(User user) {
        System.out.print("Nuova email: ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) {
            System.out.println("Email obbligatoria");
            return;
        }
        try {
            profileController.changeEmail(user, email);
            System.out.println("Email aggiornata");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile aggiornare l'email: " + e.getMessage());
        }
    }

    private void showMenu() {
        try {
            Map<Category, List<Dish>> menu = customerController.getMenu();
            System.out.println("=== MENU ===");
            menu.forEach((category, dishes) -> {
                System.out.println("-- " + category.getName() + " --");
                if (dishes.isEmpty()) System.out.println("(Nessun piatto disponibile)");
                printDishRows(dishes);
            });
        } catch (SQLException e) {
            System.err.println("Impossibile caricare il menu: " + e.getMessage());
        }
    }

    private void printDishes(List<Dish> dishes) {
        if (dishes.isEmpty()) {
            System.out.println("Nessun piatto trovato");
            return;
        }
        System.out.println("=== Risultati ricerca ===");
        printDishRows(dishes);
    }

    private void printDishRows(List<Dish> dishes) {
        for (Dish dish : dishes) {
            System.out.println("#" + dish.getId() + " - " + dish.getName() + " (" + dish.getPrice() + ")");
            if (dish.getDescription() != null) System.out.println("   " + dish.getDescription());
        }
    }

    private void handleListReservations(User user) {
        try {
            List<Reservation> reservations = customerController.listReservations(user);
            if (reservations.isEmpty()) {
                System.out.println("Non hai prenotazioni attive");
                return;
            }
            System.out.println("=== Le tue prenotazioni ===");
            reservations.forEach(reservation -> System.out.println("#" + reservation.getId() + " - "
                    + reservation.getReservDate() + " - Stato: " + reservation.getStatus()));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare le prenotazioni: " + e.getMessage());
        }
    }

    private void showAvailableSlots() {
        try {
            List<Slot> slots = customerController.getAvailableSlots();
            if (slots.isEmpty()) {
                System.out.println("Nessuno slot disponibile al momento");
                return;
            }
            System.out.println("=== SLOT DISPONIBILI ===");
            slots.forEach(slot -> System.out.println(
                    "#" + slot.getId() + " " + slot.getStartTime() + "-" + slot.getEndTime()));
        } catch (SQLException e) {
            System.err.println("Impossibile recuperare gli slot: " + e.getMessage());
        }
    }

    private void handleShowProfile(int userId) {
        try {
            User user = profileController.getProfile(userId);
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
