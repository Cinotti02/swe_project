package ServiceLayer;

import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.reservation.Reservation;
import ORM.NotificationDAO;
import ORM.OrderDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StaffOperationService {

    private final OrderDAO orderDAO;
    private final ReservationService reservationService;
    private final NotificationDAO notificationDAO;

    public StaffOperationService(OrderDAO orderDAO,
                                 ReservationService reservationService,
                                 NotificationDAO notificationDAO) {
        this.orderDAO = orderDAO;
        this.reservationService = reservationService;
        this.notificationDAO = notificationDAO;
    }

    public List<Order> listOrdersByStatus(OrderStatus status) throws SQLException {
        return orderDAO.getOrdersByStatus(status);
    }

    public void markOrderPreparing(int orderId) throws SQLException {
        setOrderStatus(orderId, OrderStatus.PREPARING, "Il tuo ordine è in preparazione");
    }

    public void markOrderReady(int orderId) throws SQLException {
        setOrderStatus(orderId, OrderStatus.READY, "Il tuo ordine è pronto per il ritiro");
    }

    public void markOrderRetired(int orderId) throws SQLException {
        setOrderStatus(orderId, OrderStatus.RETIRED, "Ordine consegnato, grazie!");
    }

    public void cancelOrder(int orderId) throws SQLException {
        setOrderStatus(orderId, OrderStatus.CANCELLED, "Il tuo ordine è stato annullato");
    }

    public List<Reservation> reservationsForDate(LocalDate date) throws SQLException {
        return reservationService.listReservationsByDate(date);
    }

    public void confirmReservation(int reservationId) throws SQLException {
        reservationService.confirmReservation(reservationId);
    }

    public void registerCheckIn(int reservationId) throws SQLException {
        reservationService.checkInReservation(reservationId);
    }

    public void registerNoShow(int reservationId) throws SQLException {
        reservationService.markNoShow(reservationId);
    }

    private void setOrderStatus(int orderId, OrderStatus newStatus, String message) throws SQLException {
        orderDAO.updateStatus(orderId, newStatus);
        notifyCustomer(orderId, message,
                newStatus == OrderStatus.READY ? TypeNotification.ALERT : TypeNotification.UPDATE);
    }

    private void notifyCustomer(int orderId, String message, TypeNotification type) throws SQLException {
        Order order = orderDAO.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Notification notification = new Notification(order.getCustomer(), message, type);
        notificationDAO.addNotification(notification);
    }
}