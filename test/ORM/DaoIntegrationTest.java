package ORM;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.notification.Notification;
import DomainModel.notification.StatusNotification;
import DomainModel.notification.TypeNotification;
import DomainModel.order.Order;
import DomainModel.order.OrderItem;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.MergeTable;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.search.DishSearchParameters;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ORM.CategoryDAO;
import ORM.DBConnection;
import ORM.DishDAO;
import ORM.NotificationDAO;
import ORM.OrderDAO;
import ORM.OrderItemDAO;
import ORM.ReservationDAO;
import ORM.SlotDAO;
import ORM.TableDAO;
import ORM.UserDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DaoIntegrationTest {

    private final String schema = "dineup_test_" +
            UUID.randomUUID().toString().replace("-", "");
    private String baseUrl;
    private String user;
    private String password;

    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final DishDAO dishDAO = new DishDAO();
    private final TableDAO tableDAO = new TableDAO();
    private final SlotDAO slotDAO = new SlotDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @BeforeAll
    void createIsolatedSchema() throws Exception {
        Properties config = databaseConfig();
        baseUrl = config.getProperty("db.URL");
        user = config.getProperty("db.USER");
        password = config.getProperty("db.PASSWORD");

        try (Connection connection = DriverManager.getConnection(baseUrl, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            statement.execute("SET search_path TO " + schema);
            statement.execute(Files.readString(Path.of("sql", "schema.sql")));
        }

        System.setProperty("db.url", withCurrentSchema(baseUrl, schema));
        System.setProperty("db.user", user);
        System.setProperty("db.password", password);
    }

    @AfterAll
    void dropIsolatedSchema() throws Exception {
        try (Connection connection = DriverManager.getConnection(baseUrl, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        } finally {
            System.clearProperty("db.url");
            System.clearProperty("db.user");
            System.clearProperty("db.password");
        }
    }

    @BeforeEach
    void clearData() throws Exception {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    TRUNCATE TABLE merge_tables, order_items, notifications, orders,
                                   reservations, dishes, categories, slots, tables, users
                    RESTART IDENTITY CASCADE
                    """);
        }
    }

    @Test
    void dbConnectionUsesConfiguredTestSchema() throws Exception {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT current_schema(), 1")) {
            assertTrue(rs.next());
            assertEquals(schema, rs.getString(1));
            assertEquals(1, rs.getInt(2));
        }
    }

    @Test
    void userDaoCoversCreateReadUpdateSearchAndDelete() throws Exception {
        User saved = addUser("mario", "mario@example.com");
        assertTrue(saved.getId() > 0);
        assertEquals(saved.getId(), userDAO.getUserById(saved.getId()).orElseThrow().getId());
        assertEquals(saved.getId(), userDAO.getUserByEmail(saved.getEmailValue()).orElseThrow().getId());
        assertTrue(userDAO.emailExists(saved.getEmailValue()));
        assertEquals(1, userDAO.getUsersByName("Nom").size());
        assertEquals(1, userDAO.getAllUsers().size());

        saved.setFidelityPoints(25);
        saved.setName("Luigi");
        userDAO.updateUser(saved);
        assertEquals(25, userDAO.getUserById(saved.getId()).orElseThrow().getFidelityPoints());
        assertEquals("Luigi", userDAO.getUserById(saved.getId()).orElseThrow().getName());

        assertTrue(userDAO.removeUser(saved.getId()));
        assertTrue(userDAO.getUserById(saved.getId()).isEmpty());
    }

    @Test
    void categoryDaoCoversCrudActivationAndNameSearch() throws Exception {
        Category category = addCategory("Pizze");
        assertEquals(category.getId(), categoryDAO.getCategoryById(category.getId()).orElseThrow().getId());
        assertEquals(1, categoryDAO.getAllCategories().size());
        assertEquals(1, categoryDAO.getActiveCategories().size());
        assertEquals(1, categoryDAO.findByName("piz", true).size());

        category.setName("Pizze classiche");
        category.deactivate();
        categoryDAO.updateCategory(category);
        assertEquals("Pizze classiche",
                categoryDAO.getCategoryById(category.getId()).orElseThrow().getName());
        assertTrue(categoryDAO.getActiveCategories().isEmpty());

        categoryDAO.setCategoryActive(category.getId(), true);
        assertTrue(categoryDAO.getCategoryById(category.getId()).orElseThrow().isActive());
        categoryDAO.deleteCategory(category.getId());
        assertTrue(categoryDAO.getCategoryById(category.getId()).isEmpty());
    }

    @Test
    void dishDaoCoversCrudCategoryAndDynamicSearch() throws Exception {
        Category category = addCategory("Primi");
        Dish dish = addDish(category, "Pasta", 12.50);

        assertEquals(dish.getId(), dishDAO.getDishById(dish.getId()).orElseThrow().getId());
        assertEquals(1, dishDAO.getAllDishes().size());
        assertEquals(1, dishDAO.getDishesByCategory(category.getId()).size());

        DishSearchParameters params = DishSearchParameters.builder()
                .setCategoryId(category.getId())
                .setNameContains("pas")
                .setOnlyAvailable(true)
                .setMinPrice(new java.math.BigDecimal("10"))
                .setMaxPrice(new java.math.BigDecimal("13"));
        assertEquals(1, dishDAO.searchDishes(params).size());

        dish.setPrice(13.0);
        dish.markUnavailable();
        dishDAO.updateDish(dish);
        assertEquals("13.00",
                dishDAO.getDishById(dish.getId()).orElseThrow().getPrice().getAmount().toPlainString());
        assertTrue(dishDAO.searchDishes(params).isEmpty());

        dishDAO.setDishActive(dish.getId(), true);
        assertTrue(dishDAO.getDishById(dish.getId()).orElseThrow().isAvailable());
        dishDAO.deleteDish(dish.getId());
        assertTrue(dishDAO.getDishById(dish.getId()).isEmpty());
    }

    @Test
    void tableDaoCoversCrudLookupAndAvailability() throws Exception {
        Table table = new Table(10, 4, true, "sala");
        tableDAO.addTable(table);

        assertEquals(table.getId(), tableDAO.getTableById(table.getId()).orElseThrow().getId());
        assertEquals(table.getId(), tableDAO.getTableByNumber(10).orElseThrow().getId());
        assertEquals(1, tableDAO.getAllTables().size());
        assertEquals(1, tableDAO.getAvailableTables().size());

        table.setSeats(6);
        table.setLocation("terrazza");
        tableDAO.updateTable(table);
        assertEquals(6, tableDAO.getTableById(table.getId()).orElseThrow().getSeats());
        tableDAO.setAvailability(table.getId(), false);
        assertTrue(tableDAO.getAvailableTables().isEmpty());

        tableDAO.deleteTable(table.getId());
        assertTrue(tableDAO.getTableById(table.getId()).isEmpty());
    }

    @Test
    void slotDaoCoversCrudFiltersAndContainment() throws Exception {
        Slot slot = new Slot(LocalTime.of(19, 0), LocalTime.of(21, 0));
        slotDAO.addSlot(slot);

        assertEquals(slot.getId(), slotDAO.getSlotById(slot.getId()).orElseThrow().getId());
        assertEquals(1, slotDAO.getAllSlots().size());
        assertEquals(1, slotDAO.getOpenSlots().size());
        assertEquals(1, slotDAO.findSlotsContaining(LocalTime.of(20, 0)).size());

        slot.setStartTime(LocalTime.of(18, 30));
        slot.setEndTime(LocalTime.of(20, 30));
        slot.setClosed(true);
        slotDAO.updateSlot(slot);
        assertTrue(slotDAO.getSlotById(slot.getId()).orElseThrow().isClosed());
        assertTrue(slotDAO.getOpenSlots().isEmpty());

        slotDAO.setClosed(slot.getId(), false);
        assertFalse(slotDAO.getSlotById(slot.getId()).orElseThrow().isClosed());
        slotDAO.deleteSlot(slot.getId());
        assertTrue(slotDAO.getSlotById(slot.getId()).isEmpty());
    }

    @Test
    void reservationDaoCoversCrudSearchAndTableAssignments() throws Exception {
        User customer = addUser("anna", "anna@example.com");
        Slot slot = addSlot();
        Table table = addTable(1, 4);
        Reservation reservation = new Reservation(
                customer,
                LocalDateTime.of(2027, 1, 10, 19, 0),
                slot,
                3,
                "finestra"
        );

        List<MergeTable> assignments =
                reservationDAO.addReservationWithTables(reservation, List.of(table));

        assertTrue(reservation.getId() > 0);
        assertEquals(1, assignments.size());
        assertEquals(1, reservationDAO.getTableAssignments(reservation.getId()).size());
        assertEquals(List.of(table.getId()),
                reservationDAO.getReservedTableIds(LocalDate.of(2027, 1, 10), slot.getId()));
        assertEquals(1, reservationDAO.getReservationsByCustomer(customer.getId()).size());
        assertEquals(1, reservationDAO.getReservationsByDate(LocalDate.of(2027, 1, 10)).size());

        ReservationSearchParameters params = ReservationSearchParameters.builder()
                .setCustomerId(customer.getId())
                .setSlotId(slot.getId())
                .setStatus(ReservationStatus.CREATED);
        assertEquals(1, reservationDAO.searchReservations(params).size());

        reservation.setNumberOfGuests(4);
        reservation.setNotes("compleanno");
        reservationDAO.updateReservation(reservation);
        assertEquals(4, reservationDAO.getReservationById(reservation.getId())
                .orElseThrow().getNumberOfGuests());

        reservationDAO.updateStatus(reservation.getId(), ReservationStatus.CANCELED);
        assertEquals(ReservationStatus.CANCELED,
                reservationDAO.getReservationById(reservation.getId()).orElseThrow().getStatus());
        reservationDAO.deleteReservation(reservation.getId());
        assertTrue(reservationDAO.getReservationById(reservation.getId()).isEmpty());
    }

    @Test
    void doubleBookingTriggerRejectsConcurrentEquivalentAssignment() throws Exception {
        User customer = addUser("anna", "anna@example.com");
        Slot slot = addSlot();
        Table table = addTable(1, 4);
        LocalDateTime dateTime = LocalDateTime.of(2027, 1, 10, 19, 0);

        reservationDAO.addReservationWithTables(
                new Reservation(customer, dateTime, slot, 2, null), List.of(table));

        assertThrows(java.sql.SQLException.class, () ->
                reservationDAO.addReservationWithTables(
                        new Reservation(customer, dateTime, slot, 2, null), List.of(table)));
    }

    @Test
    void orderAndOrderItemDaosCoverAtomicSaveSearchStatusAndDeletion() throws Exception {
        User customer = addUser("paolo", "paolo@example.com");
        Category category = addCategory("Panini");
        Dish dish = addDish(category, "Burger", 9.00);
        OrderItem item = new OrderItem(dish, 2);
        Order order = new Order(customer, PaymentMethod.ONLINE, 18.0, "senza cipolla");

        orderDAO.addOrderWithItems(order, List.of(item));
        assertTrue(order.getId() > 0);
        assertTrue(item.getId() > 0);
        assertEquals(1, orderItemDAO.getItemsByOrder(order.getId()).size());
        assertEquals(1, orderDAO.getOrdersByCustomer(customer.getId()).size());
        assertEquals(1, orderDAO.getOrdersByStatus(OrderStatus.CREATED).size());

        OrderSearchParameters params = OrderSearchParameters.builder()
                .setCustomerId(customer.getId())
                .setCategoryId(category.getId())
                .setPaymentMethod(PaymentMethod.ONLINE);
        assertEquals(1, orderDAO.searchOrders(params).size());

        orderDAO.updateStatus(order.getId(), OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING,
                orderDAO.getOrderById(order.getId()).orElseThrow().getStatus());
        orderDAO.updatePayment(order.getId(), PaymentMethod.IN_LOCO);
        assertEquals(PaymentMethod.IN_LOCO,
                orderDAO.getOrderById(order.getId()).orElseThrow().getPaymentMethod());

        orderItemDAO.deleteItemsByOrder(order.getId());
        assertTrue(orderItemDAO.getItemsByOrder(order.getId()).isEmpty());
        orderDAO.deleteOrder(order.getId());
        assertTrue(orderDAO.getOrderById(order.getId()).isEmpty());
    }

    @Test
    void notificationDaoCoversCreateQueriesStateChangesAndDelete() throws Exception {
        User recipient = addUser("luca", "luca@example.com");
        Notification notification =
                new Notification(recipient, "Ordine pronto", TypeNotification.ALERT);
        notificationDAO.addNotification(notification);

        assertTrue(notification.getId() > 0);
        assertEquals(1, notificationDAO.getNotificationsForUser(recipient.getId()).size());
        assertEquals(1, notificationDAO.getUnreadNotificationsForUser(recipient.getId()).size());

        notificationDAO.markAsRead(notification.getId());
        Notification read = notificationDAO.getNotificationById(notification.getId()).orElseThrow();
        assertEquals(StatusNotification.READ, read.getStatus());
        assertNotNull(read.getReadAt());
        assertTrue(notificationDAO.getUnreadNotificationsForUser(recipient.getId()).isEmpty());

        notificationDAO.markAsFailed(notification.getId());
        assertEquals(StatusNotification.FAILED,
                notificationDAO.getNotificationById(notification.getId()).orElseThrow().getStatus());
        notificationDAO.deleteNotification(notification.getId());
        assertTrue(notificationDAO.getNotificationById(notification.getId()).isEmpty());
    }

    private User addUser(String username, String email) throws Exception {
        User user = new User(username, new Email(email), "hash",
                "Nome", "Cognome", Role.CUSTOMER);
        userDAO.addUser(user);
        return user;
    }

    private Category addCategory(String name) throws Exception {
        Category category = new Category(name, "Descrizione");
        categoryDAO.addCategory(category);
        return category;
    }

    private Dish addDish(Category category, String name, double price) throws Exception {
        Dish dish = new Dish(name, "Descrizione", price, category);
        dishDAO.addDish(dish);
        return dish;
    }

    private Slot addSlot() throws Exception {
        Slot slot = new Slot(LocalTime.of(19, 0), LocalTime.of(21, 0));
        slotDAO.addSlot(slot);
        return slot;
    }

    private Table addTable(int number, int seats) throws Exception {
        Table table = new Table(number, seats, true, "sala");
        tableDAO.addTable(table);
        return table;
    }

    private Properties databaseConfig() throws Exception {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(Path.of("src", "ORM", "db.properties"))) {
            props.load(input);
        }
        return props;
    }

    private String withCurrentSchema(String url, String schema) {
        return url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema;
    }
}
