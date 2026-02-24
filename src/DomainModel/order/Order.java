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

    public Order(User customer,
                 PaymentMethod paymentMethod,
                 Money totalAmount,
                 String notes) {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
        this.customer = customer;
        this.paymentMethod = paymentMethod;
        this.totalAmount = (totalAmount != null) ? totalAmount : new Money(0.0);
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
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    //fixme: se vuoi un overload comodo per settare il totale con un double, puoi aggiungere questo metodo:
    public void setTotalAmount(double amount) {
        this.totalAmount = new Money(amount);
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
        if (status == OrderStatus.CREATED) {
            this.status = OrderStatus.PREPARING;
        }
    }

    public void markReady() {
        if (status == OrderStatus.PREPARING) {
            this.status = OrderStatus.READY;
        }
    }

    public void markRetired() {
        if (status == OrderStatus.READY) {
            this.status = OrderStatus.RETIRED;
        }
    }

    public void cancel() {
        if (status != OrderStatus.RETIRED && status != OrderStatus.CANCELLED) {
            this.status = OrderStatus.CANCELLED;
        }
    }

    /**
     * Aggiunge un importo al totale (es. aggiungo un piatto).
     */
    public void addToTotal(Money amount) {
        if (amount == null) return;
        this.totalAmount = this.totalAmount.add(amount);
    }

    /**
     * Aggiunge un importo moltiplicato per una quantità (es. 3 × piatto da 8€).
     */
    public void addToTotal(Money unitPrice, int quantity) {
        if (unitPrice == null || quantity <= 0) return;
        this.totalAmount = this.totalAmount.add(unitPrice.multiply(quantity));
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