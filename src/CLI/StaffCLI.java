package CLI;

import Controller.StaffController;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.user.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StaffCLI {

    private final StaffController staffController;
    private final Scanner scanner;

    public StaffCLI(StaffController staffController, Scanner scanner) {
        this.staffController = staffController;
        this.scanner = scanner;
    }

    public void run(User user) {
        while (true) {
            System.out.println("\n=================================================================");
            System.out.println("Benvenuto, " + user.getName() + "!");
            System.out.println("=== Staff ===");
            System.out.println("");
            System.out.println("== Asporto ==");
            System.out.println("1) Vedi coda cucina");
            System.out.println("2) Aggiorna stato ordine");
            System.out.println("3) Cerca ordini");
            System.out.println("");
            System.out.println("== Prenotazioni ==");
            System.out.println("4) Prenotazioni per data");
            System.out.println("5) Conferma prenotazione");
            System.out.println("6) Check-in prenotazione");
            System.out.println("7) Segna no-show");
            System.out.println("8) Cerca prenotazioni");
            System.out.println("9) Mostra notifiche");
            System.out.println("10) Mostra notifiche non lette");
            System.out.println("11) Segna notifica come letta");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showKitchenQueue();
                case "2" -> handleOrderUpdate(user);
                case "3" -> handleSearchOrders();
                case "4" -> handleReservationList();
                case "5" -> handleReservationAction(Action.CONFIRM);
                case "6" -> handleReservationAction(Action.CHECK_IN);
                case "7" -> handleReservationAction(Action.NO_SHOW);
                case "8" -> handleSearchReservations();
                case "9" -> handleShowNotifications(user, false);
                case "10" -> handleShowNotifications(user, true);
                case "11" -> handleMarkNotificationAsRead();
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleOrderUpdate(User user) {
        Integer orderId = readInt("ID ordine: ");
        if (orderId == null) return;

        OrderStatus targetStatus = readOrderStatus();
        if (targetStatus == null) return;

        try {
            staffController.updateOrderStatus(orderId, targetStatus, user.getId());
            System.out.println("Stato ordine aggiornato");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel cambio stato ordine: " + e.getMessage());
        }
    }

    private OrderStatus readOrderStatus() {
        System.out.println("Stati disponibili:");
        System.out.println("1) PREPARING");
        System.out.println("2) READY");
        System.out.println("3) RETIRED");
        System.out.println("4) CANCELLED");
        System.out.print("Nuovo stato: ");

        String choice = scanner.nextLine().trim();
        return switch (choice) {
            case "1" -> OrderStatus.PREPARING;
            case "2" -> OrderStatus.READY;
            case "3" -> OrderStatus.RETIRED;
            case "4" -> OrderStatus.CANCELLED;
            default -> {
                System.out.println("Scelta stato non valida\n");
                yield null;
            }
        };
    }

    private void handleReservationList() {
        LocalDate date = readDate("Data (YYYY-MM-DD): ");
        if (date == null) return;
        try {
            printReservationsForDate(date, staffController.getReservations(date));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare le prenotazioni: " + e.getMessage());
        }
    }

    private void handleReservationAction(Action action) {
        Integer reservationId = readInt("ID prenotazione: ");
        if (reservationId == null) return;
        try {
            switch (action) {
                case CONFIRM -> staffController.confirmReservation(reservationId);
                case CHECK_IN -> staffController.registerCheckIn(reservationId);
                case NO_SHOW -> staffController.markNoShow(reservationId);
            }
            System.out.println("Prenotazione aggiornata");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore aggiornamento prenotazione: " + e.getMessage());
        }
    }

    private void handleSearchOrders() {
        System.out.println("=== Ricerca ordini ===");
        Integer customerId = readOptionalInt("Customer ID (invio per tutti): ");
        OrderStatus status = readOptionalOrderStatus();
        PaymentMethod paymentMethod = readOptionalPaymentMethod();
        Integer categoryId = readOptionalInt("Categoria piatto (ID, invio per tutte): ");
        LocalDate startDate = readOptionalDate("Data inizio creazione YYYY-MM-DD (invio per saltare): ");
        LocalDate endDate = readOptionalDate("Data fine creazione YYYY-MM-DD (invio per saltare): ");

        try {
            printOrders(staffController.searchOrders(
                    customerId, status, paymentMethod, categoryId, startDate, endDate));
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca ordini: " + e.getMessage());
        }
    }

    private void handleSearchReservations() {
        System.out.println("=== Ricerca prenotazioni ===");
        LocalDate exactDate = readOptionalDate("Data precisa YYYY-MM-DD (invio per saltare): ");
        LocalDate startDate = readOptionalDate("Data inizio YYYY-MM-DD (invio per saltare): ");
        LocalDate endDate = readOptionalDate("Data fine YYYY-MM-DD (invio per saltare): ");
        Integer customerId = readOptionalInt("Customer ID (invio per tutti): ");
        Integer slotId = readOptionalInt("Slot ID (invio per tutti): ");
        Integer minGuests = readOptionalInt("Ospiti minimi (invio per saltare): ");
        Integer maxGuests = readOptionalInt("Ospiti massimi (invio per saltare): ");
        ReservationStatus status = readOptionalReservationStatus();

        try {
            printSearchReservations(staffController.searchReservations(
                    exactDate, startDate, endDate, customerId, slotId, minGuests, maxGuests, status));
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca prenotazioni: " + e.getMessage());
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

    private Integer readOptionalInt(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) return null;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            System.out.println("Numero non valido, filtro ignorato.");
            return null;
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
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

    private LocalDate readOptionalDate(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            System.out.println("Data non valida, filtro ignorato.");
            return null;
        }
    }

    private OrderStatus readOptionalOrderStatus() {
        System.out.print("Stato ordine [CREATED,PREPARING,READY,RETIRED,CANCELLED] (invio per tutti): ");
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) return null;
        try {
            return OrderStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Stato non valido, filtro ignorato.");
            return null;
        }
    }

    private PaymentMethod readOptionalPaymentMethod() {
        System.out.print("Metodo pagamento [ONLINE,IN_LOCO] (invio per tutti): ");
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) return null;
        try {
            return PaymentMethod.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Metodo non valido, filtro ignorato.");
            return null;
        }
    }

    private ReservationStatus readOptionalReservationStatus() {
        System.out.print("Stato prenotazione [CREATED,CONFIRMED,CHECKED_IN,COMPLETED,NO_SHOW,CANCELED] (invio per tutti): ");
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) return null;
        try {
            return ReservationStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Stato non valido, filtro ignorato.");
            return null;
        }
    }


    private void handleShowNotifications(User user, boolean unreadOnly) {
        try {
            var notifications = staffController.getNotifications(user.getId(), unreadOnly);
            System.out.println(unreadOnly ? "=== Notifiche staff non lette ===" : "=== Notifiche staff ===");
            if (notifications.isEmpty()) {
                System.out.println("(nessuna notifica)");
                return;
            }
            notifications.forEach(n -> System.out.println("#" + n.getId() + " | " + n.getType() + " | " + n.getStatus() + " | " + n.getMessage()));
        } catch (Exception e) {
            System.err.println("Errore caricamento notifiche: " + e.getMessage());
        }
    }

    private void handleMarkNotificationAsRead() {
        Integer notificationId = readInt("ID notifica: ");
        if (notificationId == null) return;
        try { staffController.markNotificationAsRead(notificationId); System.out.println("Notifica segnata come letta"); } catch (Exception e) { System.err.println("Errore aggiornamento notifica: " + e.getMessage()); }
    }

    private void showKitchenQueue() {
        try {
            Map<OrderStatus, List<Order>> queue = staffController.getKitchenQueue();
            System.out.println("\n=== CODA CUCINA ===");
            System.out.println("\n== ORDINI ATTIVI ==");
            printOrderBucket("CREATI (DA PREPARARE)", queue.get(OrderStatus.CREATED));
            printOrderBucket("IN PREPARAZIONE", queue.get(OrderStatus.PREPARING));
            printOrderBucket("PRONTI", queue.get(OrderStatus.READY));
            System.out.println("\n== ORDINI CONCLUSI ==");
            printOrderBucket("RITIRATI", queue.get(OrderStatus.RETIRED));
            printOrderBucket("ANNULLATI", queue.get(OrderStatus.CANCELLED));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare la coda cucina: " + e.getMessage());
        }
    }

    private void printOrderBucket(String title, List<Order> orders) {
        System.out.println("\n--- " + title + " ---");
        if (orders == null || orders.isEmpty()) {
            System.out.println("(nessun ordine)");
            return;
        }
        System.out.printf("%-8s %-12s %-10s %-12s %-20s%n",
                "ID", "Cliente", "Pagamento", "Totale", "Creato il");
        for (Order order : orders) {
            String payment = order.getPaymentMethod() != null
                    ? order.getPaymentMethod().name() : "-";
            String total = order.getTotalAmount() != null
                    ? order.getTotalAmount().toString() : "-";
            String createdAt = order.getCreatedAt() != null
                    ? order.getCreatedAt().toString() : "-";
            int customerId = order.getCustomer() != null ? order.getCustomer().getId() : -1;
            System.out.printf("#%-7d %-12d %-10s %-12s %-20s%n",
                    order.getId(), customerId, payment, total, createdAt);
        }
    }

    private void printReservationsForDate(LocalDate date, List<Reservation> reservations) {
        System.out.println("=== Prenotazioni per " + date + " ===");
        reservations.forEach(reservation -> System.out.println(
                "#" + reservation.getId()
                        + " ospiti:" + reservation.getNumberOfGuests()
                        + " slot:" + reservation.getTimeSlot().getStartTime()
                        + " stato:" + reservation.getStatus()));
    }

    private void printOrders(List<Order> orders) {
        System.out.println("=== RISULTATI ORDINI ===");
        if (orders.isEmpty()) {
            System.out.println("(nessun ordine trovato)");
            return;
        }
        orders.forEach(order -> {
            int customerId = order.getCustomer() != null ? order.getCustomer().getId() : -1;
            System.out.println("#" + order.getId()
                    + " | customer:" + customerId
                    + " | stato:" + order.getStatus()
                    + " | payment:" + order.getPaymentMethod()
                    + " | totale:" + order.getTotalAmount()
                    + " | creato:" + order.getCreatedAt());
        });
    }

    private void printSearchReservations(List<Reservation> reservations) {
        System.out.println("=== RISULTATI PRENOTAZIONI ===");
        if (reservations.isEmpty()) {
            System.out.println("(nessuna prenotazione trovata)");
            return;
        }
        reservations.forEach(reservation -> {
            int customerId = reservation.getCustomer() != null
                    ? reservation.getCustomer().getId() : -1;
            Integer slotId = reservation.getTimeSlot() != null
                    ? reservation.getTimeSlot().getId() : null;
            System.out.println("#" + reservation.getId()
                    + " | customer:" + customerId
                    + " | data:" + reservation.getReservDate().toLocalDate()
                    + " | slot:" + slotId
                    + " | guests:" + reservation.getNumberOfGuests()
                    + " | stato:" + reservation.getStatus());
        });
    }


    private enum Action {
        CONFIRM, CHECK_IN, NO_SHOW
    }
}
