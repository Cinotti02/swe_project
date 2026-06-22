package DomainModel;

import DomainModel.search.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultTest {

    @Test
    void listsAreInitializedAndMutableReferencesCanBeReplaced() {
        SearchResult result = new SearchResult();
        assertNotNull(result.getDishes());
        assertNotNull(result.getOrders());
        assertNotNull(result.getReservations());
        assertTrue(result.isEmpty());
        result.setDishes(List.of());
        result.setOrders(List.of());
        result.setReservations(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void nullSettersResetListsToEmpty() {
        SearchResult result = new SearchResult();
        result.setDishes(Arrays.asList((DomainModel.menu.Dish) null));
        result.setOrders(Arrays.asList((DomainModel.order.Order) null));
        result.setReservations(Arrays.asList((DomainModel.reservation.Reservation) null));
        assertFalse(result.isEmpty());
        result.setDishes(null);
        result.setOrders(null);
        result.setReservations(null);
        assertTrue(result.getDishes().isEmpty());
        assertTrue(result.getOrders().isEmpty());
        assertTrue(result.getReservations().isEmpty());
        assertTrue(result.isEmpty());
    }

    @Test
    void isEmptyWhenAnyListContainsElements() {
        SearchResult result = new SearchResult();
        result.setDishes(Arrays.asList((DomainModel.menu.Dish) null));
        assertFalse(result.isEmpty());
        result.setDishes(List.of());
        result.setOrders(Arrays.asList((DomainModel.order.Order) null));
        assertFalse(result.isEmpty());
        result.setOrders(List.of());
        result.setReservations(Arrays.asList((DomainModel.reservation.Reservation) null));
        assertFalse(result.isEmpty());
    }

    @Test
    void setterKeepsProvidedListReference() {
        SearchResult result = new SearchResult();
        ArrayList<DomainModel.menu.Dish> dishes = new ArrayList<>();
        result.setDishes(dishes);
        assertSame(dishes, result.getDishes());

        dishes.add(null);
        assertFalse(result.isEmpty());
    }

    @Test
    void replacingOneListDoesNotAffectOthers() {
        SearchResult result = new SearchResult();
        result.setDishes(Arrays.asList((DomainModel.menu.Dish) null));
        result.setOrders(List.of());
        result.setReservations(List.of());

        result.setDishes(null);

        assertTrue(result.getDishes().isEmpty());
        assertTrue(result.getOrders().isEmpty());
        assertTrue(result.getReservations().isEmpty());
        assertTrue(result.isEmpty());
    }
}