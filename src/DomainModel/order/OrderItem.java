package DomainModel.order;

import DomainModel.menu.Dish;
import DomainModel.valueObject.Money;

public class OrderItem {

    private int id;
    private Dish dish;            // piatto ordinato
    private Money unitPrice;      // prezzo del piatto al momento dell'ordine
    private int quantity;         // quantità ordinata

    public OrderItem() {}

    public OrderItem(Dish dish, int quantity) {
        if (dish == null)
            throw new IllegalArgumentException("Dish cannot be null");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");

        this.dish = dish;
        this.unitPrice = dish.getPrice();   // salvi il prezzo al momento dell'ordine
        this.quantity = quantity;
    }

    // ------------------ Getter & Setter ------------------

    public int getId() {
        return id;
    }

    public void setId(int id) { // impostato dal DAO
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        if (dish == null)
            throw new IllegalArgumentException("Dish cannot be null");
        this.dish = dish;
        this.unitPrice = dish.getPrice();
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        if (unitPrice == null)
            throw new IllegalArgumentException("Unit price cannot be null");
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");
        this.quantity = quantity;
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity);
    }

    // ------------------ ToString ------------------

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", dish=" + dish.getName() +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}