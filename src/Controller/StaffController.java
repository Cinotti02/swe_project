package Controller;

import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.reservation.Reservation;
import ServiceLayer.StaffOperationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StaffController {
    private final StaffOperationService staffOperationService;

    public StaffController(StaffOperationService staffOperationService) {
        this.staffOperationService = staffOperationService;
    }

    public void showKitchenQueue() {
        try {
            List<Order> pending = staffOperationService.listOrdersByStatus(OrderStatus.CREATED);
            List<Order> preparing = staffOperationService.listOrdersByStatus(OrderStatus.PREPARING);
            System.out.println("=== DA PREPARARE ===");
            pending.forEach(order -> System.out.println("#" + order.getId() + " di " + order.getCustomer().getId()));
            System.out.println("=== IN PREPARAZIONE ===");
            preparing.forEach(order -> System.out.println("#" + order.getId() + " di " + order.getCustomer().getId()));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare la coda cucina: " + e.getMessage());
        }
    }

    public void markOrderReady(int orderId) {
        try {
            staffOperationService.markOrderReady(orderId);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel cambio stato ordine: " + e.getMessage());
        }
    }

    public void markOrderRetired(int orderId) {
        try {
            staffOperationService.markOrderRetired(orderId);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel cambio stato ordine: " + e.getMessage());
        }
    }

    public void showReservations(LocalDate date) {
        try {
            List<Reservation> reservations = staffOperationService.reservationsForDate(date);
            System.out.println("=== Prenotazioni per " + date + " ===");
            for (Reservation reservation : reservations) {
                System.out.println("#" + reservation.getId() + " ospiti:" + reservation.getNumberOfGuests()
                        + " slot:" + reservation.getTimeSlot().getStartTime() + " stato:" + reservation.getStatus());
            }
        } catch (SQLException e) {
            System.err.println("Impossibile caricare le prenotazioni: " + e.getMessage());
        }
    }

    public void confirmReservation(int reservationId) {
        try {
            staffOperationService.confirmReservation(reservationId);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nella conferma: " + e.getMessage());
        }
    }

    public void registerCheckIn(int reservationId) {
        try {
            staffOperationService.registerCheckIn(reservationId);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel check-in: " + e.getMessage());
        }
    }

    public void markNoShow(int reservationId) {
        try {
            staffOperationService.registerNoShow(reservationId);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nella segnalazione no-show: " + e.getMessage());
        }
    }
}