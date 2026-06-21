package test.ServiceLayer;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.order.Order;
import DomainModel.order.OrderItem;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.search.OrderSearchParameters;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ORM.OrderDAO;
import ServiceLayer.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private FakeOrderDAO orderDAO;
    private OrderService service;
    private User customer;

    @BeforeEach
    void setUp() {
        orderDAO = new FakeOrderDAO();
        service = new OrderService(orderDAO);
        customer = new User(
                "mario",
                new Email("mario@example.com"),
                "hash",
                "Mario",
                "Rossi",
                Role.CUSTOMER
        );
        customer.setId(7);
    }

    @Test
    void placeTakeAwayOrderCalculatesTotalAndUsesAtomicPersistence() throws SQLException {
        OrderItem pizza = item(1, "Pizza", 8.50, 2);
        OrderItem water = item(2, "Acqua", 1.50, 1);

        Order order = service.placeTakeAwayOrder(
                customer,
                List.of(pizza, water),
                PaymentMethod.ONLINE,
                "citofono rotto"
        );

        assertEquals(42, order.getId());
        assertEquals("18.50", order.getTotalAmount().getAmount().toPlainString());
        assertSame(order, orderDAO.savedOrder);
        assertEquals(List.of(pizza, water), orderDAO.savedItems);
        assertTrue(orderDAO.atomicSaveUsed);
    }

    @Test
    void placeTakeAwayOrderRejectsEmptyCart() {
        assertThrows(IllegalArgumentException.class, () ->
                service.placeTakeAwayOrder(customer, List.of(), PaymentMethod.ONLINE, null));
        assertFalse(orderDAO.atomicSaveUsed);
    }

    @Test
    void changeStatusAppliesValidDomainTransition() throws SQLException {
        Order order = existingOrder(OrderStatus.CREATED);
        orderDAO.orders.add(order);

        service.changeStatus(order.getId(), OrderStatus.PREPARING);

        assertEquals(OrderStatus.PREPARING, order.getStatus());
        assertEquals(OrderStatus.PREPARING, orderDAO.lastUpdatedStatus);
    }

    @Test
    void changeStatusRejectsInvalidTransition() {
        Order order = existingOrder(OrderStatus.CREATED);
        orderDAO.orders.add(order);

        assertThrows(IllegalStateException.class,
                () -> service.changeStatus(order.getId(), OrderStatus.RETIRED));
        assertNull(orderDAO.lastUpdatedStatus);
    }

    @Test
    void cancelOrderUsesSameTransitionRules() throws SQLException {
        Order order = existingOrder(OrderStatus.PREPARING);
        orderDAO.orders.add(order);

        service.cancelOrder(order.getId());

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(OrderStatus.CANCELLED, orderDAO.lastUpdatedStatus);
    }

    @Test
    void getOrderByIdFailsWhenOrderDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> service.getOrderById(999));
    }

    private OrderItem item(int id, String name, double price, int quantity) {
        Category category = new Category("Categoria " + id, "Descrizione");
        Dish dish = new Dish(name, "Descrizione", price, category);
        dish.setId(id);
        return new OrderItem(dish, quantity);
    }

    private Order existingOrder(OrderStatus status) {
        Order order = new Order(customer, PaymentMethod.IN_LOCO, 10.0, null);
        order.setId(12);
        if (status == OrderStatus.PREPARING) {
            order.markPreparing();
        }
        return order;
    }

    private static class FakeOrderDAO extends OrderDAO {
        private final List<Order> orders = new ArrayList<>();
        private Order savedOrder;
        private List<OrderItem> savedItems;
        private OrderStatus lastUpdatedStatus;
        private boolean atomicSaveUsed;

        @Override
        public void addOrderWithItems(Order order, List<OrderItem> items) {
            atomicSaveUsed = true;
            savedOrder = order;
            savedItems = List.copyOf(items);
            order.setId(42);
            orders.add(order);
        }

        @Override
        public void updateStatus(int orderId, OrderStatus status) {
            lastUpdatedStatus = status;
        }

        @Override
        public Optional<Order> getOrderById(int orderId) {
            return orders.stream().filter(order -> order.getId() == orderId).findFirst();
        }

        @Override
        public List<Order> getOrdersByCustomer(int customerId) {
            return List.copyOf(orders);
        }

        @Override
        public List<Order> searchOrders(OrderSearchParameters params) {
            return List.copyOf(orders);
        }
    }
}
