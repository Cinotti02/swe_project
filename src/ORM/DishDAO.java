package ORM;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.search.DishSearchParameters;
import DomainModel.valueObject.Money;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DishDAO extends BaseDAO{

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------
    public void addDish(Dish dish) throws SQLException {
        String sql = """
                INSERT INTO dishes(name, description, price, active, category_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, dish.getName());
            ps.setString(2, dish.getDescription());
            ps.setBigDecimal(3, dish.getPrice().getAmount());
            ps.setBoolean(4, dish.isAvailable());

            if (dish.getCategory() != null)
                ps.setInt(5, dish.getCategory().getId());
            else
                ps.setNull(5, Types.INTEGER);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) dish.setId(keys.getInt(1));
            }
        }
    }

    // -------------------------------------------------------
    // READ - get by ID
    // -------------------------------------------------------
    public Optional<Dish> getDishById(int id) throws SQLException {
        String sql = """
                SELECT id, name, description, price, active, category_id
                FROM dishes
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRowToDish(rs));
            }
        }
        return Optional.empty();
    }

    // -------------------------------------------------------
    // READ - all dishes
    // -------------------------------------------------------
    public List<Dish> getAllDishes() throws SQLException {
        String sql = """
                SELECT id, name, description, price, active, category_id
                FROM dishes
                ORDER BY name
                """;

        List<Dish> list = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToDish(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------
    // READ - by category
    // -------------------------------------------------------
    public List<Dish> getDishesByCategory(int categoryId) throws SQLException {
        String sql = """
                SELECT id, name, description, price, active, category_id
                FROM dishes
                WHERE category_id = ?
                ORDER BY name
                """;

        List<Dish> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDish(rs));
                }
            }
        }
        return list;
    }

    // -------------------------------------------------------
    // READ - dynamic search
    // -------------------------------------------------------
    public List<Dish> searchDishes(DishSearchParameters params) throws SQLException {
        DishSearchParameters criteria = (params != null) ? params : DishSearchParameters.builder();

        StringBuilder sql = new StringBuilder("""
                SELECT id, name, description, price, active, category_id
                FROM dishes
                WHERE 1=1
                """);
        List<Object> bindValues = new ArrayList<>();

        criteria.getCategoryId().ifPresent(categoryId -> {
            sql.append(" AND category_id = ?");
            bindValues.add(categoryId);
        });

        criteria.getOnlyAvailable().ifPresent(onlyAvailable -> {
            if (onlyAvailable) {
                sql.append(" AND active = TRUE");
            }
        });

        criteria.getMinPrice().ifPresent(minPrice -> {
            sql.append(" AND price >= ?");
            bindValues.add(minPrice);
        });

        criteria.getMaxPrice().ifPresent(maxPrice -> {
            sql.append(" AND price <= ?");
            bindValues.add(maxPrice);
        });

        criteria.getNameContains()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .ifPresent(name -> {
                    sql.append(" AND LOWER(name) LIKE ?");
                    bindValues.add("%" + name.toLowerCase() + "%");
                });

        sql.append(" ORDER BY name");

        List<Dish> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object bindValue : bindValues) {
                if (bindValue instanceof Integer v) {
                    ps.setInt(idx++, v);
                } else if (bindValue instanceof java.math.BigDecimal v) {
                    ps.setBigDecimal(idx++, v);
                } else if (bindValue instanceof String v) {
                    ps.setString(idx++, v);
                } else {
                    ps.setObject(idx++, bindValue);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDish(rs));
                }
            }
        }

        return list;
    }


    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    public void updateDish(Dish dish) throws SQLException {
        String sql = """
                UPDATE dishes
                SET name = ?, description = ?, price = ?, active = ?, category_id = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dish.getName());
            ps.setString(2, dish.getDescription());
            ps.setBigDecimal(3, dish.getPrice().getAmount());
            ps.setBoolean(4, dish.isAvailable());

            if (dish.getCategory() != null)
                ps.setInt(5, dish.getCategory().getId());
            else
                ps.setNull(5, Types.INTEGER);

            ps.setInt(6, dish.getId());

            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------
    public void deleteDish(int dishId) throws SQLException {
        String sql = "DELETE FROM dishes WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // Activate / Deactivate
    // -------------------------------------------------------
    public void setDishActive(int dishId, boolean active) throws SQLException {
        String sql = "UPDATE dishes SET active = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, dishId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // Row mapper
    // -------------------------------------------------------
    private Dish mapRowToDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();

        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDescription(rs.getString("description"));
        dish.setPrice(new Money(rs.getBigDecimal("price")));
        dish.setAvailable(rs.getBoolean("active"));

        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            Category c = new Category();
            c.setId(categoryId);
            dish.setCategory(c);
        }

        return dish;
    }
}