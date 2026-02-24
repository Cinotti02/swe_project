package Controller;

import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.search.SearchCriteria;
import DomainModel.search.SearchResult;
import ServiceLayer.SearchService;
import ServiceLayer.StaffOperationService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StaffController {
    private final StaffOperationService staffOperationService;
    private final SearchService searchService;

    public StaffController(StaffOperationService staffOperationService, SearchService searchService) {
        this.staffOperationService = staffOperationService;
        this.searchService = searchService;
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

    public void searchOrders(Integer customerId,
                             OrderStatus status,
                             PaymentMethod paymentMethod,
                             Integer categoryId,
                             LocalDate startDate,
                             LocalDate endDate) {
        try {
            OrderSearchParameters params = OrderSearchParameters.builder();
            if (customerId != null) params.setCustomerId(customerId);
            if (status != null) params.setStatus(status);
            if (paymentMethod != null) params.setPaymentMethod(paymentMethod);
            if (categoryId != null) params.setCategoryId(categoryId);
            if (startDate != null) params.setStartDate(startDate);
            if (endDate != null) params.setEndDate(endDate);

            SearchResult result = searchService.search(SearchCriteria.builder().setOrder(params));
            printOrders(result.getOrders());
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca ordini: " + e.getMessage());
        }
    }

    public void searchReservations(LocalDate date,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   Integer customerId,
                                   Integer slotId,
                                   Integer minGuests,
                                   Integer maxGuests,
                                   ReservationStatus status) {
        try {
            ReservationSearchParameters params = ReservationSearchParameters.builder();
            if (date != null) params.setDate(date);
            if (startDate != null) params.setStartDate(startDate);
            if (endDate != null) params.setEndDate(endDate);
            if (customerId != null) params.setCustomerId(customerId);
            if (slotId != null) params.setSlotId(slotId);
            if (minGuests != null) params.setMinGuests(minGuests);
            if (maxGuests != null) params.setMaxGuests(maxGuests);
            if (status != null) params.setStatus(status);

            SearchResult result = searchService.search(SearchCriteria.builder().setReservation(params));
            printReservations(result.getReservations());
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca prenotazioni: " + e.getMessage());
        }
    }

    private void printOrders(List<Order> orders) {
        System.out.println("=== RISULTATI ORDINI ===");
        if (orders.isEmpty()) {
            System.out.println("(nessun ordine trovato)");
            return;
        }
        for (Order order : orders) {
            int customerId = (order.getCustomer() != null) ? order.getCustomer().getId() : -1;
            System.out.println("#" + order.getId()
                    + " | customer:" + customerId
                    + " | stato:" + order.getStatus()
                    + " | payment:" + order.getPaymentMethod()
                    + " | totale:" + order.getTotalAmount()
                    + " | creato:" + order.getCreatedAt());
        }
    }

    private void printReservations(List<Reservation> reservations) {
        System.out.println("=== RISULTATI PRENOTAZIONI ===");
        if (reservations.isEmpty()) {
            System.out.println("(nessuna prenotazione trovata)");
            return;
        }
        for (Reservation reservation : reservations) {
            int customerId = (reservation.getCustomer() != null) ? reservation.getCustomer().getId() : -1;
            Integer slotId = (reservation.getTimeSlot() != null) ? reservation.getTimeSlot().getId() : null;
            System.out.println("#" + reservation.getId()
                    + " | customer:" + customerId
                    + " | data:" + reservation.getReservDate().toLocalDate()
                    + " | slot:" + slotId
                    + " | guests:" + reservation.getNumberOfGuests()
                    + " | stato:" + reservation.getStatus());
        }
    }
}