package ORM;

import DomainModel.reservation.MergeTable;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.user.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDAO extends BaseDAO {

    public void addReservation(Reservation reservation) throws SQLException {
        String sql = """
                INSERT INTO reservations(customer_id, guests, reservation_date, slot_id, status, notes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reservation.getCustomer().getId());
            ps.setInt(2, reservation.getNumberOfGuests());
            ps.setDate(3, Date.valueOf(reservation.getReservDate().toLocalDate()));
            ps.setInt(4, reservation.getTimeSlot().getId());
            ps.setString(5, reservation.getStatus().name());
            ps.setString(6, reservation.getNotes());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reservation.setId(keys.getInt(1));
                }
            }
        }
    }

    public void updateReservation(Reservation reservation) throws SQLException {
        String sql = """
                UPDATE reservations
                SET guests = ?, reservation_date = ?, slot_id = ?, status = ?, notes = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservation.getNumberOfGuests());
            ps.setDate(2, Date.valueOf(reservation.getReservDate().toLocalDate()));
            ps.setInt(3, reservation.getTimeSlot().getId());
            ps.setString(4, reservation.getStatus().name());
            ps.setString(5, reservation.getNotes());
            ps.setInt(6, reservation.getId());

            ps.executeUpdate();
        }
    }

    public void updateStatus(int reservationId, ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    public Optional<Reservation> getReservationById(int reservationId) throws SQLException {
        String sql = baseReservationSelect("WHERE r.id = ?");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = mapRowToReservation(rs);
                    reservation.setTables(getTableAssignments(reservationId));
                    return Optional.of(reservation);
                }
            }
        }
        return Optional.empty();
    }

    public List<Reservation> getReservationsByCustomer(int customerId) throws SQLException {
        String sql = baseReservationSelect("WHERE r.customer_id = ? ORDER BY r.reservation_date");

        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        }
        return reservations;
    }

    public List<Reservation> getReservationsByDate(LocalDate date) throws SQLException {
        String sql = baseReservationSelect("WHERE r.reservation_date = ? ORDER BY s.start_time");

        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        }
        return reservations;
    }

    public List<Reservation> searchReservations(ReservationSearchParameters params) throws SQLException {
        ReservationSearchParameters criteria = (params != null) ? params : ReservationSearchParameters.builder();

        StringBuilder where = new StringBuilder("WHERE 1=1");
        List<Object> bindValues = new ArrayList<>();

        criteria.getDate().ifPresent(date -> {
            where.append(" AND r.reservation_date = ?");
            bindValues.add(Date.valueOf(date));
        });

        criteria.getStartDate().ifPresent(startDate -> {
            where.append(" AND r.reservation_date >= ?");
            bindValues.add(Date.valueOf(startDate));
        });

        criteria.getEndDate().ifPresent(endDate -> {
            where.append(" AND r.reservation_date <= ?");
            bindValues.add(Date.valueOf(endDate));
        });

        criteria.getCustomerId().ifPresent(customerId -> {
            where.append(" AND r.customer_id = ?");
            bindValues.add(customerId);
        });

        criteria.getSlotId().ifPresent(slotId -> {
            where.append(" AND r.slot_id = ?");
            bindValues.add(slotId);
        });

        criteria.getMinGuests().ifPresent(minGuests -> {
            where.append(" AND r.guests >= ?");
            bindValues.add(minGuests);
        });

        criteria.getMaxGuests().ifPresent(maxGuests -> {
            where.append(" AND r.guests <= ?");
            bindValues.add(maxGuests);
        });

        criteria.getStatus().ifPresent(status -> {
            where.append(" AND r.status = ?");
            bindValues.add(status.name());
        });

        where.append(" ORDER BY r.reservation_date, s.start_time");

        String sql = baseReservationSelect(where.toString());
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            for (Object bindValue : bindValues) {
                if (bindValue instanceof Integer v) {
                    ps.setInt(idx++, v);
                } else if (bindValue instanceof String v) {
                    ps.setString(idx++, v);
                } else if (bindValue instanceof Date v) {
                    ps.setDate(idx++, v);
                } else {
                    ps.setObject(idx++, bindValue);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        }

        return reservations;
    }

    public void deleteReservation(int reservationId) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    public List<Integer> getReservedTableIds(LocalDate date, int slotId) throws SQLException {
        String sql = """
                SELECT mt.table_id
                FROM merge_tables mt
                JOIN reservations r ON mt.reservation_id = r.id
                WHERE r.reservation_date = ?
                  AND r.slot_id = ?
                  AND r.status NOT IN ('CANCELED', 'NO_SHOW', 'COMPLETED')
                """;

        List<Integer> ids = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, slotId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("table_id"));
                }
            }
        }
        return ids;
    }

    public void saveTableAssignments(List<MergeTable> assignments) throws SQLException {
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        String deleteSql = "DELETE FROM merge_tables WHERE reservation_id = ?";
        String insertSql = """
                INSERT INTO merge_tables(reservation_id, table_id, seats_assigned, merged_group_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement delete = conn.prepareStatement(deleteSql)) {
                    delete.setInt(1, assignments.get(0).getReservation().getId());
                    delete.executeUpdate();
                }

                try (PreparedStatement insert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (MergeTable assignment : assignments) {
                        insert.setInt(1, assignment.getReservation().getId());
                        insert.setInt(2, assignment.getTable().getId());
                        insert.setInt(3, assignment.getSeatsAssigned());
                        insert.setString(4, assignment.getMergedGroupId());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<MergeTable> getTableAssignments(int reservationId) throws SQLException {
        String sql = """
                SELECT mt.id, mt.table_id, mt.seats_assigned, mt.merged_group_id,
                       t.number, t.seats, t.joinable, t.location, t.available
                FROM merge_tables mt
                JOIN tables t ON t.id = mt.table_id
                WHERE mt.reservation_id = ?
                ORDER BY t.number
                """;

        List<MergeTable> assignments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapRowToMergeTable(rs, reservationId));
                }
            }
        }
        return assignments;
    }

    private String baseReservationSelect(String whereClause) {
        return """
                SELECT r.id, r.customer_id, r.guests, r.reservation_date,
                       r.slot_id, r.status, r.notes,
                       s.start_time AS slot_start, s.end_time AS slot_end, s.closed AS slot_closed
                FROM reservations r
                JOIN slots s ON s.id = r.slot_id
                """ + " " + whereClause;
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));

        User customer = new User();
        customer.setId(rs.getInt("customer_id"));
        reservation.setCustomer(customer);

        reservation.setNumberOfGuests(rs.getInt("guests"));

        LocalDate date = rs.getDate("reservation_date").toLocalDate();
        Time slotStartTime = rs.getTime("slot_start");
        LocalTime slotStart = slotStartTime != null ? slotStartTime.toLocalTime() : LocalTime.MIDNIGHT;

        Slot slot = new Slot();
        slot.setId(rs.getInt("slot_id"));
        slot.setStartTime(slotStart);
        Time slotEndTime = rs.getTime("slot_end");
        if (slotEndTime != null) {
            slot.setEndTime(slotEndTime.toLocalTime());
        }
        slot.setClosed(rs.getBoolean("slot_closed"));
        reservation.setTimeSlot(slot);

        reservation.setReservDate(LocalDateTime.of(date, slotStart));
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        reservation.setNotes(rs.getString("notes"));

        return reservation;
    }

    private MergeTable mapRowToMergeTable(ResultSet rs, int reservationId) throws SQLException {
        MergeTable mergeTable = new MergeTable();
        mergeTable.setId(rs.getInt("id"));

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        mergeTable.setReservation(reservation);

        Table table = new Table();
        table.setId(rs.getInt("table_id"));
        table.setNumber(rs.getInt("number"));
        table.setSeats(rs.getInt("seats"));
        table.setJoinable(rs.getBoolean("joinable"));
        table.setLocation(rs.getString("location"));
        table.setAvailable(rs.getBoolean("available"));
        mergeTable.setTable(table);

        mergeTable.setSeatsAssigned(rs.getInt("seats_assigned"));
        mergeTable.setMergedGroupId(rs.getString("merged_group_id"));

        return mergeTable;
    }
}