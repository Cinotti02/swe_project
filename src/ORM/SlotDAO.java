package ORM;

import DomainModel.reservation.Slot;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotDAO extends BaseDAO {

    public void addSlot(Slot slot) throws SQLException {
        String sql = """
                INSERT INTO slots(start_time, end_time, closed)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTime(1, Time.valueOf(slot.getStartTime()));
            ps.setTime(2, Time.valueOf(slot.getEndTime()));
            ps.setBoolean(3, slot.isClosed());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    slot.setId(keys.getInt(1));
                }
            }
        }
    }

    public void updateSlot(Slot slot) throws SQLException {
        String sql = """
                UPDATE slots
                SET start_time = ?, end_time = ?, closed = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTime(1, Time.valueOf(slot.getStartTime()));
            ps.setTime(2, Time.valueOf(slot.getEndTime()));
            ps.setBoolean(3, slot.isClosed());
            ps.setInt(4, slot.getId());

            ps.executeUpdate();
        }
    }

    public void setClosed(int slotId, boolean closed) throws SQLException {
        String sql = "UPDATE slots SET closed = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, closed);
            ps.setInt(2, slotId);
            ps.executeUpdate();
        }
    }

    public Optional<Slot> getSlotById(int slotId) throws SQLException {
        String sql = """
                SELECT id, start_time, end_time, closed
                FROM slots
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Slot> getAllSlots() throws SQLException {
        String sql = """
                SELECT id, start_time, end_time, closed
                FROM slots
                ORDER BY start_time
                """;

        List<Slot> slots = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                slots.add(mapRowToSlot(rs));
            }
        }
        return slots;
    }

    public List<Slot> getOpenSlots() throws SQLException {
        String sql = """
                SELECT id, start_time, end_time, closed
                FROM slots
                WHERE closed = FALSE
                ORDER BY start_time
                """;

        List<Slot> slots = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                slots.add(mapRowToSlot(rs));
            }
        }
        return slots;
    }

    public List<Slot> findSlotsContaining(LocalTime time) throws SQLException {
        String sql = """
                SELECT id, start_time, end_time, closed
                FROM slots
                WHERE closed = FALSE
                  AND start_time <= ?
                  AND end_time > ?
                ORDER BY start_time
                """;

        List<Slot> slots = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Time t = Time.valueOf(time);
            ps.setTime(1, t);
            ps.setTime(2, t);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slots.add(mapRowToSlot(rs));
                }
            }
        }
        return slots;
    }

    public void deleteSlot(int slotId) throws SQLException {
        String sql = "DELETE FROM slots WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            ps.executeUpdate();
        }
    }

    private Slot mapRowToSlot(ResultSet rs) throws SQLException {
        Slot slot = new Slot();
        slot.setId(rs.getInt("id"));
        Time start = rs.getTime("start_time");
        Time end = rs.getTime("end_time");
        if (start != null) {
            slot.setStartTime(start.toLocalTime());
        }
        if (end != null) {
            slot.setEndTime(end.toLocalTime());
        }
        slot.setClosed(rs.getBoolean("closed"));
        return slot;
    }
}