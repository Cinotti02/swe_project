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
        if (customerId != null && customerId <= 0)
            throw new IllegalArgumentException("Customer id must be positive");
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
        if (categoryId != null && categoryId <= 0)
            throw new IllegalArgumentException("Category id must be positive");
        this.categoryId = categoryId;
        return this;
    }

    public OrderSearchParameters setStartDate(LocalDate startDate) {
        if (endDate != null && startDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date");
        this.startDate = startDate;
        return this;
    }

    public OrderSearchParameters setEndDate(LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date cannot be before start date");
        this.endDate = endDate;
        return this;
    }

    public OrderSearchParameters setMinTotalAmount(BigDecimal minTotalAmount) {
        if (minTotalAmount != null && minTotalAmount.signum() < 0)
            throw new IllegalArgumentException("Min total amount cannot be negative");
        if (maxTotalAmount != null && minTotalAmount != null && minTotalAmount.compareTo(maxTotalAmount) > 0)
            throw new IllegalArgumentException("Min total amount cannot be greater than max total amount");
        this.minTotalAmount = minTotalAmount;
        return this;
    }

    public OrderSearchParameters setMaxTotalAmount(BigDecimal maxTotalAmount) {
        if (maxTotalAmount != null && maxTotalAmount.signum() < 0)
            throw new IllegalArgumentException("Max total amount cannot be negative");
        if (minTotalAmount != null && maxTotalAmount != null && maxTotalAmount.compareTo(minTotalAmount) < 0)
            throw new IllegalArgumentException("Max total amount cannot be lower than min total amount");
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