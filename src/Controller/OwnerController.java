package Controller;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.valueObject.Money;
import ServiceLayer.MenuQueryService;
import ServiceLayer.OwnerAdminService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class OwnerController {

    private final OwnerAdminService ownerAdminService;
    private final MenuQueryService menuQueryService;

    public OwnerController(OwnerAdminService ownerAdminService, MenuQueryService menuQueryService) {
        this.ownerAdminService = ownerAdminService;
        this.menuQueryService = menuQueryService;
    }

    public void printMenuOverview() {
        try {
            Map<Category, List<Dish>> menu = menuQueryService.buildMenu(false, false);
            System.out.println("=== MENU COMPLETO ===");
            menu.forEach((category, dishes) -> {
                System.out.println(category.getName() + " (" + (category.isActive() ? "attiva" : "disattiva") + ")");
                for (Dish dish : dishes) {
                    String availability = dish.isAvailable() ? "[ON]" : "[OFF]";
                    System.out.println("  " + availability + " #" + dish.getId() + " " + dish.getName() + " - " + dish.getPrice());
                }
            });
        } catch (SQLException e) {
            System.err.println("Errore nel caricamento del menu: " + e.getMessage());
        }
    }

    public void addCategory(String name, String description) {
        try {
            Category category = ownerAdminService.createCategory(name, description);
            System.out.println("Categoria creata con id " + category.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare la categoria: " + e.getMessage());
        }
    }

    public void addDish(String name, String description, Double price, int categoryId) {
        try {
            Dish dish = ownerAdminService.createDish(name, description, new Money(price), categoryId);
            System.out.println("Piatto creato con id " + dish.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare il piatto: " + e.getMessage());
        }
    }

    public void toggleDish(int dishId, boolean active) {
        try {
            ownerAdminService.changeDishAvailability(dishId, active);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore durante l'aggiornamento del piatto: " + e.getMessage());
        }
    }

    public void updateDishPrice(int dishId, double price) {
        try {
            ownerAdminService.updateDishPrice(dishId, new Money(price));
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile aggiornare il prezzo: " + e.getMessage());
        }
    }

    public void listTables() {
        try {
            List<Table> tables = ownerAdminService.listTables();
            System.out.println("=== TAVOLI ===");
            tables.forEach(table -> System.out.println("#" + table.getId() + " n." + table.getNumber()
                    + " posti:" + table.getSeats() + " joinable:" + table.isJoinable()
                    + " disponibile:" + table.isAvailable()));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare i tavoli: " + e.getMessage());
        }
    }

    public void addTable(int number, int seats, boolean joinable, String location) {
        try {
            Table table = ownerAdminService.addTable(number, seats, joinable, location);
            System.out.println("Tavolo creato con id " + table.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare il tavolo: " + e.getMessage());
        }
    }

    public void configureSlot(LocalTime startTime, LocalTime endTime) {
        try {
            Slot slot = ownerAdminService.addSlot(startTime, endTime);
            System.out.println("Slot creato con id " + slot.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare lo slot: " + e.getMessage());
        }
    }

    public void listSlots(boolean includeClosed) {
        try {
            List<Slot> slots = ownerAdminService.listSlots(includeClosed);
            System.out.println("=== SLOT ===");
            for (Slot slot : slots) {
                System.out.println("#" + slot.getId() + " " + slot.getStartTime() + "-" + slot.getEndTime()
                        + (slot.isClosed() ? " [CHIUSO]" : ""));
            }
        } catch (SQLException e) {
            System.err.println("Impossibile caricare gli slot: " + e.getMessage());
        }
    }
}