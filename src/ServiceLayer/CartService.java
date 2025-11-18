package ServiceLayer;

import DomainModel.menu.Dish;
import DomainModel.order.OrderItem;
import DomainModel.user.User;
import DomainModel.valueObject.Money;

import java.util.*;

/**
 * Gestisce il carrello di ciascun utente.
 * Il carrello è mantenuto in memoria (Map<userId, List<OrderItem>>),
 * non viene salvato a DB.
 */
public class CartService {

    // carrelli: userId -> lista di OrderItem
    private final Map<Integer, List<OrderItem>> carts = new HashMap<>();

    // ----------------------------------------------------------------
    // Metodi pubblici richiesti dall'UML
    // ----------------------------------------------------------------

    /**
     * Aggiunge un piatto al carrello dell'utente.
     * Se il piatto è già presente, incrementa la quantità.
     */
    public void addDishToCart(User user, Dish dish, int quantity){
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (dish == null) throw new IllegalArgumentException("Dish cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");

        List<OrderItem> cart = carts.computeIfAbsent(user.getId(), id -> new ArrayList<>());

        // se esiste già un item per quel piatto, aggiorno la quantità
        for (OrderItem item : cart) {
            if (item.getDish().getId() == dish.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        // altrimenti aggiungo nuovo OrderItem
        cart.add(new OrderItem(dish, quantity));
    }

    /**
     * Ritorna la lista di OrderItem nel carrello dell'utente.
     */
    public List<OrderItem> getCartItems(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        return List.copyOf(getOrCreateCart(user.getId()));
    }

    /**
     * Aggiorna la quantità di un piatto nel carrello.
     * Se newQuantity <= 0, rimuove l'item dal carrello.
     */
    public void updateItemQuantity(User user, int dishId, int newQuantity) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        List<OrderItem> cart = getOrCreateCart(user.getId());

        Iterator<OrderItem> it = cart.iterator();
        while (it.hasNext()) {
            OrderItem item = it.next();
            if (item.getDish().getId() == dishId) {
                if (newQuantity <= 0) {
                    it.remove();
                } else {
                    item.setQuantity(newQuantity);
                }
                return;
            }
        }
    }

    /**
     * Rimuove completamente un piatto dal carrello.
     */
    public void removeItem(User user, int dishId) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        List<OrderItem> cart = getOrCreateCart(user.getId());
        cart.removeIf(item -> item.getDish().getId() == dishId);
    }

    /**
     * Svuota il carrello dell'utente.
     */
    public void clearCart(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        carts.remove(user.getId());
    }

    /**
     * Calcola il totale del carrello.
     */
    public Money getCartTotal(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        List<OrderItem> cart = getOrCreateCart(user.getId());
        Money total = new Money(0.0);
        for (OrderItem item : cart) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    /**
     * Ritorna una stringa leggibile per CLI con il contenuto del carrello.
     */
    public String getCartSummary(User user) {
        List<OrderItem> cart = getCartItems(user);
        if (cart.isEmpty()) {
            return "Il carrello è vuoto.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Carrello di ").append(user.getUsername()).append(":\n");
        for (OrderItem item : cart) {
            sb.append("- ")
                    .append(item.getDish().getId()).append(" | ")
                    .append(item.getDish().getName()).append(" x")
                    .append(item.getQuantity())
                    .append(" = ").append(item.getTotalPrice())
                    .append("\n");
        }
        sb.append("Totale: ").append(getCartTotal(user));
        return sb.toString();
    }

    // ----------------------------------------------------------------
    // Metodi di supporto
    // ----------------------------------------------------------------

    private List<OrderItem> getOrCreateCart(int userId) {
        return carts.computeIfAbsent(userId, id -> new ArrayList<>());
    }
}