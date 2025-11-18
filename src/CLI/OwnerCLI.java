package CLI;

import Controller.OwnerController;
import DomainModel.user.User;

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
        Double price = readDouble("Prezzo: ");
        if (price == null) return;
        Integer categoryId = readInt("ID categoria: ");
        if (categoryId == null) return;
        ownerController.addDish(name, description, price, categoryId);
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
        Double price = readDouble("Nuovo prezzo: ");
        if (price == null) return;
        ownerController.updateDishPrice(dishId, price);
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

    private Double readDouble(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        if (raw.isBlank()) {
            System.out.println("Valore obbligatorio\n");
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            System.out.println("Inserire un numero valido\n");
            return null;
        }
    }

    private boolean readYesNo(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim().toLowerCase();
        return raw.startsWith("s");
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
}
