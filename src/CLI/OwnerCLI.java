package CLI;

import Controller.OwnerController;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.ReservationStatus;
import DomainModel.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class OwnerCLI {

    private final OwnerController ownerController;
    private final Scanner scanner;

    public OwnerCLI(OwnerController ownerController, Scanner scanner) {
        this.ownerController = ownerController;
        this.scanner = scanner;
    }

    public void run(User user) {
        while (true) {
            System.out.println("=== Menu owner ===");
            System.out.println("1) Panoramica menu");
            System.out.println("2) Crea categoria");
            System.out.println("3) Crea piatto");
            System.out.println("4) Attiva/Disattiva piatto");
            System.out.println("5) Aggiorna prezzo piatto");
            System.out.println("6) Lista tavoli");
            System.out.println("7) Aggiungi tavolo");
            System.out.println("8) Lista slot");
            System.out.println("9) Aggiungi slot");
            System.out.println("10) Cerca piatti");
            System.out.println("11) Cerca ordini");
            System.out.println("12) Cerca prenotazioni");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> ownerController.printMenuOverview();
                case "2" -> handleCreateCategory();
                case "3" -> handleCreateDish();
                case "4" -> handleToggleDish();
                case "5" -> handleUpdateDishPrice();
                case "6" -> ownerController.listTables();
                case "7" -> handleAddTable();
                case "8" -> handleListSlots();
                case "9" -> handleAddSlot();
                case "10" -> handleSearchDishes();
                case "11" -> handleSearchOrders();
                case "12" -> handleSearchReservations();
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void handleCreateCategory() {
        System.out.print("Nome categoria: ");
        String name = scanner.nextLine().trim();
        System.out.print("Descrizione: ");
        String desc = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.out.println("Il nome è obbligatorio\n");
            return;
        }
        ownerController.addCategory(name, desc);
    }

    private void handleCreateDish() {
        System.out.print("Nome piatto: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.out.println("Nome obbligatorio\n");
            return;
        }
        System.out.print("Descrizione: ");
        String description = scanner.nextLine().trim();
        BigDecimal price = readBigDecimal("Prezzo: ");
        if (price == null) return;
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        ownerController.addDish(name, description, price.doubleValue(), categoryId);
    }

    private void handleToggleDish() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        boolean active = readYesNo("Impostare disponibile? (s/n): ");
        ownerController.toggleDish(dishId, active);

    }

    private void handleUpdateDishPrice() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        BigDecimal price = readBigDecimal("Nuovo prezzo: ");
        if (price == null) return;
        ownerController.updateDishPrice(dishId, price.doubleValue());
    }

    private void handleAddTable() {
        Integer number = readInt("Numero tavolo: ");
        if (number == null) return;
        Integer seats = readInt("Numero posti: ");
        if (seats == null) return;
        boolean joinable = readYesNo("Tavolo unibile? (s/n): ");
        System.out.print("Posizione: ");
        String location = scanner.nextLine().trim();
        ownerController.addTable(number, seats, joinable, location.isBlank() ? null : location);
    }

    private void handleListSlots() {
        boolean includeClosed = readYesNo("Mostrare anche slot chiusi? (s/n): ");
        ownerController.listSlots(includeClosed);
    }

    private void handleAddSlot() {
        LocalTime start = readTime("Orario inizio (HH:MM): ");
        if (start == null) return;
        LocalTime end = readTime("Orario fine (HH:MM): ");
        if (end == null) return;
        ownerController.configureSlot(start, end);
    }

    private void handleSearchDishes() {
        System.out.println("=== Ricerca piatti ===");
        System.out.print("Nome contiene (invio per saltare): ");
        String name = scanner.nextLine().trim();
        Integer categoryId = readOptionalInt("ID categoria (invio per tutti): ");
        Boolean onlyAvailable = readOptionalYesNo("Solo disponibili? (s/n, invio=tutti): ");
        BigDecimal minPrice = readOptionalBigDecimal("Prezzo minimo (invio per saltare): ");
        BigDecimal maxPrice = readOptionalBigDecimal("Prezzo massimo (invio per saltare): ");

        ownerController.searchDishes(name.isBlank() ? null : name, categoryId, onlyAvailable, minPrice, maxPrice);
    }

    private void handleSearchOrders() {
        System.out.println("=== Ricerca ordini ===");
        Integer customerId = readOptionalInt("Customer ID (invio per tutti): ");
        OrderStatus status = readOptionalOrderStatus();
        PaymentMethod paymentMethod = readOptionalPaymentMethod();
        Integer categoryId = readOptionalInt("Categoria piatto (ID, invio per tutte): ");
        LocalDate startDate = readOptionalDate("Data inizio creazione YYYY-MM-DD (invio per saltare): ");
        LocalDate endDate = readOptionalDate("Data fine creazione YYYY-MM-DD (invio per saltare): ");

        ownerController.searchOrders(customerId, status, paymentMethod, categoryId, startDate, endDate);
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

        ownerController.searchReservations(exactDate, startDate, endDate, customerId, slotId, minGuests, maxGuests, status);
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
        if (raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            System.out.println("Numero non valido, filtro ignorato.");
            return null;
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            System.out.println("Valore obbligatorio\n");
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            System.out.println("Inserire un numero valido\n");
            return null;
        }
    }

    private BigDecimal readOptionalBigDecimal(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            System.out.println("Numero non valido, filtro ignorato.");
            return null;
        }
    }

    private boolean readYesNo(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim().toLowerCase();
        return raw.startsWith("s");
    }

    private Boolean readOptionalYesNo(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim().toLowerCase();
        if (raw.isBlank()) {
            return null;
        }
        if (raw.startsWith("s")) return true;
        if (raw.startsWith("n")) return false;
        System.out.println("Valore non valido, filtro ignorato.");
        return null;
    }

    private LocalTime readTime(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            System.out.println("Valore obbligatorio\n");
            return null;
        }
        try {
            return LocalTime.parse(raw);
        } catch (DateTimeParseException e) {
            System.out.println("Formato orario non valido\n");
            return null;
        }
    }

    private LocalDate readOptionalDate(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            return null;
        }
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
}
