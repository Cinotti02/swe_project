package ORM;

import DomainModel.menu.Dish;
import DomainModel.order.OrderItem;
import DomainModel.valueObject.Money;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO extends BaseDAO {

    /** ----------------------------------------------------
     *  addOrderItem()
     *  Salva una riga dell’ordine nel DB.
     *  ---------------------------------------------------- */
    public void addOrderItem(int orderId, OrderItem item) throws SQLException {
        String sql = """
                INSERT INTO order_items(order_id, dish_id, unit_price, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderId);
            ps.setInt(2, item.getDish().getId());
            ps.setBigDecimal(3, item.getUnitPrice().getAmount());
            ps.setInt(4, item.getQuantity());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));  // assegna id dell’item
                }
            }
        }
    }

    /** ----------------------------------------------------
     *  getItemsByOrder(): List<OrderItem>
     *  Recupera tutte le righe di un ordine.
     *  ---------------------------------------------------- */
    public List<OrderItem> getItemsByOrder(int orderId) throws SQLException {
        String sql = """
                SELECT id, dish_id, unit_price, quantity, total_price
                FROM order_items
                WHERE order_id = ?
                """;

        List<OrderItem> items = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToOrderItem(rs));
                }
            }
        }
        return items;
    }

    /** ----------------------------------------------------
     *  deleteItemsByOrder()
     *  Cancella tutte le righe associate ad un ordine
     *  ---------------------------------------------------- */
    public void deleteItemsByOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    /** ----------------------------------------------------
     *  Mapper: ResultSet → OrderItem
     *  ---------------------------------------------------- */
    private OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();

        item.setId(rs.getInt("id"));

        // dish_id → recupero il Dish tramite DishDAO
        Dish dish = new Dish();
        dish.setId(rs.getInt("dish_id"));
        item.setDish(dish);

        item.setUnitPrice(new Money(rs.getBigDecimal("unit_price")));
        item.setQuantity(rs.getInt("quantity"));

        return item;
    }
}