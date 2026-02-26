package ORM;


import DomainModel.notification.Notification;
import DomainModel.notification.StatusNotification;
import DomainModel.notification.TypeNotification;
import DomainModel.user.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO extends BaseDAO {

    public void addNotification(Notification notification) throws SQLException {
        String sql = """
                INSERT INTO notifications(recipient_id, message, type, status, created_at, read_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notification.getRecipient().getId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getStatus().name());

            LocalDateTime createdAt = notification.getCreatedAt() != null
                    ? notification.getCreatedAt()
                    : LocalDateTime.now();
            notification.setCreatedAt(createdAt);
            ps.setTimestamp(5, Timestamp.valueOf(createdAt));

            if (notification.getReadAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(notification.getReadAt()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setId(keys.getInt(1));
                }
            }
        }
    }

    public java.util.Optional<Notification> getNotificationById(int notificationId) throws SQLException {
        String sql = """
                SELECT id, recipient_id, message, type, status, created_at, read_at
                FROM notifications
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return java.util.Optional.of(mapRowToNotification(rs));
                }
            }
        }
        return java.util.Optional.empty();
    }

    public List<Notification> getNotificationsForUser(int userId) throws SQLException {
        String sql = """
                SELECT id, recipient_id, message, type, status, created_at, read_at
                FROM notifications
                WHERE recipient_id = ?
                ORDER BY created_at DESC
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRowToNotification(rs));
                }
            }
        }
        return notifications;
    }

    public List<Notification> getUnreadNotificationsForUser(int userId) throws SQLException {
        String sql = """
                SELECT id, recipient_id, message, type, status, created_at, read_at
                FROM notifications
                WHERE recipient_id = ? AND status <> 'READ'
                ORDER BY created_at DESC
                """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRowToNotification(rs));
                }
            }
        }
        return notifications;
    }

    public void markAsRead(int notificationId) throws SQLException {
        Notification notification = getNotificationById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.markRead();
        updateNotificationState(notification);
    }

    public void markAsFailed(int notificationId) throws SQLException {
        Notification notification = getNotificationById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.markFailed();
        updateNotificationState(notification);
    }

    private void updateNotificationState(Notification notification) throws SQLException {
        String sql = """
                UPDATE notifications
                SET status = ?, read_at = ?
                WHERE id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, notification.getStatus().name());
            if (notification.getReadAt() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(notification.getReadAt()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, notification.getId());
            ps.executeUpdate();
        }
    }

    public void deleteNotification(int notificationId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.executeUpdate();
        }
    }

    private Notification mapRowToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));

        User recipient = new User();
        recipient.setId(rs.getInt("recipient_id"));
        notification.setRecipient(recipient);

        notification.setMessage(rs.getString("message"));
        notification.setType(TypeNotification.valueOf(rs.getString("type")));
        notification.setStatus(StatusNotification.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) {
            notification.setReadAt(readAt.toLocalDateTime());
        }

        return notification;
    }
}