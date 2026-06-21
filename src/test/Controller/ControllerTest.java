package test.Controller;

import Controller.AuthController;
import Controller.CustomerController;
import Controller.CustomerProfileController;
import Controller.OwnerController;
import Controller.StaffController;
import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.order.Order;
import DomainModel.order.OrderItem;
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
import DomainModel.search.SearchResult;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import DomainModel.valueObject.Money;
import ServiceLayer.AuthService;
import ServiceLayer.CartService;
import ServiceLayer.MenuQueryService;
import ServiceLayer.NotificationService;
import ServiceLayer.OrderService;
import ServiceLayer.OwnerAdminService;
import ServiceLayer.ProfileService;
import ServiceLayer.ReservationService;
import ServiceLayer.SearchService;
import ServiceLayer.StaffOperationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    @Test
    void authControllerDelegatesAllUseCases() throws Exception {
        FakeAuthService service = new FakeAuthService();
        AuthController controller = new AuthController(service);

        assertSame(service.user, controller.login("a@b.it", "pwd").orElseThrow());
        assertSame(service.user,
                controller.registerCustomer("u", "a@b.it", "pwd", "A", "B"));
        controller.resetForgottenPassword("a@b.it", "new");

        assertEquals("a@b.it", service.email);
        assertEquals("new", service.password);
    }

    @Test
    void customerProfileControllerDelegatesAllOperations() throws Exception {
        FakeProfileService service = new FakeProfileService();
        CustomerProfileController controller = new CustomerProfileController(service);

        assertSame(service.user, controller.getProfile(4));
        controller.updateProfile(service.user, "new", "Nome", "Cognome");
        controller.changeEmail(service.user, "new@example.com");
        controller.addFidelityPoints(service.user, 10);

        assertEquals(4, service.requestedId);
        assertEquals("new", service.username);
        assertEquals("new@example.com", service.email);
        assertEquals(10, service.points);
    }

    @Test
    void customerControllerCoversMenuCartCheckoutReservationsAndNotifications() throws Exception {
        FakeMenuService menu = new FakeMenuService();
        CartService cart = new CartService();
        FakeOrderService orders = new FakeOrderService();
        FakeReservationService reservations = new FakeReservationService();
        FakeNotificationService notifications = new FakeNotificationService();
        CustomerController controller =
                new CustomerController(menu, cart, orders, reservations, notifications);
        User customer = user(3);

        assertSame(menu.menu, controller.getMenu());
        assertSame(menu.dishes, controller.searchDishes("pizza"));
        controller.addDishToCart(customer, menu.dish.getId(), 2);
        assertEquals(2, cart.getCartItems(customer).get(0).getQuantity());
        assertTrue(controller.getCartSummary(customer).contains("Pizza"));

        Order order = controller.checkoutTakeAway(customer, PaymentMethod.ONLINE, "note");
        assertSame(orders.order, order);
        assertTrue(cart.getCartItems(customer).isEmpty());

        assertSame(reservations.reservation,
                controller.createReservation(customer, LocalDate.now(), 2, 4, null));
        controller.cancelReservation(customer, 7);
        assertEquals(7, reservations.cancelledId);
        assertSame(reservations.reservations, controller.listReservations(customer));
        assertSame(notifications.notifications, controller.getNotifications(customer, true));
        controller.markNotificationAsRead(9);
        assertEquals(9, notifications.markedId);
        assertSame(reservations.slots, controller.getAvailableSlots());
    }

    @Test
    void customerCheckoutRejectsEmptyCartWithoutCallingOrderService() {
        FakeOrderService orders = new FakeOrderService();
        CustomerController controller = new CustomerController(
                new FakeMenuService(),
                new CartService(),
                orders,
                new FakeReservationService(),
                new FakeNotificationService()
        );

        assertThrows(IllegalStateException.class,
                () -> controller.checkoutTakeAway(user(1), PaymentMethod.ONLINE, null));
        assertFalse(orders.called);
    }

    @Test
    void ownerControllerCoversAdministrationQueriesSearchAndNotifications() throws Exception {
        FakeOwnerAdminService admin = new FakeOwnerAdminService();
        FakeMenuService menu = new FakeMenuService();
        FakeSearchService search = new FakeSearchService();
        FakeNotificationService notifications = new FakeNotificationService();
        OwnerController controller = new OwnerController(admin, menu, search, notifications);

        assertSame(notifications.notifications, controller.getNotifications(1, false));
        controller.markNotificationAsRead(2);
        controller.notifyOwnerAction(1, "azione");
        assertEquals("azione", notifications.message);
        assertSame(menu.menu, controller.getMenuOverview());

        assertSame(admin.category, controller.addCategory("Cat", "Desc"));
        controller.renameCategory(1, "Nuova", "Desc");
        controller.toggleCategory(1, false);
        controller.deleteCategory(1);
        assertSame(admin.dish, controller.addDish("Piatto", "Desc", 5, 1));
        controller.toggleDish(2, false);
        controller.updateDishPrice(2, 7);
        controller.updateDishDescription(2, "Nuova desc");
        controller.deleteDish(2);

        assertSame(admin.tables, controller.listTables());
        assertSame(admin.table, controller.addTable(1, 4, true, "sala"));
        controller.updateTable(1, 2, 6, false, "fuori");
        controller.setTableAvailability(1, false);
        controller.deleteTable(1);
        assertSame(admin.slot,
                controller.configureSlot(LocalTime.of(19, 0), LocalTime.of(21, 0)));
        controller.updateSlot(1, LocalTime.of(18, 0), LocalTime.of(20, 0), true);
        controller.setSlotClosed(1, true);
        controller.deleteSlot(1);
        assertSame(admin.slots, controller.listSlots(true));

        assertSame(search.result.getDishes(),
                controller.searchDishes("p", 1, true, BigDecimal.ONE, BigDecimal.TEN));
        assertSame(search.result.getOrders(),
                controller.searchOrders(3, OrderStatus.CREATED, PaymentMethod.ONLINE,
                        1, LocalDate.now(), LocalDate.now()));
        assertSame(search.result.getReservations(),
                controller.searchReservations(LocalDate.now(), null, null,
                        3, 2, 1, 5, ReservationStatus.CREATED));

        assertEquals(1, admin.categoryId);
        assertEquals(2, admin.dishId);
        assertEquals(1, admin.tableId);
        assertEquals(1, admin.slotId);
        assertNotNull(search.lastCriteria);
    }

    @Test
    void staffControllerCoversQueueActionsSearchAndNotifications() throws Exception {
        FakeStaffService staff = new FakeStaffService();
        FakeSearchService search = new FakeSearchService();
        FakeNotificationService notifications = new FakeNotificationService();
        StaffController controller = new StaffController(staff, search, notifications);

        assertSame(notifications.notifications, controller.getNotifications(8, true));
        controller.markNotificationAsRead(4);
        Map<OrderStatus, List<Order>> queue = controller.getKitchenQueue();
        assertEquals(OrderStatus.values().length, queue.size());
        assertEquals(OrderStatus.CANCELLED, staff.lastListedStatus);

        controller.updateOrderStatus(3, OrderStatus.PREPARING, 8);
        assertEquals(3, staff.orderId);
        assertEquals(OrderStatus.PREPARING, staff.newStatus);
        assertTrue(notifications.message.contains("#3"));

        assertSame(staff.reservations, controller.getReservations(LocalDate.now()));
        controller.confirmReservation(10);
        controller.registerCheckIn(11);
        controller.markNoShow(12);
        assertEquals(List.of(10, 11, 12), staff.reservationActions);

        assertSame(search.result.getOrders(),
                controller.searchOrders(1, OrderStatus.READY, PaymentMethod.IN_LOCO,
                        2, null, null));
        assertSame(search.result.getReservations(),
                controller.searchReservations(LocalDate.now(), null, null,
                        1, 2, 1, 6, ReservationStatus.CONFIRMED));
    }

    private static User user(int id) {
        User user = new User("user" + id, new Email("user" + id + "@example.com"),
                "hash", "Nome", "Cognome", Role.CUSTOMER);
        user.setId(id);
        return user;
    }

    private static Dish dish() {
        Category category = new Category("Pizze", "Descrizione");
        category.setId(1);
        Dish dish = new Dish("Pizza", "Descrizione", 8.0, category);
        dish.setId(2);
        return dish;
    }

    private static class FakeAuthService extends AuthService {
        private final User user = ControllerTest.user(1);
        private String email;
        private String password;

        FakeAuthService() {
            super(null);
        }

        @Override
        public Optional<User> authenticate(String email, String rawPassword) {
            this.email = email;
            this.password = rawPassword;
            return Optional.of(user);
        }

        @Override
        public User registerClient(String username, String email, String rawPassword,
                                   String name, String surname) {
            this.email = email;
            this.password = rawPassword;
            return user;
        }

        @Override
        public void resetPasswordByEmail(String email, String newRawPassword) {
            this.email = email;
            this.password = newRawPassword;
        }
    }

    private static class FakeProfileService extends ProfileService {
        private final User user = ControllerTest.user(4);
        private int requestedId;
        private String username;
        private String email;
        private int points;

        FakeProfileService() {
            super(null);
        }

        @Override
        public User getProfile(int userId) {
            requestedId = userId;
            return user;
        }

        @Override
        public void updatePersonalInfo(User user, String username, String name, String surname) {
            this.username = username;
        }

        @Override
        public void updateEmail(User user, String email) {
            this.email = email;
        }

        @Override
        public void addFidelityPoints(User user, int delta) {
            points = delta;
        }
    }

    private static class FakeMenuService extends MenuQueryService {
        private final Category category = new Category("Pizze", "Descrizione");
        private final Dish dish = ControllerTest.dish();
        private final List<Dish> dishes = List.of(dish);
        private final Map<Category, List<Dish>> menu = new LinkedHashMap<>();

        FakeMenuService() {
            super(null, null);
            menu.put(category, dishes);
        }

        @Override
        public Map<Category, List<Dish>> buildMenu(boolean active, boolean available) {
            return menu;
        }

        @Override
        public List<Dish> searchDishes(String query, boolean onlyAvailable) {
            return dishes;
        }

        @Override
        public Optional<Dish> findDishById(int dishId) {
            return Optional.of(dish);
        }
    }

    private static class FakeOrderService extends OrderService {
        private final Order order =
                new Order(ControllerTest.user(3), PaymentMethod.ONLINE, 16, null);
        private boolean called;

        FakeOrderService() {
            super(null);
            order.setId(20);
        }

        @Override
        public Order placeTakeAwayOrder(User customer, List<OrderItem> items,
                                        PaymentMethod paymentMethod, String notes) {
            called = true;
            return order;
        }
    }

    private static class FakeReservationService extends ReservationService {
        private final Slot slot = new Slot(LocalTime.of(19, 0), LocalTime.of(21, 0));
        private final Reservation reservation =
                new Reservation(ControllerTest.user(3),
                        LocalDateTime.now().withHour(19).withMinute(0),
                        slot, 2, null);
        private final List<Reservation> reservations = List.of(reservation);
        private final List<Slot> slots = List.of(slot);
        private int cancelledId;

        FakeReservationService() {
            super(null, null, null, null, null);
        }

        @Override
        public Reservation createReservation(User customer, LocalDate date,
                                             int slotId, int guests, String notes) {
            return reservation;
        }

        @Override
        public void cancelReservation(int reservationId, User requester) {
            cancelledId = reservationId;
        }

        @Override
        public List<Reservation> listCustomerReservations(User customer) {
            return reservations;
        }

        @Override
        public List<Slot> listOpenSlots() {
            return slots;
        }
    }

    private static class FakeNotificationService extends NotificationService {
        private final List<Notification> notifications = List.of(new Notification());
        private Integer markedId;
        private String message;

        FakeNotificationService() {
            super(null);
        }

        @Override
        public List<Notification> listNotificationsForUser(int userId, boolean unreadOnly) {
            return notifications;
        }

        @Override
        public void notifyUser(int userId, String message, TypeNotification type) {
            this.message = message;
        }

        @Override
        public void markAsRead(int notificationId) {
            markedId = notificationId;
        }
    }

    private static class FakeOwnerAdminService extends OwnerAdminService {
        private final Category category = new Category("Categoria", "Descrizione");
        private final Dish dish = ControllerTest.dish();
        private final Table table = new Table(1, 4, true, "sala");
        private final Slot slot = new Slot(LocalTime.of(19, 0), LocalTime.of(21, 0));
        private final List<Table> tables = List.of(table);
        private final List<Slot> slots = List.of(slot);
        private int categoryId;
        private int dishId;
        private int tableId;
        private int slotId;

        FakeOwnerAdminService() {
            super(null, null, null, null);
        }

        @Override public Category createCategory(String n, String d) { return category; }
        @Override public void renameCategory(int id, String n, String d) { categoryId = id; }
        @Override public void toggleCategory(int id, boolean active) { categoryId = id; }
        @Override public void deleteCategory(int id) { categoryId = id; }
        @Override public Dish createDish(String n, String d, Money p, int id) {
            categoryId = id; return dish;
        }
        @Override public void changeDishAvailability(int id, boolean a) { dishId = id; }
        @Override public void updateDishPrice(int id, Money p) { dishId = id; }
        @Override public void updateDishDescription(int id, String d) { dishId = id; }
        @Override public void deleteDish(int id) { dishId = id; }
        @Override public List<Table> listTables() { return tables; }
        @Override public Table addTable(int n, int s, boolean j, String l) { return table; }
        @Override public void updateTable(int id, int n, int s, boolean j, String l) { tableId = id; }
        @Override public void setTableAvailability(int id, boolean a) { tableId = id; }
        @Override public void deleteTable(int id) { tableId = id; }
        @Override public Slot addSlot(LocalTime s, LocalTime e) { return slot; }
        @Override public void updateSlot(int id, LocalTime s, LocalTime e, boolean c) { slotId = id; }
        @Override public void setSlotClosed(int id, boolean c) { slotId = id; }
        @Override public void deleteSlot(int id) { slotId = id; }
        @Override public List<Slot> listSlots(boolean includeClosed) { return slots; }
    }

    private static class FakeSearchService extends SearchService {
        private final SearchResult result = new SearchResult();
        private SearchCriteria lastCriteria;

        FakeSearchService() {
            super(null, null, null);
            result.setDishes(List.of(ControllerTest.dish()));
            result.setOrders(List.of(new Order()));
            result.setReservations(List.of(new Reservation()));
        }

        @Override
        public SearchResult search(SearchCriteria criteria) {
            lastCriteria = criteria;
            return result;
        }
    }

    private static class FakeStaffService extends StaffOperationService {
        private OrderStatus lastListedStatus;
        private int orderId;
        private OrderStatus newStatus;
        private final List<Reservation> reservations = List.of(new Reservation());
        private final List<Integer> reservationActions = new java.util.ArrayList<>();

        FakeStaffService() {
            super(null, null, null);
        }

        @Override
        public List<Order> listOrdersByStatus(OrderStatus status) {
            lastListedStatus = status;
            return List.of();
        }

        @Override
        public void updateOrderStatus(int orderId, OrderStatus newStatus) {
            this.orderId = orderId;
            this.newStatus = newStatus;
        }

        @Override
        public List<Reservation> reservationsForDate(LocalDate date) {
            return reservations;
        }

        @Override public void confirmReservation(int id) { reservationActions.add(id); }
        @Override public void registerCheckIn(int id) { reservationActions.add(id); }
        @Override public void registerNoShow(int id) { reservationActions.add(id); }
    }
}
