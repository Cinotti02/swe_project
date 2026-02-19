package Controller;

import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
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
            System.out.println("\n=== CODA CUCINA ===");

            System.out.println("\n== ORDINI ATTIVI ==");
            printOrderBucket(labelFor(OrderStatus.CREATED), staffOperationService.listOrdersByStatus(OrderStatus.CREATED));
            printOrderBucket(labelFor(OrderStatus.PREPARING), staffOperationService.listOrdersByStatus(OrderStatus.PREPARING));
            printOrderBucket(labelFor(OrderStatus.READY), staffOperationService.listOrdersByStatus(OrderStatus.READY));

            System.out.println("\n== ORDINI CONCLUSI ==");
            printOrderBucket(labelFor(OrderStatus.RETIRED), staffOperationService.listOrdersByStatus(OrderStatus.RETIRED));
            printOrderBucket(labelFor(OrderStatus.CANCELLED), staffOperationService.listOrdersByStatus(OrderStatus.CANCELLED));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare la coda cucina: " + e.getMessage());
        }
    }

    private String labelFor(OrderStatus status) {
        return switch (status) {
            case CREATED -> "CREATI (DA PREPARARE)";
            case PREPARING -> "IN PREPARAZIONE";
            case READY -> "PRONTI";
            case RETIRED -> "RITIRATI";
            case CANCELLED -> "ANNULLATI";
        };
    }

    private void printOrderBucket(String title, List<Order> orders) {
        System.out.println("\n--- " + title + " ---");

        if (orders.isEmpty()) {
            System.out.println("(nessun ordine)");
            return;
        }

        System.out.printf("%-8s %-12s %-10s %-12s %-20s%n", "ID", "Cliente", "Pagamento", "Totale", "Creato il");
        for (Order order : orders) {
            String payment = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "-";
            String total = order.getTotalAmount() != null ? order.getTotalAmount().toString() : "-";
            String createdAt = order.getCreatedAt() != null ? order.getCreatedAt().toString() : "-";
            int customerId = order.getCustomer() != null ? order.getCustomer().getId() : -1;

            System.out.printf("#%-7d %-12d %-10s %-12s %-20s%n",
                    order.getId(), customerId, payment, total, createdAt);
        }
    }

    public void updateOrderStatus(int orderId, OrderStatus nextStatus) {
        try {
            staffOperationService.updateOrderStatus(orderId, nextStatus);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel cambio stato ordine: " + e.getMessage());
        }
    }

    public void seeOrdersState(OrderStatus status) {
        try {
            List<Order> orders = staffOperationService.listOrdersByStatus(status);
            printOrderBucket(labelFor(status), orders);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile caricare gli ordini per stato: " + e.getMessage());
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

    public void markDishUnavailable(int dishId) {
        System.out.println("Operazione non disponibile per lo staff (dish #" + dishId + ").");
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

    public void seeStateReservation(int reservationId) {
        try {
            Reservation reservation = staffOperationService.getReservationById(reservationId);
            System.out.println("Prenotazione #" + reservation.getId() + " stato: " + reservation.getStatus());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile leggere lo stato prenotazione: " + e.getMessage());
        }
    }

    public void seeDayReservations(LocalDate date) {
        showReservations(date);
    }

    public void updateStateReservation(int reservationId, ReservationStatus nextStatus) {
        try {
            staffOperationService.updateReservationStatus(reservationId, nextStatus);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore nel cambio stato prenotazione: " + e.getMessage());
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