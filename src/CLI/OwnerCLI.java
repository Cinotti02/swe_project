package CLI;

import Controller.OwnerController;
import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.user.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class OwnerCLI {

    private final OwnerController ownerController;
    private final Scanner scanner;
    private int currentUserId;

    public OwnerCLI(OwnerController ownerController, Scanner scanner) {
        this.ownerController = ownerController;
        this.scanner = scanner;
    }

    public void run(User user) {
        this.currentUserId = user.getId();
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
            System.out.println("24) Notifiche");
            System.out.println("0) Logout");
            System.out.print("Scelta: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showMenuOverview();
                case "2" -> handleCreateCategory();
                case "3" -> handleUpdateCategory();
                case "4" -> handleToggleCategory();
                case "5" -> handleDeleteCategory();
                case "6" -> handleCreateDish();
                case "7" -> handleToggleDish();
                case "8" -> handleUpdateDishPrice();
                case "9" -> handleUpdateDishDescription();
                case "10" -> handleDeleteDish();
                case "11" -> handleListTables();
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
                case "24" -> notificationsSubmenu(user);
                case "0" -> {
                    System.out.println("Logout effettuato.\n");
                    return;
                }
                default -> System.out.println("Scelta non valida.\n");
            }
        }
    }

    private void notificationsSubmenu(User user) {
        while (true) {
            System.out.println("--- Notifiche owner ---");
            System.out.println("1) Mostra tutte");
            System.out.println("2) Mostra non lette");
            System.out.println("3) Segna come letta");
            System.out.println("0) Indietro");
            System.out.print("Scelta: ");
            String c = scanner.nextLine().trim();
            switch (c) {
                case "1" -> handleShowNotifications(user, false);
                case "2" -> handleShowNotifications(user, true);
                case "3" -> handleMarkNotificationAsRead();
                case "0" -> { return; }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void handleShowNotifications(User user, boolean unreadOnly) {
        try {
            var notifications = ownerController.getNotifications(user.getId(), unreadOnly);
            System.out.println(unreadOnly ? "=== Notifiche owner non lette ===" : "=== Notifiche owner ===");
            if (notifications.isEmpty()) {
                System.out.println("(nessuna notifica)");
                return;
            }
            notifications.forEach(n -> System.out.println("#" + n.getId() + " | " + n.getType() + " | " + n.getStatus() + " | " + n.getMessage()));
        } catch (Exception e) {
            System.err.println("Errore caricamento notifiche: " + e.getMessage());
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
        try {
            Category category = ownerController.addCategory(name, desc);
            System.out.println("Categoria creata con id " + category.getId());
            ownerController.notifyOwnerAction(currentUserId, "Categoria creata: " + name);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare la categoria: " + e.getMessage());
        }
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
        execute("Categoria aggiornata", "Impossibile aggiornare la categoria",
                () -> ownerController.renameCategory(categoryId, name, description));
    }

    private void handleToggleCategory() {
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        boolean active = readYesNo("Impostare attiva? (s/n): ");
        execute("Stato categoria aggiornato", "Impossibile aggiornare lo stato categoria",
                () -> ownerController.toggleCategory(categoryId, active));
    }

    private void handleDeleteCategory() {
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        execute("Categoria eliminata", "Impossibile eliminare la categoria",
                () -> ownerController.deleteCategory(categoryId));
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
        try {
            Dish dish = ownerController.addDish(name, description, price.doubleValue(), categoryId);
            System.out.println("Piatto creato con id " + dish.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare il piatto: " + e.getMessage());
        }
    }

    private void handleToggleDish() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        boolean active = readYesNo("Impostare disponibile? (s/n): ");
        execute("Stato piatto aggiornato", "Errore durante l'aggiornamento del piatto",
                () -> ownerController.toggleDish(dishId, active));
    }

    private void handleUpdateDishPrice() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        BigDecimal price = readBigDecimal("Nuovo prezzo: ");
        if (price == null) return;
        execute("Prezzo piatto aggiornato", "Impossibile aggiornare il prezzo",
                () -> ownerController.updateDishPrice(dishId, price.doubleValue()));
    }

    private void handleUpdateDishDescription() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        System.out.print("Nuova descrizione: ");
        String description = scanner.nextLine().trim();
        execute("Descrizione piatto aggiornata", "Impossibile aggiornare la descrizione",
                () -> ownerController.updateDishDescription(dishId, description));
    }

    private void handleDeleteDish() {
        Integer dishId = readInt("ID piatto: ");
        if (dishId == null) return;
        execute("Piatto eliminato", "Impossibile eliminare il piatto",
                () -> ownerController.deleteDish(dishId));
    }

    private void handleAddTable() {
        Integer number = readInt("Numero tavolo: ");
        if (number == null) return;
        Integer seats = readInt("Numero posti: ");
        if (seats == null) return;
        boolean joinable = readYesNo("Tavolo unibile? (s/n): ");
        System.out.print("Posizione: ");
        String location = scanner.nextLine().trim();
        try {
            Table table = ownerController.addTable(
                    number, seats, joinable, location.isBlank() ? null : location);
            System.out.println("Tavolo creato con id " + table.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare il tavolo: " + e.getMessage());
        }
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
        execute("Tavolo aggiornato", "Impossibile aggiornare il tavolo",
                () -> ownerController.updateTable(
                        tableId, number, seats, joinable, location.isBlank() ? null : location));
    }

    private void handleSetTableAvailability() {
        Integer tableId = readInt("ID tavolo: ");
        if (tableId == null) return;
        boolean available = readYesNo("Disponibile? (s/n): ");
        execute("Disponibilità tavolo aggiornata", "Impossibile aggiornare disponibilità tavolo",
                () -> ownerController.setTableAvailability(tableId, available));
    }

    private void handleDeleteTable() {
        Integer tableId = readInt("ID tavolo: ");
        if (tableId == null) return;
        execute("Tavolo eliminato", "Impossibile eliminare il tavolo",
                () -> ownerController.deleteTable(tableId));
    }

    private void handleListSlots() {
        boolean includeClosed = readYesNo("Mostrare anche slot chiusi? (s/n): ");
        try {
            printSlots(ownerController.listSlots(includeClosed));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare gli slot: " + e.getMessage());
        }
    }

    private void handleAddSlot() {
        LocalTime s = readTime("Orario inizio (HH:MM): ");
        if (s == null) return;
        LocalTime e = readTime("Orario fine (HH:MM): ");
        if (e == null) return;
        try {
            Slot slot = ownerController.configureSlot(s, e);
            System.out.println("Slot creato con id " + slot.getId());
        } catch (SQLException | IllegalArgumentException ex) {
            System.err.println("Impossibile creare lo slot: " + ex.getMessage());
        }
    }

    private void handleUpdateSlot() {
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        LocalTime s = readTime("Orario inizio (HH:MM): ");
        if (s == null) return;
        LocalTime e = readTime("Orario fine (HH:MM): ");
        if (e == null) return;
        boolean closed = readYesNo("Slot chiuso? (s/n): ");
        execute("Slot aggiornato", "Impossibile aggiornare lo slot",
                () -> ownerController.updateSlot(slotId, s, e, closed));
    }

    private void handleSetSlotClosed() {
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        boolean closed = readYesNo("Chiudere slot? (s/n): ");
        execute("Stato slot aggiornato", "Impossibile aggiornare stato slot",
                () -> ownerController.setSlotClosed(slotId, closed));
    }

    private void handleDeleteSlot() {
        Integer slotId = readInt("ID slot: ");
        if (slotId == null) return;
        execute("Slot eliminato", "Impossibile eliminare lo slot",
                () -> ownerController.deleteSlot(slotId));
    }

    private void handleSearchDishes() {
        System.out.println("=== Ricerca piatti ===");
        System.out.print("Nome contiene (invio per saltare): ");
        String name = scanner.nextLine().trim();
        Integer categoryId = readOptionalInt("ID categoria (invio per tutti): ");
        Boolean onlyAvailable = readOptionalYesNo("Solo disponibili? (s/n, invio=tutti): ");
        BigDecimal minPrice = readOptionalBigDecimal("Prezzo minimo (invio per saltare): ");
        BigDecimal maxPrice = readOptionalBigDecimal("Prezzo massimo (invio per saltare): ");

        try {
            printDishes(ownerController.searchDishes(
                    name.isBlank() ? null : name, categoryId, onlyAvailable, minPrice, maxPrice));
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca piatti: " + e.getMessage());
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
            printOrders(ownerController.searchOrders(
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
            printReservations(ownerController.searchReservations(
                    exactDate, startDate, endDate, customerId, slotId, minGuests, maxGuests, status));
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca prenotazioni: " + e.getMessage());
        }
    }

    private void handleMarkNotificationAsRead() {
        Integer notificationId = readInt("ID notifica: ");
        if (notificationId == null) return;
        try { ownerController.markNotificationAsRead(notificationId); System.out.println("Notifica segnata come letta"); } catch (Exception e) { System.err.println("Errore aggiornamento notifica: " + e.getMessage()); }
    }

    private void showMenuOverview() {
        try {
            Map<Category, List<Dish>> menu = ownerController.getMenuOverview();
            System.out.println("=== MENU COMPLETO ===");
            menu.forEach((category, dishes) -> {
                System.out.println(category.getName() + " ("
                        + (category.isActive() ? "attiva" : "disattiva") + ")");
                dishes.forEach(dish -> System.out.println("  "
                        + (dish.isAvailable() ? "[ON]" : "[OFF]")
                        + " #" + dish.getId() + " " + dish.getName() + " - " + dish.getPrice()));
            });
        } catch (SQLException e) {
            System.err.println("Errore nel caricamento del menu: " + e.getMessage());
        }
    }

    private void handleListTables() {
        try {
            System.out.println("=== TAVOLI ===");
            ownerController.listTables().forEach(table -> System.out.println(
                    "#" + table.getId() + " n." + table.getNumber()
                            + " posti:" + table.getSeats()
                            + " joinable:" + table.isJoinable()
                            + " disponibile:" + table.isAvailable()));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare i tavoli: " + e.getMessage());
        }
    }

    private void printSlots(List<Slot> slots) {
        System.out.println("=== SLOT ===");
        slots.forEach(slot -> System.out.println("#" + slot.getId() + " "
                + slot.getStartTime() + "-" + slot.getEndTime()
                + (slot.isClosed() ? " [CHIUSO]" : "")));
    }

    private void printDishes(List<Dish> dishes) {
        System.out.println("=== RISULTATI PIATTI ===");
        if (dishes.isEmpty()) {
            System.out.println("(nessun piatto trovato)");
            return;
        }
        dishes.forEach(dish -> {
            String category = dish.getCategory() != null
                    ? String.valueOf(dish.getCategory().getId()) : "-";
            System.out.println("#" + dish.getId() + " " + dish.getName()
                    + " | cat:" + category
                    + " | prezzo:" + dish.getPrice()
                    + " | disponibile:" + dish.isAvailable());
        });
    }

    private void printOrders(List<Order> orders) {
        System.out.println("=== RISULTATI ORDINI ===");
        if (orders.isEmpty()) {
            System.out.println("(nessun ordine trovato)");
            return;
        }
        orders.forEach(order -> {
            int customerId = order.getCustomer() != null ? order.getCustomer().getId() : -1;
            LocalDateTime createdAt = order.getCreatedAt();
            System.out.println("#" + order.getId()
                    + " | customer:" + customerId
                    + " | stato:" + order.getStatus()
                    + " | payment:" + order.getPaymentMethod()
                    + " | totale:" + order.getTotalAmount()
                    + " | creato:" + (createdAt != null ? createdAt : "-"));
        });
    }

    private void printReservations(List<Reservation> reservations) {
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

    private void execute(String successMessage,
                         String errorPrefix,
                         ControllerAction action) {
        try {
            action.run();
            System.out.println(successMessage);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println(errorPrefix + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ControllerAction {
        void run() throws SQLException;
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
