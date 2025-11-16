package ORM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import DomainModel.user.User;
import DomainModel.user.Role;
import DomainModel.valueObject.Email;

public class UserDAO {

    // Ottieni la connessione (sostituisci con il tuo ConnectionManager / DataSource)
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/dineup",
                "username",
                "password"
        );
    }

    // ----------------------------------------------------
    // addUser()
    // ----------------------------------------------------
    public void addUser(User user) throws SQLException {
        String sql = """
                INSERT INTO users(username, email, password_hash, fidality_points, name, surname, role)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmailValue());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getFidalityPoints());
            ps.setString(5, user.getName());
            ps.setString(6, user.getSurname());
            ps.setString(7, user.getRole().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }
    }

    // ----------------------------------------------------
    // removeUser()
    // ----------------------------------------------------
    public void removeUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------
    // getUserById(): Optional<User>
    // ----------------------------------------------------
    public Optional<User> getUserById(int id) throws SQLException {
        String sql = """
                SELECT id, username, email, password_hash, fidality_points,
                       name, surname, role
                FROM users
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    // ----------------------------------------------------
    // getUserByEmail(): Optional<User>
    // ----------------------------------------------------
    public Optional<User> getUserByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, username, email, password_hash, fidality_points,
                       name, surname, role
                FROM users
                WHERE email = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    // ----------------------------------------------------
    // getUsersByName(): List<User>
    // (puoi decidere se cercare per name, surname o entrambi)
    // ----------------------------------------------------
    public List<User> getUsersByName(String name) throws SQLException {
        String sql = """
                SELECT id, username, email, password_hash, fidality_points,
                       name, surname, role
                FROM users
                WHERE name LIKE ?
                """;

        List<User> result = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToUser(rs));
                }
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // getAllUsers(): List<User>
    // ----------------------------------------------------
    public List<User> getAllUsers() throws SQLException {
        String sql = """
                SELECT id, username, email, password_hash, fidality_points,
                       name, surname, role
                FROM users
                """;

        List<User> result = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRowToUser(rs));
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // updateUser()
    // ----------------------------------------------------
    public void updateUser(User user) throws SQLException {
        String sql = """
                UPDATE users
                SET username = ?, email = ?, password_hash = ?, fidality_points = ?,
                    name = ?, surname = ?, role = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmailValue());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getFidalityPoints());
            ps.setString(5, user.getName());
            ps.setString(6, user.getSurname());
            ps.setString(7, user.getRole().name());
            ps.setInt(8, user.getId());

            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------
    // emailExists(): boolean
    // ----------------------------------------------------
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------------------------------
    // mapper ResultSet -> User
    // ----------------------------------------------------
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFidalityPoints(rs.getInt("fidality_points"));
        u.setName(rs.getString("name"));
        u.setSurname(rs.getString("surname"));

        String emailStr = rs.getString("email");
        if (emailStr != null) {
            u.setEmail(new Email(emailStr));
        }

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            u.setRole(Role.valueOf(roleStr)); // CUSTOMER / STAFF / OWNER
        }

        return u;
    }
}