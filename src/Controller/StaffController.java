package Controller;

import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.search.SearchCriteria;
import ServiceLayer.NotificationService;
import ServiceLayer.SearchService;
import ServiceLayer.StaffOperationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StaffController {

    private final StaffOperationService staffOperationService;
    private final SearchService searchService;
    private final NotificationService notificationService;

    public StaffController(StaffOperationService staffOperationService,
                           SearchService searchService,
                           NotificationService notificationService) {
        this.staffOperationService = staffOperationService;
        this.searchService = searchService;
        this.notificationService = notificationService;
    }

    public List<Notification> getNotifications(int userId, boolean unreadOnly) throws SQLException {
        return notificationService.listNotificationsForUser(userId, unreadOnly);
    }

    public void markNotificationAsRead(int notificationId) throws SQLException {
        notificationService.markAsRead(notificationId);
    }

    public Map<OrderStatus, List<Order>> getKitchenQueue() throws SQLException {
        Map<OrderStatus, List<Order>> queue = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            queue.put(status, staffOperationService.listOrdersByStatus(status));
        }
        return queue;
    }

    public void updateOrderStatus(int orderId,
                                  OrderStatus nextStatus,
                                  int staffUserId) throws SQLException {
        staffOperationService.updateOrderStatus(orderId, nextStatus);
        notificationService.notifyUser(
                staffUserId,
                "Stato ordine #" + orderId + " aggiornato a " + nextStatus,
                TypeNotification.UPDATE
        );
    }

    public List<Reservation> getReservations(LocalDate date) throws SQLException {
        return staffOperationService.reservationsForDate(date);
    }

    public void confirmReservation(int reservationId) throws SQLException {
        staffOperationService.confirmReservation(reservationId);
    }

    public void registerCheckIn(int reservationId) throws SQLException {
        staffOperationService.registerCheckIn(reservationId);
    }

    public void markNoShow(int reservationId) throws SQLException {
        staffOperationService.registerNoShow(reservationId);
    }

    public List<Order> searchOrders(Integer customerId,
                                    OrderStatus status,
                                    PaymentMethod paymentMethod,
                                    Integer categoryId,
                                    LocalDate startDate,
                                    LocalDate endDate) throws SQLException {
        OrderSearchParameters params = OrderSearchParameters.builder();
        if (customerId != null) params.setCustomerId(customerId);
        if (status != null) params.setStatus(status);
        if (paymentMethod != null) params.setPaymentMethod(paymentMethod);
        if (categoryId != null) params.setCategoryId(categoryId);
        if (startDate != null) params.setStartDate(startDate);
        if (endDate != null) params.setEndDate(endDate);
        return searchService.search(SearchCriteria.builder().setOrder(params)).getOrders();
    }

    public List<Reservation> searchReservations(LocalDate date,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                Integer customerId,
                                                Integer slotId,
                                                Integer minGuests,
                                                Integer maxGuests,
                                                ReservationStatus status) throws SQLException {
        ReservationSearchParameters params = ReservationSearchParameters.builder();
        if (date != null) params.setDate(date);
        if (startDate != null) params.setStartDate(startDate);
        if (endDate != null) params.setEndDate(endDate);
        if (customerId != null) params.setCustomerId(customerId);
        if (slotId != null) params.setSlotId(slotId);
        if (minGuests != null) params.setMinGuests(minGuests);
        if (maxGuests != null) params.setMaxGuests(maxGuests);
        if (status != null) params.setStatus(status);
        return searchService.search(SearchCriteria.builder().setReservation(params)).getReservations();
    }
}
