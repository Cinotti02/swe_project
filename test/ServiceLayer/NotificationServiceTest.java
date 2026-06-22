package ServiceLayer;

import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import ORM.NotificationDAO;
import ServiceLayer.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private FakeNotificationDAO dao;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        dao = new FakeNotificationDAO();
        service = new NotificationService(dao);
    }

    @Test
    void listSelectsAllOrUnreadQuery() throws Exception {
        assertSame(dao.all, service.listNotificationsForUser(3, false));
        assertSame(dao.unread, service.listNotificationsForUser(3, true));
        assertEquals(3, dao.lastUserId);
    }

    @Test
    void notifyUserBuildsValidNotification() throws Exception {
        service.notifyUser(7, "Ordine pronto", TypeNotification.ALERT);

        assertNotNull(dao.added);
        assertEquals(7, dao.added.getRecipient().getId());
        assertEquals("Ordine pronto", dao.added.getMessage());
        assertEquals(TypeNotification.ALERT, dao.added.getType());
    }

    @Test
    void invalidInputsAreRejectedWithoutCallingDao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listNotificationsForUser(0, false));
        assertThrows(IllegalArgumentException.class,
                () -> service.notifyUser(1, " ", TypeNotification.UPDATE));
        assertThrows(IllegalArgumentException.class,
                () -> service.markAsRead(-1));
        assertNull(dao.added);
        assertNull(dao.markedId);
    }

    @Test
    void markAsReadDelegatesValidatedId() throws Exception {
        service.markAsRead(12);
        assertEquals(12, dao.markedId);
    }

    private static class FakeNotificationDAO extends NotificationDAO {
        private final List<Notification> all = List.of(new Notification());
        private final List<Notification> unread = List.of();
        private int lastUserId;
        private Notification added;
        private Integer markedId;

        @Override
        public List<Notification> getNotificationsForUser(int userId) {
            lastUserId = userId;
            return all;
        }

        @Override
        public List<Notification> getUnreadNotificationsForUser(int userId) {
            lastUserId = userId;
            return unread;
        }

        @Override
        public void addNotification(Notification notification) {
            added = notification;
        }

        @Override
        public void markAsRead(int notificationId) {
            markedId = notificationId;
        }
    }
}
