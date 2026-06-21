package Controller;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.notification.Notification;
import DomainModel.order.Order;
import DomainModel.order.OrderItem;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.Slot;
import DomainModel.user.User;
import ServiceLayer.CartService;
import ServiceLayer.MenuQueryService;
import ServiceLayer.NotificationService;
import ServiceLayer.OrderService;
import ServiceLayer.ReservationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CustomerController {

    private final MenuQueryService menuQueryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public CustomerController(MenuQueryService menuQueryService,
                              CartService cartService,
                              OrderService orderService,
                              ReservationService reservationService,
                              NotificationService notificationService) {
        this.menuQueryService = menuQueryService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    public Map<Category, List<Dish>> getMenu() throws SQLException {
        return menuQueryService.buildMenu(true, true);
    }

    public List<Dish> searchDishes(String query) throws SQLException {
        return menuQueryService.searchDishes(query, true);
    }

    public void addDishToCart(User user, int dishId, int quantity) throws SQLException {
        Dish dish = menuQueryService.findDishById(dishId)
                .filter(Dish::isAvailable)
                .orElseThrow(() -> new IllegalArgumentException("Piatto non disponibile"));
        cartService.addDishToCart(user, dish, quantity);
    }

    public String getCartSummary(User user) {
        return cartService.getCartSummary(user);
    }

    public void removeItem(User user, int dishId) {
        cartService.removeItem(user, dishId);
    }

    public Order checkoutTakeAway(User user,
                                  PaymentMethod paymentMethod,
                                  String notes) throws SQLException {
        List<OrderItem> items = cartService.getCartItems(user);
        if (items.isEmpty()) {
            throw new IllegalStateException("Il carrello è vuoto");
        }

        Order order = orderService.placeTakeAwayOrder(user, items, paymentMethod, notes);
        cartService.clearCart(user);
        return order;
    }

    public Reservation createReservation(User user,
                                         LocalDate date,
                                         int slotId,
                                         int guests,
                                         String notes) throws SQLException {
        return reservationService.createReservation(user, date, slotId, guests, notes);
    }

    public void cancelReservation(User user, int reservationId) throws SQLException {
        reservationService.cancelReservation(reservationId, user);
    }

    public List<Reservation> listReservations(User user) throws SQLException {
        return reservationService.listCustomerReservations(user);
    }

    public List<Notification> getNotifications(User user,
                                               boolean unreadOnly) throws SQLException {
        return notificationService.listNotificationsForUser(user.getId(), unreadOnly);
    }

    public void markNotificationAsRead(int notificationId) throws SQLException {
        notificationService.markAsRead(notificationId);
    }

    public List<Slot> getAvailableSlots() throws SQLException {
        return reservationService.listOpenSlots();
    }
}
