package ORM;

import DomainModel.menu.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAO extends BaseDAO {

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------
    public void addCategory(Category category) throws SQLException {
        String sql = """
                INSERT INTO categories(name, description, active)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setBoolean(3, category.isActive());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
        }
    }

    // -------------------------------------------------------
    // READ - by ID
    // -------------------------------------------------------
    public Optional<Category> getCategoryById(int id) throws SQLException {
        String sql = """
                SELECT id, name, description, active
                FROM categories
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCategory(rs));
                }
            }
        }
        return Optional.empty();
    }

    // -------------------------------------------------------
    // READ - all
    // -------------------------------------------------------
    public List<Category> getAllCategories() throws SQLException {
        String sql = """
                SELECT id, name, description, active
                FROM categories
                ORDER BY name
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
        }
        return categories;
    }

    // -------------------------------------------------------
    // READ - only active
    // -------------------------------------------------------
    public List<Category> getActiveCategories() throws SQLException {
        String sql = """
                SELECT id, name, description, active
                FROM categories
                WHERE active = TRUE
                ORDER BY name
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
        }
        return categories;
    }

    // -------------------------------------------------------
    // READ - search by name (LIKE)
    // -------------------------------------------------------
    public List<Category> findByName(String namePart, boolean caseInsensitive) throws SQLException {
        if (namePart == null || namePart.isBlank()) {
            return List.of();
        }

        String operator = caseInsensitive ? "ILIKE" : "LIKE";

        //TODO
        String sql = """ 
            SELECT id, name, description, active
            FROM categories
            WHERE name " + operator + " ?
            ORDER BY name
            """;

        List<Category> categories = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + namePart.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRowToCategory(rs));
                }
            }
        }
        return categories;
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    public void updateCategory(Category category) throws SQLException {
        String sql = """
                UPDATE categories
                SET name = ?, description = ?, active = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setBoolean(3, category.isActive());
            ps.setInt(4, category.getId());

            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------
    public void deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // Activate / Deactivate
    // -------------------------------------------------------
    public void setCategoryActive(int categoryId, boolean active) throws SQLException {
        String sql = "UPDATE categories SET active = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // Mapper ResultSet -> Category
    // -------------------------------------------------------
    private Category mapRowToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setActive(rs.getBoolean("active"));
        return c;
    }
}
