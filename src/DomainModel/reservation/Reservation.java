package DomainModel.reservation;

import DomainModel.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reservation {

    private int id;
    private User customer;
    private LocalDateTime reservDate;   // data + ora della prenotazione
    private Slot timeSlot;
    private int numberOfGuests;
    private String notes;
    private ReservationStatus status;
    private List<MergeTable> tables;    // lista dei tavoli uniti assegnati
    private LocalDateTime createdAt;

    public Reservation() {
        this.tables = new ArrayList<>();
    }

    public Reservation(User customer,
                       LocalDateTime reservDate,
                       Slot timeSlot,
                       int numberOfGuests,
                       String notes) {

        if (customer == null)
            throw new IllegalArgumentException("Customer cannot be null");

        if (reservDate == null)
            throw new IllegalArgumentException("Reservation date cannot be null");

        if (timeSlot == null)
            throw new IllegalArgumentException("Time slot cannot be null");

        if (numberOfGuests <= 0)
            throw new IllegalArgumentException("Number of guests must be > 0");

        setCustomer(customer);
        setReservDate(reservDate);
        setTimeSlot(timeSlot);
        setNumberOfGuests(numberOfGuests);
        this.notes = notes;
        setStatus(ReservationStatus.CREATED);
        this.createdAt = LocalDateTime.now();
        this.tables = new ArrayList<>();
    }

    // -------------------- Getter & Setter --------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        if (customer == null)
            throw new IllegalArgumentException("Customer cannot be null");
        this.customer = customer;
    }

    public LocalDateTime getReservDate() {
        return reservDate;
    }

    public void setReservDate(LocalDateTime reservDate) {
        if (reservDate == null)
            throw new IllegalArgumentException("Reservation date cannot be null");
        this.reservDate = reservDate;
    }

    public Slot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(Slot timeSlot) {
        if (timeSlot == null)
            throw new IllegalArgumentException("Time slot cannot be null");
        this.timeSlot = timeSlot;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        if (numberOfGuests <= 0)
            throw new IllegalArgumentException("Number of guests must be > 0");
        this.numberOfGuests = numberOfGuests;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Reservation status cannot be null");
        if (this.status != null && this.status != status && !this.status.canTransitionTo(status))
            throw new IllegalStateException("Invalid reservation transition from " + this.status + " to " + status);
        this.status = status;
    }

    public List<MergeTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public void setTables(List<MergeTable> tables) {
        if (tables == null) {
            this.tables = new ArrayList<>();
            return;
        }

        List<MergeTable> copy = new ArrayList<>();
        for (MergeTable table : tables) {
            if (table == null)
                throw new IllegalArgumentException("Tables list cannot contain null elements");
            copy.add(table);
        }
        this.tables = copy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // -------------------- Domain Logic --------------------

    public void addTable(MergeTable mergeTable) {
        if (mergeTable == null)
            throw new IllegalArgumentException("Merge table cannot be null");
        this.tables.add(mergeTable);
    }

    public boolean isCreated()   { return status == ReservationStatus.CREATED; }
    public boolean isConfirmed() { return status == ReservationStatus.CONFIRMED; }
    public boolean isCheckedIn() { return status == ReservationStatus.CHECKED_IN; }
    public boolean isCompleted() { return status == ReservationStatus.COMPLETED; }
    public boolean isNoShow()    { return status == ReservationStatus.NO_SHOW; }
    public boolean isCanceled()  { return status == ReservationStatus.CANCELED; }

    public void confirm() {
        transitionTo(ReservationStatus.CONFIRMED);
    }

    public void checkIn() {
        transitionTo(ReservationStatus.CHECKED_IN);
    }

    public void complete() {
        transitionTo(ReservationStatus.COMPLETED);
    }

    public void cancel() {
        transitionTo(ReservationStatus.CANCELED);
    }

    public void markNoShow() {
        transitionTo(ReservationStatus.NO_SHOW);
    }


    private void transitionTo(ReservationStatus nextStatus) {
        if (nextStatus == null)
            throw new IllegalArgumentException("Next status cannot be null");
        if (status == null)
            throw new IllegalStateException("Current status cannot be null");

        if (!status.canTransitionTo(nextStatus))
            throw new IllegalStateException("Invalid reservation transition from " + status + " to " + nextStatus);

        setStatus(nextStatus);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getUsername() : null) +
                ", guests=" + numberOfGuests +
                ", status=" + status +
                ", reservDate=" + reservDate +
                ", timeSlot=" + timeSlot +
                ", tables=" + tables.size() +
                '}';
    }
}