package Controller;

import DomainModel.menu.Category;
import DomainModel.notification.Notification;
import DomainModel.menu.Dish;
import DomainModel.order.Order;
import DomainModel.order.OrderItem;
import DomainModel.order.PaymentMethod;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.Slot;
import DomainModel.user.User;
import ServiceLayer.CartService;
import ServiceLayer.MenuQueryService;
import ServiceLayer.OrderService;
import ServiceLayer.ReservationService;
import ServiceLayer.NotificationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CustomerController {

    private final MenuQueryService menuQueryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public CustomerController(MenuQueryService menuQueryService,
                              CartService cartService,
                              OrderService orderService,
                              ReservationService reservationService,
                              NotificationService notificationService) {
        this.menuQueryService = menuQueryService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    public void showMenu() {
        try {
            Map<Category, List<Dish>> menu = menuQueryService.buildMenu(true, true);
            System.out.println("=== MENU ===");
            menu.forEach((category, dishes) -> {
                System.out.println("-- " + category.getName() + " --");
                if (dishes.isEmpty()) {
                    System.out.println("(Nessun piatto disponibile)");
                }
                for (Dish dish : dishes) {
                    System.out.println("#" + dish.getId() + " - " + dish.getName() + " (" + dish.getPrice() + ")");
                    if (dish.getDescription() != null) {
                        System.out.println("   " + dish.getDescription());
                    }
                }
            });
        } catch (SQLException e) {
            System.err.println("Impossibile caricare il menu: " + e.getMessage());
        }
    }

    public void searchDishes(String query) {
        try {
            List<Dish> dishes = menuQueryService.searchDishes(query, true);
            if (dishes.isEmpty()) {
                System.out.println("Nessun piatto trovato");
                return;
            }
            System.out.println("=== Risultati ricerca ===");
            for (Dish dish : dishes) {
                System.out.println("#" + dish.getId() + " - " + dish.getName() + " (" + dish.getPrice() + ")");
                if (dish.getDescription() != null) {
                    System.out.println("   " + dish.getDescription());
                }
            }
        } catch (SQLException e) {
            System.err.println("Impossibile cercare i piatti: " + e.getMessage());
        }
    }

    public void addDishToCart(User user, int dishId, int quantity) {
        try {
            Dish dish = menuQueryService.findDishById(dishId)
                    .filter(Dish::isAvailable)
                    .orElseThrow(() -> new IllegalArgumentException("Piatto non disponibile"));
            cartService.addDishToCart(user, dish, quantity);
            System.out.println("Piatto aggiunto al carrello");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    public void showCart(User user) {
        System.out.println(cartService.getCartSummary(user));
    }

    public void removeItem(User user, int dishId) {
        cartService.removeItem(user, dishId);
    }

    public void checkoutTakeAway(User user, PaymentMethod paymentMethod, String notes) {
        try {
            List<OrderItem> items = cartService.getCartItems(user);
            if (items.isEmpty()) {
                System.out.println("Il carrello è vuoto");
                return;
            }
            Order order = orderService.placeTakeAwayOrder(user, items, paymentMethod, notes);
            cartService.clearCart(user);
            System.out.println("Ordine creato con ID " + order.getId());
        } catch (SQLException e) {
            System.err.println("Errore durante la creazione dell'ordine: " + e.getMessage());
        }
    }

    public void createReservation(User user,
                                  LocalDate date,
                                  int slotId,
                                  int guests,
                                  String notes) {
        try {
            Reservation reservation = reservationService.createReservation(user, date, slotId, guests, notes);
            System.out.println("Prenotazione creata con ID " + reservation.getId());
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile creare la prenotazione: " + e.getMessage());
        }
    }

    public void cancelReservation(User user, int reservationId) {
        try {
            reservationService.cancelReservation(reservationId, user);
            System.out.println("Prenotazione annullata");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Errore durante l'annullamento: " + e.getMessage());
        }
    }

    public void listReservations(User user) {
        try {
            List<Reservation> reservations = reservationService.listCustomerReservations(user);
            if (reservations.isEmpty()) {
                System.out.println("Non hai prenotazioni attive");
                return;
            }
            System.out.println("=== Le tue prenotazioni ===");
            for (Reservation reservation : reservations) {
                System.out.println("#" + reservation.getId() + " - "
                        + reservation.getReservDate() + " - Stato: " + reservation.getStatus());
            }
        } catch (SQLException e) {
            System.err.println("Impossibile caricare le prenotazioni: " + e.getMessage());
        }
    }

    public void showNotifications(User user, boolean unreadOnly) {
        try {
            List<Notification> notifications = notificationService.listNotificationsForUser(user.getId(), unreadOnly);

            System.out.println(unreadOnly ? "=== Notifiche non lette ===" : "=== Tutte le notifiche ===");
            if (notifications.isEmpty()) {
                System.out.println("(nessuna notifica)");
                return;
            }
            for (Notification n : notifications) {
                System.out.println("#" + n.getId()
                        + " | tipo:" + n.getType()
                        + " | stato:" + n.getStatus()
                        + " | msg:" + n.getMessage()
                        + " | creata:" + n.getCreatedAt());
            }
        } catch (SQLException e) {
            System.err.println("Impossibile caricare le notifiche: " + e.getMessage());
        }
    }

    public List<Notification> getNotifications(User user, boolean unreadOnly) throws SQLException {
        return notificationService.listNotificationsForUser(user.getId(), unreadOnly);
    }

    public void markNotificationAsRead(int notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            System.out.println("Notifica segnata come letta");
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Impossibile aggiornare la notifica: " + e.getMessage());
        }
    }

    public void showAvailableSlots() {
        try {
            List<Slot> slots = reservationService.listOpenSlots();
            if (slots.isEmpty()) {
                System.out.println("Nessuno slot disponibile al momento");
                return;
            }
            System.out.println("=== SLOT DISPONIBILI ===");
            for (Slot slot : slots) {
                System.out.println("#" + slot.getId() + " " + slot.getStartTime() + "-" + slot.getEndTime());
            }
        } catch (SQLException e) {
            System.err.println("Impossibile recuperare gli slot: " + e.getMessage());
        }
    }
}