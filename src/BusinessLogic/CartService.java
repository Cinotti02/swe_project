package BusinessLogic;

import DomainModel.menu.Dish;
import DomainModel.order.OrderItem;
import DomainModel.valueObject.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartService {

    // Lista di item che rappresentano il carrello corrente
    private final List<OrderItem> items = new ArrayList<>();

    /**
     * Aggiunge un piatto al carrello.
     * Se il piatto è già presente, aumenta solo la quantità.
     */
    public void addDishToCart(Dish dish, int quantity) {
        if (dish == null) {
            throw new IllegalArgumentException("Dish cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        // Se l'item esiste già, aggiorno la quantità
        for (OrderItem item : items) {
            if (item.getDish().getId() == dish.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        // Altrimenti creo un nuovo OrderItem
        OrderItem newItem = new OrderItem(dish, quantity);
        items.add(newItem);
    }

    /**
     * Restituisce una copia non modificabile della lista di item nel carrello.
     */
    public List<OrderItem> getCartItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Calcola il totale corrente del carrello.
     */
    public Money getTotalAmount() {
        Money total = new Money(0.0);
        for (OrderItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    /**
     * Aggiorna la quantità di un piatto nel carrello.
     * Se newQuantity <= 0, rimuove l'item.
     */
    public void updateItemQuantity(int dishId, int newQuantity) {
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            if (item.getDish().getId() == dishId) {
                if (newQuantity <= 0) {
                    items.remove(i);
                } else {
                    item.setQuantity(newQuantity);
                }
                return;
            }
        }
    }

    /**
     * Rimuove un piatto dal carrello dato il suo id.
     */
    public void removeItem(int dishId) {
        items.removeIf(item -> item.getDish().getId() == dishId);
    }

    /**
     * Svuota completamente il carrello.
     */
    public void clearCart() {
        items.clear();
    }

    /**
     * True se il carrello è vuoto.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
}