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
            System.out.println("3) Modifica categoria");
            System.out.println("4) Attiva/Disattiva categoria");
            System.out.println("5) Elimina categoria");
            System.out.println("6) Crea piatto");
            System.out.println("7) Attiva/Disattiva piatto");
            System.out.println("8) Aggiorna prezzo piatto");
            System.out.println("9) Aggiorna descrizione piatto");
            System.out.println("10) Elimina piatto");
            System.out.println("11) Lista tavoli");
            System.out.println("12) Aggiungi tavolo");
            System.out.println("13) Modifica tavolo");
            System.out.println("14) Imposta disponibilità tavolo");
            System.out.println("15) Elimina tavolo");
            System.out.println("16) Lista slot");
            System.out.println("17) Aggiungi slot");
            System.out.println("18) Modifica slot");
            System.out.println("19) Imposta stato slot (chiuso/aperto)");
            System.out.println("20) Elimina slot");
            System.out.println("21) Cerca piatti");
            System.out.println("22) Cerca ordini");
            System.out.println("23) Cerca prenotazioni");
            System.out.println("24) Mostra notifiche");
            System.out.println("25) Mostra notifiche non lette");
            System.out.println("26) Segna notifica come letta");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> ownerController.printMenuOverview();
                case "2" -> handleCreateCategory();
                case "3" -> handleUpdateCategory();
                case "4" -> handleToggleCategory();
                case "5" -> handleDeleteCategory();
                case "6" -> handleCreateDish();
                case "7" -> handleToggleDish();
                case "8" -> handleUpdateDishPrice();
                case "9" -> handleUpdateDishDescription();
                case "10" -> handleDeleteDish();
                case "11" -> ownerController.listTables();
                case "12" -> handleAddTable();
                case "13" -> handleUpdateTable();
                case "14" -> handleSetTableAvailability();
                case "15" -> handleDeleteTable();
                case "16" -> handleListSlots();
                case "17" -> handleAddSlot();
                case "18" -> handleUpdateSlot();
                case "19" -> handleSetSlotClosed();
                case "20" -> handleDeleteSlot();
                case "21" -> handleSearchDishes();
                case "22" -> handleSearchOrders();
                case "23" -> handleSearchReservations();
                case "24" -> ownerController.showNotifications(user.getId(), false);
                case "25" -> ownerController.showNotifications(user.getId(), true);
                case "26" -> handleMarkNotificationAsRead();
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

    private void handleUpdateCategory() {
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        System.out.print("Nuovo nome: ");
        String name = scanner.nextLine().trim();
        System.out.print("Nuova descrizione: ");
        String description = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.out.println("Nome obbligatorio\n");
            return;
        }
        ownerController.renameCategory(categoryId, name, description);
    }

    private void handleToggleCategory() {
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        boolean active = readYesNo("Impostare attiva? (s/n): ");
        ownerController.toggleCategory(categoryId, active);
    }

    private void handleDeleteCategory() {
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        ownerController.deleteCategory(categoryId);
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
        ownerController.toggleDish(dishId, readYesNo("Impostare disponibile? (s/n): "));
    }

    private void handleUpdateDishPrice() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        BigDecimal price = readBigDecimal("Nuovo prezzo: ");
        if (price == null) return;
        ownerController.updateDishPrice(dishId, price.doubleValue());
    }

    private void handleUpdateDishDescription() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        System.out.print("Nuova descrizione: ");
        ownerController.updateDishDescription(dishId, scanner.nextLine().trim());
    }

    private void handleDeleteDish() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        ownerController.deleteDish(dishId);
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

    private void handleUpdateTable() {
        Integer tableId = readInt("ID tavolo: ");
        if (tableId == null) return;
        Integer number = readInt("Numero tavolo: ");
        if (number == null) return;
        Integer seats = readInt("Numero posti: ");
        if (seats == null) return;
        boolean joinable = readYesNo("Tavolo unibile? (s/n): ");
        System.out.print("Posizione: ");
        String location = scanner.nextLine().trim();
        ownerController.updateTable(tableId, number, seats, joinable, location.isBlank() ? null : location);
    }

    private void handleSetTableAvailability() {
        Integer tableId = readInt("ID tavolo: ");
        if (tableId == null) return;
        ownerController.setTableAvailability(tableId, readYesNo("Disponibile? (s/n): "));
    }

    private void handleDeleteTable() {
        Integer tableId = readInt("ID tavolo: ");
        if (tableId == null) return;
        ownerController.deleteTable(tableId);
    }

    private void handleListSlots() {
        ownerController.listSlots(readYesNo("Mostrare anche slot chiusi? (s/n): "));
    }

    private void handleAddSlot() {
        LocalTime s = readTime("Orario inizio (HH:MM): ");
        if (s == null) return;
        LocalTime e = readTime("Orario fine (HH:MM): ");
        if (e == null) return;
        ownerController.configureSlot(s, e);
    }

    private void handleUpdateSlot() {
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        LocalTime s = readTime("Orario inizio (HH:MM): ");
        if (s == null) return;
        LocalTime e = readTime("Orario fine (HH:MM): ");
        if (e == null) return;
        boolean closed = readYesNo("Slot chiuso? (s/n): ");
        ownerController.updateSlot(slotId, s, e, closed);
    }

    private void handleSetSlotClosed() {
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        ownerController.setSlotClosed(slotId, readYesNo("Chiudere slot? (s/n): "));
    }

    private void handleDeleteSlot() {
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        ownerController.deleteSlot(slotId);
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

    private void handleMarkNotificationAsRead() {
        Integer notificationId = readInt("ID notifica: ");
        if (notificationId == null) return;
        ownerController.markNotificationAsRead(notificationId);
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
