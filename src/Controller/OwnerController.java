package Controller;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.order.Order;
import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.search.DishSearchParameters;
import DomainModel.search.OrderSearchParameters;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.search.SearchCriteria;
import DomainModel.search.SearchResult;
import DomainModel.valueObject.Money;
import ServiceLayer.MenuQueryService;
import ServiceLayer.OwnerAdminService;
import ServiceLayer.SearchService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class OwnerController {

    private final OwnerAdminService ownerAdminService;
    private final MenuQueryService menuQueryService;
    private final SearchService searchService;

    public OwnerController(OwnerAdminService ownerAdminService, MenuQueryService menuQueryService, SearchService searchService) {
        this.ownerAdminService = ownerAdminService;
        this.menuQueryService = menuQueryService;
        this.searchService = searchService;
    }

    public void printMenuOverview() {
        try {
            Map<Category, List<Dish>> menu = menuQueryService.buildMenu(false, false);
            System.out.println("=== MENU COMPLETO ===");
            menu.forEach((category, dishes) -> {
                System.out.println(category.getName() + " (" + (category.isActive() ? "attiva" : "disattiva") + ")");
                for (Dish dish : dishes) {
                    String availability = dish.isAvailable() ? "[ON]" : "[OFF]";
                    System.out.println("  " + availability + " #" + dish.getId() + " " + dish.getName() + " - " + dish.getPrice());
                }
            });
        } catch (SQLException e) {
            System.err.println("Errore nel caricamento del menu: " + e.getMessage());
        }
    }

    public void addCategory(String name, String description) {
        try {
            Category category = ownerAdminService.createCategory(name, description);
            System.out.println("Categoria creata con id " + category.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare la categoria: " + e.getMessage());
        }
    }

    public void addDish(String name, String description, double price, int categoryId) {
        try {
            Dish dish = ownerAdminService.createDish(name, description, new Money(price), categoryId);
            System.out.println("Piatto creato con id " + dish.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare il piatto: " + e.getMessage());
        }
    }

    public void toggleDish(int dishId, boolean active) {
        try {
            ownerAdminService.changeDishAvailability(dishId, active);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore durante l'aggiornamento del piatto: " + e.getMessage());
        }
    }

    public void updateDishPrice(int dishId, double price) {
        try {
            ownerAdminService.updateDishPrice(dishId, new Money(price));
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile aggiornare il prezzo: " + e.getMessage());
        }
    }

    public void listTables() {
        try {
            List<Table> tables = ownerAdminService.listTables();
            System.out.println("=== TAVOLI ===");
            tables.forEach(table -> System.out.println("#" + table.getId() + " n." + table.getNumber()
                    + " posti:" + table.getSeats() + " joinable:" + table.isJoinable()
                    + " disponibile:" + table.isAvailable()));
        } catch (SQLException e) {
            System.err.println("Impossibile caricare i tavoli: " + e.getMessage());
        }
    }

    public void addTable(int number, int seats, boolean joinable, String location) {
        try {
            Table table = ownerAdminService.addTable(number, seats, joinable, location);
            System.out.println("Tavolo creato con id " + table.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare il tavolo: " + e.getMessage());
        }
    }

    public void configureSlot(LocalTime startTime, LocalTime endTime) {
        try {
            Slot slot = ownerAdminService.addSlot(startTime, endTime);
            System.out.println("Slot creato con id " + slot.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare lo slot: " + e.getMessage());
        }
    }

    public void listSlots(boolean includeClosed) {
        try {
            List<Slot> slots = ownerAdminService.listSlots(includeClosed);
            System.out.println("=== SLOT ===");
            for (Slot slot : slots) {
                System.out.println("#" + slot.getId() + " " + slot.getStartTime() + "-" + slot.getEndTime()
                        + (slot.isClosed() ? " [CHIUSO]" : ""));
            }
        } catch (SQLException e) {
            System.err.println("Impossibile caricare gli slot: " + e.getMessage());
        }
    }

    public void searchDishes(String nameContains,
                             Integer categoryId,
                             Boolean onlyAvailable,
                             BigDecimal minPrice,
                             BigDecimal maxPrice) {
        try {
            DishSearchParameters params = DishSearchParameters.builder();
            if (nameContains != null && !nameContains.isBlank()) params.setNameContains(nameContains);
            if (categoryId != null) params.setCategoryId(categoryId);
            if (onlyAvailable != null) params.setOnlyAvailable(onlyAvailable);
            if (minPrice != null) params.setMinPrice(minPrice);
            if (maxPrice != null) params.setMaxPrice(maxPrice);

            SearchResult result = searchService.search(SearchCriteria.builder().setDish(params));
            printDishes(result.getDishes());
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca piatti: " + e.getMessage());
        }
    }

    public void searchOrders(Integer customerId,
                             OrderStatus status,
                             PaymentMethod paymentMethod,
                             Integer categoryId,
                             LocalDate startDate,
                             LocalDate endDate) {
        try {
            OrderSearchParameters params = OrderSearchParameters.builder();
            if (customerId != null) params.setCustomerId(customerId);
            if (status != null) params.setStatus(status);
            if (paymentMethod != null) params.setPaymentMethod(paymentMethod);
            if (categoryId != null) params.setCategoryId(categoryId);
            if (startDate != null) params.setStartDate(startDate);
            if (endDate != null) params.setEndDate(endDate);

            SearchResult result = searchService.search(SearchCriteria.builder().setOrder(params));
            printOrders(result.getOrders());
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca ordini: " + e.getMessage());
        }
    }

    public void searchReservations(LocalDate date,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   Integer customerId,
                                   Integer slotId,
                                   Integer minGuests,
                                   Integer maxGuests,
                                   ReservationStatus status) {
        try {
            ReservationSearchParameters params = ReservationSearchParameters.builder();
            if (date != null) params.setDate(date);
            if (startDate != null) params.setStartDate(startDate);
            if (endDate != null) params.setEndDate(endDate);
            if (customerId != null) params.setCustomerId(customerId);
            if (slotId != null) params.setSlotId(slotId);
            if (minGuests != null) params.setMinGuests(minGuests);
            if (maxGuests != null) params.setMaxGuests(maxGuests);
            if (status != null) params.setStatus(status);

            SearchResult result = searchService.search(SearchCriteria.builder().setReservation(params));
            printReservations(result.getReservations());
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca prenotazioni: " + e.getMessage());
        }
    }

    private void printDishes(List<Dish> dishes) {
        System.out.println("=== RISULTATI PIATTI ===");
        if (dishes.isEmpty()) {
            System.out.println("(nessun piatto trovato)");
            return;
        }
        for (Dish dish : dishes) {
            String category = (dish.getCategory() != null) ? String.valueOf(dish.getCategory().getId()) : "-";
            System.out.println("#" + dish.getId() + " " + dish.getName() +
                    " | cat:" + category +
                    " | prezzo:" + dish.getPrice() +
                    " | disponibile:" + dish.isAvailable());
        }
    }

    private void printOrders(List<Order> orders) {
        System.out.println("=== RISULTATI ORDINI ===");
        if (orders.isEmpty()) {
            System.out.println("(nessun ordine trovato)");
            return;
        }
        for (Order order : orders) {
            int customerId = (order.getCustomer() != null) ? order.getCustomer().getId() : -1;
            LocalDateTime createdAt = order.getCreatedAt();
            System.out.println("#" + order.getId()
                    + " | customer:" + customerId
                    + " | stato:" + order.getStatus()
                    + " | payment:" + order.getPaymentMethod()
                    + " | totale:" + order.getTotalAmount()
                    + " | creato:" + (createdAt != null ? createdAt : "-"));
        }
    }

    private void printReservations(List<Reservation> reservations) {
        System.out.println("=== RISULTATI PRENOTAZIONI ===");
        if (reservations.isEmpty()) {
            System.out.println("(nessuna prenotazione trovata)");
            return;
        }
        for (Reservation reservation : reservations) {
            int customerId = (reservation.getCustomer() != null) ? reservation.getCustomer().getId() : -1;
            Integer slotId = (reservation.getTimeSlot() != null) ? reservation.getTimeSlot().getId() : null;
            System.out.println("#" + reservation.getId()
                    + " | customer:" + customerId
                    + " | data:" + reservation.getReservDate().toLocalDate()
                    + " | slot:" + slotId
                    + " | guests:" + reservation.getNumberOfGuests()
                    + " | stato:" + reservation.getStatus());
        }
    }
}