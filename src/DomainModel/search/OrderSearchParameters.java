package DomainModel.search;

import DomainModel.order.OrderStatus;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Parametri di ricerca per gli ordini.
 */
public class OrderSearchParameters {

    private Integer customerId;
    private OrderStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    private OrderSearchParameters() {
    }

    public static OrderSearchParameters builder() {
        return new OrderSearchParameters();
    }

    public OrderSearchParameters forCustomer(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderSearchParameters withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderSearchParameters fromDate(LocalDate date) {
        this.startDate = date;
        return this;
    }

    public OrderSearchParameters toDate(LocalDate date) {
        this.endDate = date;
        return this;
    }

    public Optional<Integer> getCustomerId() {
        return Optional.ofNullable(customerId);
    }

    public Optional<OrderStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public boolean hasFilters() {
        return customerId != null || status != null || startDate != null || endDate != null;
    }
}