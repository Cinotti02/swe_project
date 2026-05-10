package ServiceLayer;

import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.user.User;
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


    public void notifyUser(int userId, String message, TypeNotification type) throws SQLException {
        if (userId <= 0) throw new IllegalArgumentException("User id non valido");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Messaggio notifica vuoto");
        if (type == null) throw new IllegalArgumentException("Tipo notifica nullo");

        User recipient = new User();
        recipient.setId(userId);
        Notification notification = new Notification(recipient, message, type);
        notificationDAO.addNotification(notification);
    }

    public void markAsRead(int notificationId) throws SQLException {
        if (notificationId <= 0) {
            throw new IllegalArgumentException("Notification id non valido");
        }
        notificationDAO.markAsRead(notificationId);
    }
}
