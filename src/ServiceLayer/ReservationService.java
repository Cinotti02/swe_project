package ServiceLayer;

import DomainModel.notification.Notification;
import DomainModel.notification.TypeNotification;
import DomainModel.reservation.MergeTable;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.search.ReservationSearchParameters;
import DomainModel.user.User;
import ORM.NotificationDAO;
import ORM.ReservationDAO;
import ORM.SlotDAO;
import ORM.TableDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final TableDAO tableDAO;
    private final SlotDAO slotDAO;
    private final NotificationDAO notificationDAO;
    private final TableAllocationService tableAllocationService;

    public ReservationService(ReservationDAO reservationDAO,
                              TableDAO tableDAO,
                              SlotDAO slotDAO,
                              NotificationDAO notificationDAO,
                              TableAllocationService tableAllocationService) {
        this.reservationDAO = reservationDAO;
        this.tableDAO = tableDAO;
        this.slotDAO = slotDAO;
        this.notificationDAO = notificationDAO;
        this.tableAllocationService = tableAllocationService;
    }

    public Reservation createReservation(User customer,
                                         LocalDate date,
                                         int slotId,
                                         int guests,
                                         String notes) throws SQLException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Guests must be greater than zero");
        }

        Slot slot = slotDAO.getSlotById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
        if (slot.isClosed()) {
            throw new IllegalStateException("Selected slot is closed");
        }

        List<Integer> reservedIds = reservationDAO.getReservedTableIds(date, slotId);
        Set<Integer> reservedSet = new HashSet<>(reservedIds);

        List<Table> availableTables = tableDAO.getAvailableTables().stream()
                .filter(table -> !reservedSet.contains(table.getId()))
                .collect(Collectors.toList());

        List<Table> combination = tableAllocationService.findBestCombination(availableTables, guests);
        if (combination == null || combination.isEmpty()) {
            throw new IllegalStateException("No tables available for the requested slot");
        }

        LocalDateTime reservationDateTime = LocalDateTime.of(date, slot.getStartTime());
        Reservation reservation = new Reservation(customer, reservationDateTime, slot, guests, notes);

        reservationDAO.addReservation(reservation);

        List<MergeTable> assignments = buildAssignments(reservation, combination);
        reservationDAO.saveTableAssignments(assignments);
        reservation.setTables(assignments);

        notifyCustomer(reservation, "Prenotazione ricevuta per " + date + " alle " + slot.getStartTime(),
                TypeNotification.CONFIRMATION);

        return reservation;
    }

    public Reservation getReservation(int reservationId) throws SQLException {
        return reservationDAO.getReservationById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
    }

    public List<Reservation> listCustomerReservations(User customer) throws SQLException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        return reservationDAO.getReservationsByCustomer(customer.getId());
    }

    public List<Reservation> listReservationsByDate(LocalDate date) throws SQLException {
        return reservationDAO.getReservationsByDate(date);
    }

    public List<Reservation> searchReservations(ReservationSearchParameters params) throws SQLException {
        return reservationDAO.searchReservations(params);
    }


    public void cancelReservation(int reservationId, User requester) throws SQLException {
        Reservation reservation = getReservation(reservationId);
        if (requester == null || (!requester.isOwner() && !requester.isStaff()
                && reservation.getCustomer().getId() != requester.getId())) {
            throw new IllegalArgumentException("Requester cannot cancel this reservation");
        }

        reservation.cancel();
        reservationDAO.updateStatus(reservationId, reservation.getStatus());
        notifyCustomer(reservation,
                "La tua prenotazione #" + reservationId + " è stata annullata",
                TypeNotification.UPDATE);
    }

    public void confirmReservation(int reservationId) throws SQLException {
        changeStatus(reservationId, ReservationStatus.CONFIRMED,
                "La prenotazione è stata confermata",
                TypeNotification.CONFIRMATION);
    }

    public void checkInReservation(int reservationId) throws SQLException {
        changeStatus(reservationId, ReservationStatus.CHECKED_IN,
                "Benvenuto! Prenotazione in check-in",
                TypeNotification.UPDATE);
    }

    public void completeReservation(int reservationId) throws SQLException {
        changeStatus(reservationId, ReservationStatus.COMPLETED,
                "Grazie per averci visitato",
                TypeNotification.UPDATE);
    }

    public void markNoShow(int reservationId) throws SQLException {
        changeStatus(reservationId, ReservationStatus.NO_SHOW,
                "La prenotazione è stata segnata come no-show",
                TypeNotification.ALERT);
    }

    private void changeStatus(int reservationId, ReservationStatus nextStatus, String message, TypeNotification type) throws SQLException {
        Reservation reservation = getReservation(reservationId);
        switch (nextStatus) {
            case CONFIRMED -> reservation.confirm();
            case CHECKED_IN -> reservation.checkIn();
            case COMPLETED -> reservation.complete();
            case NO_SHOW -> reservation.markNoShow();
            case CANCELED -> reservation.cancel();
            default -> throw new IllegalArgumentException("Unsupported reservation transition: " + nextStatus);
        }
        reservationDAO.updateStatus(reservationId, reservation.getStatus());
        notifyCustomer(reservation, message, type);
    }

    public List<Slot> listOpenSlots() throws SQLException {
        return slotDAO.getOpenSlots();
    }

    private List<MergeTable> buildAssignments(Reservation reservation, List<Table> tables) {
        List<MergeTable> assignments = new ArrayList<>();
        String groupId = "RES-" + reservation.getId();

        for (Table table : tables) {
            int seats = Math.max(1, table.getSeats());
            MergeTable mergeTable = new MergeTable(reservation, table, seats, groupId);
            assignments.add(mergeTable);
        }
        return assignments;
    }

    private void notifyCustomer(Reservation reservation, String message, TypeNotification type) {
        try {
            Notification notification = new Notification(reservation.getCustomer(), message, type);
            notificationDAO.addNotification(notification);
        } catch (SQLException e) {
            // non bloccare il flusso della prenotazione, ma loggare l'errore
            System.err.println("Failed to persist notification: " + e.getMessage());
        }
    }
}