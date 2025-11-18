package CLI;

import Controller.StaffController;
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
            System.out.println("=== Menu staff ===");
            System.out.println("1) Vedi coda cucina");
            System.out.println("2) Segna ordine pronto");
            System.out.println("3) Segna ordine ritirato");
            System.out.println("4) Prenotazioni per data");
            System.out.println("5) Conferma prenotazione");
            System.out.println("6) Check-in prenotazione");
            System.out.println("7) Segna no-show");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> staffController.showKitchenQueue();
                case "2" -> handleOrderUpdate(true);
                case "3" -> handleOrderUpdate(false);
                case "4" -> handleReservationList();
                case "5" -> handleReservationAction(Action.CONFIRM);
                case "6" -> handleReservationAction(Action.CHECK_IN);
                case "7" -> handleReservationAction(Action.NO_SHOW);
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleOrderUpdate(boolean markReady) {
        Integer orderId = readInt("ID ordine: ");
        if (orderId == null) return;
        if (markReady) {
            staffController.markOrderReady(orderId);
        } else {
            staffController.markOrderRetired(orderId);
        }
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

    private enum Action {
        CONFIRM, CHECK_IN, NO_SHOW
    }
}
