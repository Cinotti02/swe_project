package ServiceLayer;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.order.OrderItem;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ServiceLayer.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private CartService service;
    private User user;
    private Dish pizza;

    @BeforeEach
    void setUp() {
        service = new CartService();
        user = new User("mario", new Email("mario@example.com"), "hash",
                "Mario", "Rossi", Role.CUSTOMER);
        user.setId(1);
        pizza = new Dish("Pizza", "Margherita", 8.50,
                new Category("Pizze", "Pizze"));
        pizza.setId(10);
    }

    @Test
    void addingSameDishMergesQuantities() {
        service.addDishToCart(user, pizza, 2);
        service.addDishToCart(user, pizza, 3);

        List<OrderItem> items = service.getCartItems(user);
        assertEquals(1, items.size());
        assertEquals(5, items.get(0).getQuantity());
        assertEquals("42.50", service.getCartTotal(user).getAmount().toPlainString());
    }

    @Test
    void updatingToZeroRemovesItem() {
        service.addDishToCart(user, pizza, 1);
        service.updateItemQuantity(user, pizza.getId(), 0);
        assertTrue(service.getCartItems(user).isEmpty());
    }

    @Test
    void returnedCartListCannotBeModified() {
        service.addDishToCart(user, pizza, 1);
        assertThrows(UnsupportedOperationException.class,
                () -> service.getCartItems(user).clear());
    }

    @Test
    void cartsAreSeparatedByUser() {
        User other = new User("luigi", new Email("luigi@example.com"), "hash",
                "Luigi", "Verdi", Role.CUSTOMER);
        other.setId(2);
        service.addDishToCart(user, pizza, 1);

        assertEquals(1, service.getCartItems(user).size());
        assertTrue(service.getCartItems(other).isEmpty());
    }

    @Test
    void clearCartRemovesContentsAndSummaryReportsEmpty() {
        service.addDishToCart(user, pizza, 1);
        service.clearCart(user);
        assertTrue(service.getCartItems(user).isEmpty());
        assertTrue(service.getCartSummary(user).contains("vuoto"));
    }
}
