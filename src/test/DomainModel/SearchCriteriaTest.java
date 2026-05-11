package test.DomainModel;

import DomainModel.search.DishSearchParameters;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.search.SearchCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaTest {
    @Test
    void builder() {
        assertNotNull(SearchCriteria.builder());
    }

    @Test
    void setDish() {
        DishSearchParameters dish = DishSearchParameters.builder().setCategoryId(1);
        SearchCriteria criteria = SearchCriteria.builder().setDish(dish);
        assertSame(dish, criteria.getDish());
    }

    @Test
    void setOrder() {
        OrderSearchParameters order = OrderSearchParameters.builder().setCategoryId(2);
        SearchCriteria criteria = SearchCriteria.builder().setOrder(order);
        assertSame(order, criteria.getOrder());
    }

    @Test
    void setReservation() {
        ReservationSearchParameters reservation = ReservationSearchParameters.builder().setCustomerId(4);
        SearchCriteria criteria = SearchCriteria.builder().setReservation(reservation);
        assertSame(reservation, criteria.getReservation());
    }

    @Test
    void getDish() {
        assertNull(SearchCriteria.builder().getDish());
    }

    @Test
    void getOrder() {
        assertNull(SearchCriteria.builder().getOrder());
    }

    @Test
    void getReservation() {
        assertNull(SearchCriteria.builder().getReservation());
    }

    @Test
    void hasFilters() {
        SearchCriteria empty = SearchCriteria.builder();
        SearchCriteria nonEmpty = SearchCriteria.builder().setDish(DishSearchParameters.builder().setNameContains("pizza"));
        assertFalse(empty.hasFilters());
        assertTrue(nonEmpty.hasFilters());
    }

    @Test
    void hasFiltersWithEmptyNestedObjects() {
        SearchCriteria criteria = SearchCriteria.builder()
                .setDish(DishSearchParameters.builder())
                .setOrder(OrderSearchParameters.builder())
                .setReservation(ReservationSearchParameters.builder());
        assertFalse(criteria.hasFilters());
    }

    @Test
    void hasFiltersWithOrderOrReservation() {
        SearchCriteria withOrder = SearchCriteria.builder().setOrder(OrderSearchParameters.builder().setCustomerId(1));
        SearchCriteria withReservation = SearchCriteria.builder().setReservation(ReservationSearchParameters.builder().setSlotId(2));
        assertTrue(withOrder.hasFilters());
        assertTrue(withReservation.hasFilters());
    }

    @Test
    void hasFiltersIgnoresNullNestedObjects() {
        SearchCriteria criteria = SearchCriteria.builder().setDish(null).setOrder(null).setReservation(null);
        assertFalse(criteria.hasFilters());
    }

    @Test
    void replacingNestedObjectUpdatesHasFilters() {
        SearchCriteria criteria = SearchCriteria.builder().setDish(DishSearchParameters.builder().setNameContains("pizza"));
        assertTrue(criteria.hasFilters());
        criteria.setDish(DishSearchParameters.builder());
        assertFalse(criteria.hasFilters());
    }
}