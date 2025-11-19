package ORM;

import DomainModel.reservation.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableDAO extends BaseDAO {

    public void addTable(Table table) throws SQLException {
        String sql = """
                INSERT INTO tables(number, seats, joinable, location, available)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, table.getNumber());
            ps.setInt(2, table.getSeats());
            ps.setBoolean(3, table.isJoinable());
            ps.setString(4, table.getLocation());
            ps.setBoolean(5, table.isAvailable());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    table.setId(keys.getInt(1));
                }
            }
        }
    }

    public Optional<Table> getTableById(int id) throws SQLException {
        String sql = """
                SELECT id, number, seats, joinable, location, available
                FROM tables
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTable(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Table> getTableByNumber(int number) throws SQLException {
        String sql = """
                SELECT id, number, seats, joinable, location, available
                FROM tables
                WHERE number = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, number);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTable(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Table> getAllTables() throws SQLException {
        String sql = """
                SELECT id, number, seats, joinable, location, available
                FROM tables
                ORDER BY number
                """;

        List<Table> tables = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tables.add(mapRowToTable(rs));
            }
        }
        return tables;
    }

    public List<Table> getAvailableTables() throws SQLException {
        String sql = """
                SELECT id, number, seats, joinable, location, available
                FROM tables
                WHERE available = TRUE
                ORDER BY number
                """;

        List<Table> tables = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tables.add(mapRowToTable(rs));
            }
        }
        return tables;
    }

    public void updateTable(Table table) throws SQLException {
        String sql = """
                UPDATE tables
                SET number = ?, seats = ?, joinable = ?, location = ?, available = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, table.getNumber());
            ps.setInt(2, table.getSeats());
            ps.setBoolean(3, table.isJoinable());
            ps.setString(4, table.getLocation());
            ps.setBoolean(5, table.isAvailable());
            ps.setInt(6, table.getId());

            ps.executeUpdate();
        }
    }

    public void setAvailability(int tableId, boolean available) throws SQLException {
        String sql = "UPDATE tables SET available = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, tableId);
            ps.executeUpdate();
        }
    }

    public void deleteTable(int tableId) throws SQLException {
        String sql = "DELETE FROM tables WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            ps.executeUpdate();
        }
    }

    private Table mapRowToTable(ResultSet rs) throws SQLException {
        Table table = new Table();
        table.setId(rs.getInt("id"));
        table.setNumber(rs.getInt("number"));
        table.setSeats(rs.getInt("seats"));
        table.setJoinable(rs.getBoolean("joinable"));
        table.setLocation(rs.getString("location"));
        table.setAvailable(rs.getBoolean("available"));
        return table;
    }
}