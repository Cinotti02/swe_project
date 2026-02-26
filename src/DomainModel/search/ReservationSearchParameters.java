package DomainModel.search;

import DomainModel.reservation.ReservationStatus;

import java.time.LocalDate;
import java.util.Optional;


// Criteri di ricerca per le prenotazioni.

public class ReservationSearchParameters {

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer customerId;
    private Integer slotId;
    private Integer minGuests;
    private Integer maxGuests;
    private ReservationStatus status;

    private ReservationSearchParameters() {
    }

    public static ReservationSearchParameters builder() {
        return new ReservationSearchParameters();
    }

    public ReservationSearchParameters setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public ReservationSearchParameters setStartDate(LocalDate startDate) {
        if (endDate != null && startDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date");
        this.startDate = startDate;
        return this;
    }

    public ReservationSearchParameters setEndDate(LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date cannot be before start date");
        this.endDate = endDate;
        return this;
    }

    public ReservationSearchParameters setCustomerId(Integer customerId) {
        if (customerId != null && customerId <= 0)
            throw new IllegalArgumentException("Customer id must be positive");
        this.customerId = customerId;
        return this;
    }

    public ReservationSearchParameters setSlotId(Integer slotId) {
        if (slotId != null && slotId <= 0)
            throw new IllegalArgumentException("Slot id must be positive");
        this.slotId = slotId;
        return this;
    }

    public ReservationSearchParameters setMinGuests(Integer minGuests) {
        if (minGuests != null && minGuests <= 0)
            throw new IllegalArgumentException("Min guests must be > 0");
        if (maxGuests != null && minGuests != null && minGuests > maxGuests)
            throw new IllegalArgumentException("Min guests cannot be greater than max guests");
        this.minGuests = minGuests;
        return this;
    }

    public ReservationSearchParameters setMaxGuests(Integer maxGuests) {
        if (maxGuests != null && maxGuests <= 0)
            throw new IllegalArgumentException("Max guests must be > 0");
        if (minGuests != null && maxGuests != null && maxGuests < minGuests)
            throw new IllegalArgumentException("Max guests cannot be lower than min guests");
        this.maxGuests = maxGuests;
        return this;
    }

    public ReservationSearchParameters setStatus(ReservationStatus status) {
        this.status = status;
        return this;
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public Optional<Integer> getCustomerId() {
        return Optional.ofNullable(customerId);
    }
    public Optional<Integer> getSlotId() {
        return Optional.ofNullable(slotId);
    }

    public Optional<Integer> getMinGuests() {
        return Optional.ofNullable(minGuests);
    }

    public Optional<Integer> getMaxGuests() {
        return Optional.ofNullable(maxGuests);
    }

    public Optional<ReservationStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public boolean hasFilters() {
        return date != null
                || startDate != null
                || endDate != null
                || customerId != null
                || slotId != null
                || minGuests != null
                || maxGuests != null
                || status != null;
    }
}