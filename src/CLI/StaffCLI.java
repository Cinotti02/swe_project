package CLI;

import Controller.StaffController;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.ReservationStatus;
import DomainModel.user.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> staffController.showKitchenQueue();
                case "2" -> handleOrderUpdate();
                case "3" -> handleSearchOrders();
                case "4" -> handleReservationList();
                case "5" -> handleReservationAction(Action.CONFIRM);
                case "6" -> handleReservationAction(Action.CHECK_IN);
                case "7" -> handleReservationAction(Action.NO_SHOW);
                case "8" -> handleSearchReservations();
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleOrderUpdate() {
        Integer orderId = readInt("ID ordine: ");
        if (orderId == null) return;

        OrderStatus targetStatus = readOrderStatus();
        if (targetStatus == null) return;

        staffController.updateOrderStatus(orderId, targetStatus);
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
        staffController.showReservations(date);
    }

    private void handleReservationAction(Action action) {
        Integer reservationId = readInt("ID prenotazione: ");
        if (reservationId == null) return;
        switch (action) {
            case CONFIRM -> staffController.confirmReservation(reservationId);
            case CHECK_IN -> staffController.registerCheckIn(reservationId);
            case NO_SHOW -> staffController.markNoShow(reservationId);
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

        staffController.searchOrders(customerId, status, paymentMethod, categoryId, startDate, endDate);
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

        staffController.searchReservations(exactDate, startDate, endDate, customerId, slotId, minGuests, maxGuests, status);
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

    private enum Action {
        CONFIRM, CHECK_IN, NO_SHOW
    }
}
