package DomainModel.search;

import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

//Parametri di ricerca per gli ordini.

public class OrderSearchParameters {

    private Integer customerId;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private Integer categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minTotalAmount;
    private BigDecimal maxTotalAmount;

    private OrderSearchParameters() {
    }

    public static OrderSearchParameters builder() {
        return new OrderSearchParameters();
    }

    public OrderSearchParameters setCustomerId(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderSearchParameters setStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderSearchParameters setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public OrderSearchParameters setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public OrderSearchParameters setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public OrderSearchParameters setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public OrderSearchParameters setMinTotalAmount(BigDecimal minTotalAmount) {
        this.minTotalAmount = minTotalAmount;
        return this;
    }

    public OrderSearchParameters setMaxTotalAmount(BigDecimal maxTotalAmount) {
        this.maxTotalAmount = maxTotalAmount;
        return this;
    }

    public Optional<Integer> getCustomerId() {
        return Optional.ofNullable(customerId);
    }

    public Optional<OrderStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<PaymentMethod> getPaymentMethod() {
        return Optional.ofNullable(paymentMethod);
    }

    public Optional<Integer> getCategoryId() {
        return Optional.ofNullable(categoryId);
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public Optional<BigDecimal> getMinTotalAmount() {
        return Optional.ofNullable(minTotalAmount);
    }

    public Optional<BigDecimal> getMaxTotalAmount() {
        return Optional.ofNullable(maxTotalAmount);
    }

    public boolean hasFilters() {
        return customerId != null
                || status != null
                || paymentMethod != null
                || categoryId != null
                || startDate != null
                || endDate != null
                || minTotalAmount != null
                || maxTotalAmount != null;
    }
}