package ServiceLayer;

import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import ORM.NotificationDAO;
import ORM.OrderDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<Order> listOrdersByDate(LocalDate date) throws SQLException {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }

        List<Order> all = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            all.addAll(orderDAO.getOrdersByStatus(status));
        }

        return all.stream()
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> order.getCreatedAt().toLocalDate().equals(date))
                .toList();
    }

    public void updateOrderStatus(int orderId, OrderStatus newStatus) throws SQLException {
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = orderDAO.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                    "Transizione non valida: " + currentStatus + " -> " + newStatus
            );
        }

        setOrderStatus(orderId, newStatus, messageForStatus(newStatus));
    }

    public void markOrderPreparing(int orderId) throws SQLException {
        updateOrderStatus(orderId, OrderStatus.PREPARING);
    }

    public void markOrderReady(int orderId) throws SQLException {
        updateOrderStatus(orderId, OrderStatus.READY);
    }

    public void markOrderRetired(int orderId) throws SQLException {
        updateOrderStatus(orderId, OrderStatus.RETIRED);
    }

    public void cancelOrder(int orderId) throws SQLException {
        updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }


    public Reservation getReservationById(int reservationId) throws SQLException {
        return reservationService.getReservation(reservationId);
    }

    public void updateReservationStatus(int reservationId, ReservationStatus newStatus) throws SQLException {
        if (newStatus == null) {
            throw new IllegalArgumentException("Reservation status is required");
        }

        switch (newStatus) {
            case CONFIRMED -> reservationService.confirmReservation(reservationId);
            case CHECKED_IN -> reservationService.checkInReservation(reservationId);
            case COMPLETED -> reservationService.completeReservation(reservationId);
            case NO_SHOW -> reservationService.markNoShow(reservationId);
            default -> throw new IllegalArgumentException("Transizione non supportata verso " + newStatus + " per staff");
        }
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

    private String messageForStatus(OrderStatus status) {
        return switch (status) {
            case PREPARING -> "Il tuo ordine è in preparazione";
            case READY -> "Il tuo ordine è pronto per il ritiro";
            case RETIRED -> "Ordine consegnato, grazie!";
            case CANCELLED -> "Il tuo ordine è stato annullato";
            case CREATED -> "Il tuo ordine è stato registrato";
        };
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