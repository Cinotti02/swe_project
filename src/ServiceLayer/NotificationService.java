package ServiceLayer;

import DomainModel.notification.Notification;
import ORM.NotificationDAO;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public List<Notification> listNotificationsForUser(int userId, boolean unreadOnly) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User id non valido");
        }
        return unreadOnly
                ? notificationDAO.getUnreadNotificationsForUser(userId)
                : notificationDAO.getNotificationsForUser(userId);
    }

    public void markAsRead(int notificationId) throws SQLException {
        if (notificationId <= 0) {
            throw new IllegalArgumentException("Notification id non valido");
        }
        notificationDAO.markAsRead(notificationId);
    }
}
