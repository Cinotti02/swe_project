package ServiceLayer;

import DomainModel.search.SearchCriteria;
import DomainModel.search.SearchResult;

import java.sql.SQLException;

/**
 * Servizio applicativo per ricerche aggregate su menu, ordini e prenotazioni.
 */
public class SearchService {

    private final MenuQueryService menuQueryService;
    private final OrderService orderService;
    private final ReservationService reservationService;

    public SearchService(MenuQueryService menuQueryService,
                         OrderService orderService,
                         ReservationService reservationService) {
        this.menuQueryService = menuQueryService;
        this.orderService = orderService;
        this.reservationService = reservationService;
    }

    public SearchResult search(SearchCriteria criteria) throws SQLException {
        SearchCriteria effective = (criteria != null) ? criteria : SearchCriteria.builder();
        SearchResult result = new SearchResult();

        if (effective.getDish() != null && effective.getDish().hasFilters()) {
            result.setDishes(menuQueryService.searchDishes(effective.getDish()));
        }

        if (effective.getOrder() != null && effective.getOrder().hasFilters()) {
            result.setOrders(orderService.searchOrders(effective.getOrder()));
        }

        if (effective.getReservation() != null && effective.getReservation().hasFilters()) {
            result.setReservations(reservationService.searchReservations(effective.getReservation()));
        }

        return result;
    }
}