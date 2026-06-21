package ServiceLayer;

import DomainModel.order.Order;
import DomainModel.order.OrderItem;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.search.OrderSearchParameters;
import DomainModel.user.User;
import DomainModel.valueObject.Money;
import ORM.OrderDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestisce la logica applicativa sugli ordini:
 * - creare un ordine take away
 * - cambiare stato
 * - cancellare
 * - elencare ordini utente
 * Non fa I/O e non parla direttamente con JDBC.
 */
public class OrderService {

    private final OrderDAO orderDAO;

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    // ---------------------------------------------------------
    // placeOrder() - ordine d'asporto dal carrello
    // ---------------------------------------------------------

    public Order placeTakeAwayOrder(User customer,
                                    List<OrderItem> items,
                                    PaymentMethod paymentMethod,
                                    String notes) throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // calcolo totale
        Money total = new Money(0.0);
        for (OrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }

        // creo l'ordine di dominio (senza reservation perché è take away)
        Order order = new Order(
                customer,
                paymentMethod,
                total,
                notes
        );

        // salvo l'ordine → il DAO imposta l'id
        orderDAO.addOrderWithItems(order, items);

        return order;
    }

    // ---------------------------------------------------------
    // Cambiare stato dell'ordine
    // ---------------------------------------------------------

    public void changeStatus(int orderId, OrderStatus newStatus) throws SQLException {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Order order = getOrderById(orderId);
        applyTransition(order, newStatus);
        orderDAO.updateStatus(orderId, order.getStatus());
    }

    public void cancelOrder(int orderId) throws SQLException {
        changeStatus(orderId, OrderStatus.CANCELLED);
    }

    // ---------------------------------------------------------
    // Elenco ordini di un utente
    // ---------------------------------------------------------

    public List<Order> listUserOrders(User user) throws SQLException {
        return orderDAO.getOrdersByCustomer(user.getId());
    }

    public List<Order> searchOrders(OrderSearchParameters params) throws SQLException {
        return orderDAO.searchOrders(params);
    }


    // Metodo di servizio per vedere un singolo ordine se serve
    public Order getOrderById(int orderId) throws SQLException {
        return orderDAO.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    private void applyTransition(Order order, OrderStatus newStatus) {
        switch (newStatus) {
            case PREPARING -> order.markPreparing();
            case READY -> order.markReady();
            case RETIRED -> order.markRetired();
            case CANCELLED -> order.cancel();
            case CREATED -> throw new IllegalArgumentException("Cannot transition back to CREATED");
        }
    }
}
