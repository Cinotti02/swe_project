package ORM;

import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
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
                INSERT INTO orders(customer_id, reservation_id, created_at, status, payment_method, total_amount, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            ps.setInt(1, order.getCustomer().getId());

            if (order.getReservation() != null)
                ps.setInt(2, order.getReservation().getId());
            else
                ps.setNull(2, Types.INTEGER);

            ps.setTimestamp(3, Timestamp.valueOf(order.getCreatedAt()));
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getPaymentMethod().name());
            ps.setBigDecimal(6, order.getTotalAmount().getAmount());
            ps.setString(7, order.getNotes());

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

        try (Connection conn = DBConnection.getInstance().getConnection();
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

        try (Connection conn = DBConnection.getInstance().getConnection();
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

        try (Connection conn = DBConnection.getInstance().getConnection();
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
                SELECT id, customer_id, reservation_id, created_at, status, payment_method, total_amount, notes
                FROM orders WHERE id = ?
                """;

        try (Connection conn = DBConnection.getInstance().getConnection();
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
                SELECT id, customer_id, reservation_id, created_at, status, payment_method, total_amount, notes
                FROM orders WHERE customer_id = ?
                ORDER BY created_at DESC
                """;

        List<Order> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
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
                SELECT id, customer_id, reservation_id, created_at, status, payment_method, total_amount, notes
                FROM orders WHERE status = ?
                """;

        List<Order> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
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
    // Mapping ResultSet → DomainModel.Order
    // ------------------------------------------------------------
    private Order mapRowToOrder(ResultSet rs) throws SQLException {

        Order order = new Order();

        order.setId(rs.getInt("id"));

        // CUSTOMER solo con id (caricherai i dettagli nel service se servono)
        User customer = new User();
        customer.setId(rs.getInt("customer_id"));
        order.setCustomer(customer);

        // Reservation opzionale
        int resId = rs.getInt("reservation_id");
        if (!rs.wasNull()) {
            Reservation r = new Reservation();
            r.setId(resId);
            order.setReservation(r);
        }

        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        order.setTotalAmount(new Money(rs.getBigDecimal("total_amount")));
        order.setNotes(rs.getString("notes"));

        return order;
    }
}