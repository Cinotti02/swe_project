package ORM;

import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.search.OrderSearchParameters;
import DomainModel.user.User;
import DomainModel.valueObject.Money;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO extends BaseDAO {

    // ------------------------------------------------------------
    // Crea un nuovo ordine
    // ------------------------------------------------------------
    public void addOrder(Order order) throws SQLException {
        String sql = """
                INSERT INTO orders(customer_id, created_at, status, payment_method, total_amount, notes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            ps.setInt(1, order.getCustomer().getId());
            ps.setTimestamp(2, Timestamp.valueOf(order.getCreatedAt()));
            ps.setString(3, order.getStatus().name());
            ps.setString(4, order.getPaymentMethod().name());
            ps.setBigDecimal(5, order.getTotalAmount().getAmount());
            ps.setString(6, order.getNotes());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getInt(1));
                }
            }
        }
    }

    // ------------------------------------------------------------
    // Cambia lo stato dell’ordine
    // ------------------------------------------------------------
    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------
    // Aggiorna metodo di pagamento (opzionale)
    // ------------------------------------------------------------
    public void updatePayment(int orderId, PaymentMethod method) throws SQLException {
        String sql = "UPDATE orders SET payment_method = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, method.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------
    // Cancella ordine
    // ------------------------------------------------------------
    public void deleteOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------
    // Trova ordine per ID
    // ------------------------------------------------------------
    public Optional<Order> getOrderById(int orderId) throws SQLException {
        String sql = """
                SELECT id, customer_id, created_at, status, payment_method, total_amount, notes
                FROM orders WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOrder(rs));
                }
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------
    // Ordini di un utente
    // ------------------------------------------------------------
    public List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        String sql = """
                SELECT id, customer_id, created_at, status, payment_method, total_amount, notes
                FROM orders WHERE customer_id = ?
                ORDER BY created_at DESC
                """;

        List<Order> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        }
        return list;
    }

    // ------------------------------------------------------------
    // Ordini per stato (utile allo staff)
    // ------------------------------------------------------------
    public List<Order> getOrdersByStatus(OrderStatus status) throws SQLException {
        String sql = """
                SELECT id, customer_id, created_at, status, payment_method, total_amount, notes
                FROM orders WHERE status = ?
                """;

        List<Order> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        }
        return list;
    }


    // ------------------------------------------------------------
    // Ricerca ordini con filtri dinamici
    // ------------------------------------------------------------
    public List<Order> searchOrders(OrderSearchParameters params) throws SQLException {
        OrderSearchParameters criteria = (params != null) ? params : OrderSearchParameters.builder();

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT o.id, o.customer_id, o.created_at,
                                o.status, o.payment_method, o.total_amount, o.notes
                FROM orders o
                """);

        List<Object> bindValues = new ArrayList<>();

        if (criteria.getCategoryId().isPresent()) {
            sql.append("""
                    JOIN order_items oi ON oi.order_id = o.id
                    JOIN dishes d ON d.id = oi.dish_id
                    """);
        }

        sql.append(" WHERE 1=1");

        criteria.getCustomerId().ifPresent(customerId -> {
            sql.append(" AND o.customer_id = ?");
            bindValues.add(customerId);
        });

        criteria.getStatus().ifPresent(status -> {
            sql.append(" AND o.status = ?");
            bindValues.add(status.name());
        });

        criteria.getPaymentMethod().ifPresent(paymentMethod -> {
            sql.append(" AND o.payment_method = ?");
            bindValues.add(paymentMethod.name());
        });

        criteria.getCategoryId().ifPresent(categoryId -> {
            sql.append(" AND d.category_id = ?");
            bindValues.add(categoryId);
        });

        criteria.getStartDate().ifPresent(startDate -> {
            sql.append(" AND o.created_at >= ?");
            bindValues.add(Timestamp.valueOf(startDate.atStartOfDay()));
        });

        criteria.getEndDate().ifPresent(endDate -> {
            sql.append(" AND o.created_at < ?");
            bindValues.add(Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
        });

        criteria.getMinTotalAmount().ifPresent(minTotal -> {
            sql.append(" AND o.total_amount >= ?");
            bindValues.add(minTotal);
        });

        criteria.getMaxTotalAmount().ifPresent(maxTotal -> {
            sql.append(" AND o.total_amount <= ?");
            bindValues.add(maxTotal);
        });

        sql.append(" ORDER BY o.created_at DESC");

        List<Order> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object bindValue : bindValues) {
                if (bindValue instanceof Integer v) {
                    ps.setInt(idx++, v);
                } else if (bindValue instanceof String v) {
                    ps.setString(idx++, v);
                } else if (bindValue instanceof Timestamp v) {
                    ps.setTimestamp(idx++, v);
                } else if (bindValue instanceof java.math.BigDecimal v) {
                    ps.setBigDecimal(idx++, v);
                } else {
                    ps.setObject(idx++, bindValue);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToOrder(rs));
                }
            }
        }

        return list;
    }


    // ------------------------------------------------------------
    // Mapping ResultSet → DomainModel.Order
    // ------------------------------------------------------------
    private Order mapRowToOrder(ResultSet rs) throws SQLException {

        Order order = new Order();

        order.setId(rs.getInt("id"));

        // CUSTOMER solo con id (caricherai i dettagli nel service se servono)
        User customer = new User();
        customer.setId(rs.getInt("customer_id"));
        order.setCustomer(customer);

        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setPaymentMethod(parsePaymentMethod(rs.getString("payment_method"), order.getId()));
        order.setTotalAmount(new Money(rs.getBigDecimal("total_amount")));
        order.setNotes(rs.getString("notes"));

        return order;
    }

    private PaymentMethod parsePaymentMethod(String rawPaymentMethod, int orderId) {
        if (rawPaymentMethod == null || rawPaymentMethod.isBlank()) {
            throw new IllegalArgumentException("Missing payment_method for order id " + orderId);
        }

        String normalized = rawPaymentMethod.trim().toUpperCase();
        if ("CASH".equals(normalized)) {
            return PaymentMethod.IN_LOCO;
        }

        try {
            return PaymentMethod.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported payment_method '" + rawPaymentMethod + "' for order id " + orderId
            );
        }
    }
}