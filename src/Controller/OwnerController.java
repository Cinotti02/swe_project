package Controller;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.search.DishSearchParameters;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.search.SearchCriteria;
import DomainModel.valueObject.Money;
import ServiceLayer.MenuQueryService;
import ServiceLayer.NotificationService;
import ServiceLayer.OwnerAdminService;
import ServiceLayer.SearchService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class OwnerController {

    private final OwnerAdminService ownerAdminService;
    private final MenuQueryService menuQueryService;
    private final SearchService searchService;
    private final NotificationService notificationService;

    public OwnerController(OwnerAdminService ownerAdminService,
                           MenuQueryService menuQueryService,
                           SearchService searchService,
                           NotificationService notificationService) {
        this.ownerAdminService = ownerAdminService;
        this.menuQueryService = menuQueryService;
        this.searchService = searchService;
        this.notificationService = notificationService;
    }

    public List<Notification> getNotifications(int userId, boolean unreadOnly) throws SQLException {
        return notificationService.listNotificationsForUser(userId, unreadOnly);
    }

    public void markNotificationAsRead(int notificationId) throws SQLException {
        notificationService.markAsRead(notificationId);
    }

    public void notifyOwnerAction(int ownerUserId, String action) throws SQLException {
        notificationService.notifyUser(ownerUserId, action, TypeNotification.UPDATE);
    }

    public Map<Category, List<Dish>> getMenuOverview() throws SQLException {
        return menuQueryService.buildMenu(false, false);
    }

    public Category addCategory(String name, String description) throws SQLException {
        return ownerAdminService.createCategory(name, description);
    }

    public void renameCategory(int categoryId, String name, String description) throws SQLException {
        ownerAdminService.renameCategory(categoryId, name, description);
    }

    public void toggleCategory(int categoryId, boolean active) throws SQLException {
        ownerAdminService.toggleCategory(categoryId, active);
    }

    public void deleteCategory(int categoryId) throws SQLException {
        ownerAdminService.deleteCategory(categoryId);
    }

    public Dish addDish(String name, String description, double price, int categoryId) throws SQLException {
        return ownerAdminService.createDish(name, description, new Money(price), categoryId);
    }

    public void toggleDish(int dishId, boolean active) throws SQLException {
        ownerAdminService.changeDishAvailability(dishId, active);
    }

    public void updateDishPrice(int dishId, double price) throws SQLException {
        ownerAdminService.updateDishPrice(dishId, new Money(price));
    }

    public void updateDishDescription(int dishId, String description) throws SQLException {
        ownerAdminService.updateDishDescription(dishId, description);
    }

    public void deleteDish(int dishId) throws SQLException {
        ownerAdminService.deleteDish(dishId);
    }

    public List<Table> listTables() throws SQLException {
        return ownerAdminService.listTables();
    }

    public Table addTable(int number, int seats, boolean joinable, String location) throws SQLException {
        return ownerAdminService.addTable(number, seats, joinable, location);
    }

    public void updateTable(int tableId, int number, int seats,
                            boolean joinable, String location) throws SQLException {
        ownerAdminService.updateTable(tableId, number, seats, joinable, location);
    }

    public void setTableAvailability(int tableId, boolean available) throws SQLException {
        ownerAdminService.setTableAvailability(tableId, available);
    }

    public void deleteTable(int tableId) throws SQLException {
        ownerAdminService.deleteTable(tableId);
    }

    public Slot configureSlot(LocalTime startTime, LocalTime endTime) throws SQLException {
        return ownerAdminService.addSlot(startTime, endTime);
    }

    public void updateSlot(int slotId, LocalTime startTime,
                           LocalTime endTime, boolean closed) throws SQLException {
        ownerAdminService.updateSlot(slotId, startTime, endTime, closed);
    }

    public void setSlotClosed(int slotId, boolean closed) throws SQLException {
        ownerAdminService.setSlotClosed(slotId, closed);
    }

    public void deleteSlot(int slotId) throws SQLException {
        ownerAdminService.deleteSlot(slotId);
    }

    public List<Slot> listSlots(boolean includeClosed) throws SQLException {
        return ownerAdminService.listSlots(includeClosed);
    }

    public List<Dish> searchDishes(String nameContains,
                                   Integer categoryId,
                                   Boolean onlyAvailable,
                                   BigDecimal minPrice,
                                   BigDecimal maxPrice) throws SQLException {
        DishSearchParameters params = DishSearchParameters.builder();
        if (nameContains != null && !nameContains.isBlank()) params.setNameContains(nameContains);
        if (categoryId != null) params.setCategoryId(categoryId);
        if (onlyAvailable != null) params.setOnlyAvailable(onlyAvailable);
        if (minPrice != null) params.setMinPrice(minPrice);
        if (maxPrice != null) params.setMaxPrice(maxPrice);
        return searchService.search(SearchCriteria.builder().setDish(params)).getDishes();
    }

    public List<Order> searchOrders(Integer customerId,
                                    OrderStatus status,
                                    PaymentMethod paymentMethod,
                                    Integer categoryId,
                                    LocalDate startDate,
                                    LocalDate endDate) throws SQLException {
        OrderSearchParameters params = OrderSearchParameters.builder();
        if (customerId != null) params.setCustomerId(customerId);
        if (status != null) params.setStatus(status);
        if (paymentMethod != null) params.setPaymentMethod(paymentMethod);
        if (categoryId != null) params.setCategoryId(categoryId);
        if (startDate != null) params.setStartDate(startDate);
        if (endDate != null) params.setEndDate(endDate);
        return searchService.search(SearchCriteria.builder().setOrder(params)).getOrders();
    }

    public List<Reservation> searchReservations(LocalDate date,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                Integer customerId,
                                                Integer slotId,
                                                Integer minGuests,
                                                Integer maxGuests,
                                                ReservationStatus status) throws SQLException {
        ReservationSearchParameters params = ReservationSearchParameters.builder();
        if (date != null) params.setDate(date);
        if (startDate != null) params.setStartDate(startDate);
        if (endDate != null) params.setEndDate(endDate);
        if (customerId != null) params.setCustomerId(customerId);
        if (slotId != null) params.setSlotId(slotId);
        if (minGuests != null) params.setMinGuests(minGuests);
        if (maxGuests != null) params.setMaxGuests(maxGuests);
        if (status != null) params.setStatus(status);
        return searchService.search(SearchCriteria.builder().setReservation(params)).getReservations();
    }
}
