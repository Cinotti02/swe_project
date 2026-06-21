package test.ServiceLayer;

import DomainModel.menu.Dish;
import DomainModel.order.Order;
import DomainModel.reservation.Reservation;
import DomainModel.search.DishSearchParameters;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.search.SearchCriteria;
import DomainModel.search.SearchResult;
import ServiceLayer.MenuQueryService;
import ServiceLayer.OrderService;
import ServiceLayer.ReservationService;
import ServiceLayer.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    private FakeMenuService menu;
    private FakeOrderService orders;
    private FakeReservationService reservations;
    private SearchService service;

    @BeforeEach
    void setUp() {
        menu = new FakeMenuService();
        orders = new FakeOrderService();
        reservations = new FakeReservationService();
        service = new SearchService(menu, orders, reservations);
    }

    @Test
    void nullOrEmptyCriteriaDoesNotQueryDependencies() throws Exception {
        assertTrue(service.search(null).isEmpty());
        assertFalse(menu.called);
        assertFalse(orders.called);
        assertFalse(reservations.called);
    }

    @Test
    void delegatesOnlyConfiguredSearchTypes() throws Exception {
        DishSearchParameters dish = DishSearchParameters.builder().setNameContains("pizza");
        OrderSearchParameters order = OrderSearchParameters.builder().setCustomerId(3);

        SearchResult result = service.search(
                SearchCriteria.builder().setDish(dish).setOrder(order));

        assertSame(menu.result, result.getDishes());
        assertSame(orders.result, result.getOrders());
        assertTrue(result.getReservations().isEmpty());
        assertTrue(menu.called);
        assertTrue(orders.called);
        assertFalse(reservations.called);
    }

    @Test
    void reservationCriteriaIsDelegated() throws Exception {
        ReservationSearchParameters params =
                ReservationSearchParameters.builder().setSlotId(2);
        SearchResult result = service.search(SearchCriteria.builder().setReservation(params));
        assertSame(reservations.result, result.getReservations());
        assertTrue(reservations.called);
    }

    private static class FakeMenuService extends MenuQueryService {
        private final List<Dish> result = List.of(new Dish());
        private boolean called;

        FakeMenuService() {
            super(null, null);
        }

        @Override
        public List<Dish> searchDishes(DishSearchParameters params) {
            called = true;
            return result;
        }
    }

    private static class FakeOrderService extends OrderService {
        private final List<Order> result = List.of(new Order());
        private boolean called;

        FakeOrderService() {
            super(null);
        }

        @Override
        public List<Order> searchOrders(OrderSearchParameters params) {
            called = true;
            return result;
        }
    }

    private static class FakeReservationService extends ReservationService {
        private final List<Reservation> result = List.of(new Reservation());
        private boolean called;

        FakeReservationService() {
            super(null, null, null, null, null);
        }

        @Override
        public List<Reservation> searchReservations(ReservationSearchParameters params) {
            called = true;
            return result;
        }
    }
}
