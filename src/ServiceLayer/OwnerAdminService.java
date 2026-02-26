package ServiceLayer;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.valueObject.Money;
import ORM.CategoryDAO;
import ORM.DishDAO;
import ORM.SlotDAO;
import ORM.TableDAO;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class OwnerAdminService {

    private final DishDAO dishDAO;
    private final CategoryDAO categoryDAO;
    private final TableDAO tableDAO;
    private final SlotDAO slotDAO;

    public OwnerAdminService(DishDAO dishDAO,
                             CategoryDAO categoryDAO,
                             TableDAO tableDAO,
                             SlotDAO slotDAO) {
        this.dishDAO = dishDAO;
        this.categoryDAO = categoryDAO;
        this.tableDAO = tableDAO;
        this.slotDAO = slotDAO;
    }

    public Category createCategory(String name, String description) throws SQLException {
        Category category = new Category(name, description);
        categoryDAO.addCategory(category);
        return category;
    }

    public void renameCategory(int categoryId, String newName, String newDescription) throws SQLException {
        Category category = categoryDAO.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        if (newName != null && !newName.isBlank()) {
            category.setName(newName);
        }
        category.setDescription(newDescription);
        categoryDAO.updateCategory(category);
    }

    public void toggleCategory(int categoryId, boolean active) throws SQLException {
        Category category = categoryDAO.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        if (active) {
            category.activate();
        } else {
            category.deactivate();
        }
        categoryDAO.updateCategory(category);
    }

    public void deleteCategory(int categoryId) throws SQLException {
        categoryDAO.deleteCategory(categoryId);
    }

    public Dish createDish(String name,
                           String description,
                           Money price,
                           int categoryId) throws SQLException {
        Category category = categoryDAO.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        Dish dish = new Dish(name, description, price, category);
        dishDAO.addDish(dish);
        return dish;
    }

    public void changeDishAvailability(int dishId, boolean available) throws SQLException {
        Dish dish = dishDAO.getDishById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));
        if (available) {
            dish.markAvailable();
        } else {
            dish.markUnavailable();
        }
        dishDAO.updateDish(dish);
    }

    public void updateDishPrice(int dishId, Money newPrice) throws SQLException {
        Dish dish = dishDAO.getDishById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));
        dish.setPrice(newPrice);
        dishDAO.updateDish(dish);
    }

    public void updateDishDescription(int dishId, String newDescription) throws SQLException {
        Dish dish = dishDAO.getDishById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));
        dish.setDescription(newDescription);
        dishDAO.updateDish(dish);
    }

    public void deleteDish(int dishId) throws SQLException {
        dishDAO.deleteDish(dishId);
    }

    public Table addTable(int number, int seats, boolean joinable, String location) throws SQLException {
        Table table = new Table(number, seats, joinable, location);
        tableDAO.addTable(table);
        return table;
    }

    public void updateTable(int tableId, int number, int seats, boolean joinable, String location) throws SQLException {
        Table table = tableDAO.getTableById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));
        table.setNumber(number);
        table.setSeats(seats);
        table.setJoinable(joinable);
        table.setLocation(location);
        tableDAO.updateTable(table);
    }

    public void setTableAvailability(int tableId, boolean available) throws SQLException {
        tableDAO.setAvailability(tableId, available);
    }

    public void deleteTable(int tableId) throws SQLException {
        tableDAO.deleteTable(tableId);
    }

    public Slot addSlot(LocalTime startTime, LocalTime endTime) throws SQLException {
        Slot slot = new Slot(startTime, endTime);
        slotDAO.addSlot(slot);
        return slot;
    }

    public void updateSlot(int slotId, LocalTime startTime, LocalTime endTime, boolean closed) throws SQLException {
        Slot slot = slotDAO.getSlotById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setClosed(closed);
        slotDAO.updateSlot(slot);
    }

    public void setSlotClosed(int slotId, boolean closed) throws SQLException {
        slotDAO.setClosed(slotId, closed);
    }

    public void deleteSlot(int slotId) throws SQLException {
        slotDAO.deleteSlot(slotId);
    }

    public List<Table> listTables() throws SQLException {
        return tableDAO.getAllTables();
    }

    public List<Slot> listSlots(boolean includeClosed) throws SQLException {
        return includeClosed ? slotDAO.getAllSlots() : slotDAO.getOpenSlots();
    }
}