package DomainModel.search;

import DomainModel.menu.Dish;
import DomainModel.order.Order;
import DomainModel.reservation.Reservation;

import java.util.ArrayList;
import java.util.List;

/**
 * Risultato composito delle ricerche cross-dominio.
 */
public class SearchResult {

    private List<Dish> dishes = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = (dishes != null) ? dishes : new ArrayList<>();
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = (orders != null) ? orders : new ArrayList<>();
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = (reservations != null) ? reservations : new ArrayList<>();
    }

    public boolean isEmpty() {
        return dishes.isEmpty() && orders.isEmpty() && reservations.isEmpty();
    }
}