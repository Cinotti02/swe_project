package DomainModel.order;

import DomainModel.user.User;
import DomainModel.valueObject.Money;

import java.time.LocalDateTime;

public class Order {

    private int id;
    private User customer;
    private LocalDateTime createdAt;       // data/ora creazione
    private OrderStatus status;            // CREATED, PREPARING, READY, RETIRED, CANCELLED
    private PaymentMethod paymentMethod;   // ONLINE, INLOCO
    private Money totalAmount;             // totale dell'ordine
    private String notes;                  // note dell'ordine (es. "senza glutine")

    public Order() {}

    public Order(User customer, PaymentMethod paymentMethod, Money totalAmount, String notes) {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
        setCustomer(customer);
        setPaymentMethod(paymentMethod);
        setTotalAmount((totalAmount != null) ? totalAmount : new Money(0.0));
        this.notes = notes;
    }

    // Overload comodo se vuoi passare il totale come double
    public Order(User customer,
                 PaymentMethod paymentMethod,
                 double totalAmount,
                 String notes) {

        this(customer, paymentMethod, new Money(totalAmount), notes);
    }


    // ----------------------------------------------------
    // Getter & Setter
    // ----------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        if (customer == null)
            throw new IllegalArgumentException("Customer cannot be null");
        this.customer = customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Order status cannot be null");
        if (this.status != null && this.status != status && !this.status.canTransitionTo(status))
            throw new IllegalStateException("Invalid order transition from " + this.status + " to " + status);
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null)
            throw new IllegalArgumentException("Payment method cannot be null");
        this.paymentMethod = paymentMethod;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        if (totalAmount == null)
            throw new IllegalArgumentException("Total amount cannot be null");
        this.totalAmount = totalAmount;
    }

    public void setTotalAmount(double amount) {
        setTotalAmount(new Money(amount));
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ----------------------------------------------------
    // Deleghe di stato (comode)
    // ----------------------------------------------------

    public boolean isCreated()   { return status == OrderStatus.CREATED; }
    public boolean isPreparing() { return status == OrderStatus.PREPARING; }
    public boolean isReady()     { return status == OrderStatus.READY; }
    public boolean isRetired()   { return status == OrderStatus.RETIRED; }
    public boolean isCancelled() { return status == OrderStatus.CANCELLED; }

    // ----------------------------------------------------
    // Metodi di business (semplici)
    // ----------------------------------------------------

    public void markPreparing() {
        transitionTo(OrderStatus.PREPARING);
    }

    public void markReady() {
        transitionTo(OrderStatus.READY);
    }

    public void markRetired() {
        transitionTo(OrderStatus.RETIRED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    public void addToTotal(Money amount) {
        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be null");
        this.totalAmount = this.totalAmount.add(amount);
    }

    public void addToTotal(Money unitPrice, int quantity) {
        if (unitPrice == null)
            throw new IllegalArgumentException("Unit price cannot be null");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");
        this.totalAmount = this.totalAmount.add(unitPrice.multiply(quantity));
    }

    private void transitionTo(OrderStatus nextStatus) {
        if (nextStatus == null)
            throw new IllegalArgumentException("Next status cannot be null");
        if (status == null)
            throw new IllegalStateException("Current status cannot be null");

        if (!status.canTransitionTo(nextStatus))
            throw new IllegalStateException("Invalid order transition from " + status + " to " + nextStatus);

        setStatus(nextStatus);
    }


    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getUsername() : "null") +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", createdAt=" + createdAt +
                '}';
    }
}