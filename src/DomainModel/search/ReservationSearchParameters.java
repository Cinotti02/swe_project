package DomainModel.search;

import DomainModel.reservation.ReservationStatus;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Criteri di ricerca per le prenotazioni.
 */
public class ReservationSearchParameters {

    private LocalDate date;
    private Integer customerId;
    private ReservationStatus status;

    private ReservationSearchParameters() {
    }

    public static ReservationSearchParameters builder() {
        return new ReservationSearchParameters();
    }

    public ReservationSearchParameters onDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public ReservationSearchParameters forCustomer(Integer customerId) {
        this.customerId = customerId;
        return this;
    }

    public ReservationSearchParameters withStatus(ReservationStatus status) {
        this.status = status;
        return this;
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    public Optional<Integer> getCustomerId() {
        return Optional.ofNullable(customerId);
    }

    public Optional<ReservationStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public boolean hasFilters() {
        return date != null || customerId != null || status != null;
    }
}