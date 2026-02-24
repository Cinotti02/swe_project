package DomainModel.search;

/**
 * Criterio composito che aggrega i filtri dei vari domini ricercabili.
 */
public class SearchCriteria {

    private DishSearchParameters dish;
    private OrderSearchParameters order;
    private ReservationSearchParameters reservation;

    private SearchCriteria() {
    }

    public static SearchCriteria builder() {
        return new SearchCriteria();
    }

    public SearchCriteria setDish(DishSearchParameters dish) {
        this.dish = dish;
        return this;
    }

    public SearchCriteria setOrder(OrderSearchParameters order) {
        this.order = order;
        return this;
    }

    public SearchCriteria setReservation(ReservationSearchParameters reservation) {
        this.reservation = reservation;
        return this;
    }

    public DishSearchParameters getDish() {
        return dish;
    }

    public OrderSearchParameters getOrder() {
        return order;
    }

    public ReservationSearchParameters getReservation() {
        return reservation;
    }

    public boolean hasFilters() {
        return (dish != null && dish.hasFilters())
                || (order != null && order.hasFilters())
                || (reservation != null && reservation.hasFilters());
    }
}