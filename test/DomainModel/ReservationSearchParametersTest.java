package DomainModel;

import DomainModel.reservation.ReservationStatus;
import DomainModel.search.ReservationSearchParameters;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReservationSearchParametersTest {

    @Test
    void defaultStateHasNoFiltersAndEmptyOptionals() {
        ReservationSearchParameters p = ReservationSearchParameters.builder();
        assertFalse(p.hasFilters());
        assertTrue(p.getDate().isEmpty());
        assertTrue(p.getStartDate().isEmpty());
        assertTrue(p.getEndDate().isEmpty());
        assertTrue(p.getCustomerId().isEmpty());
        assertTrue(p.getSlotId().isEmpty());
        assertTrue(p.getMinGuests().isEmpty());
        assertTrue(p.getMaxGuests().isEmpty());
        assertTrue(p.getStatus().isEmpty());
    }


    @Test
    void settersAreChainable() {
        ReservationSearchParameters p = ReservationSearchParameters.builder();
        assertSame(p, p.setDate(LocalDate.of(2026, 1, 1)));
        assertSame(p, p.setStartDate(LocalDate.of(2026, 1, 1)));
        assertSame(p, p.setEndDate(LocalDate.of(2026, 1, 2)));
        assertSame(p, p.setCustomerId(1));
        assertSame(p, p.setSlotId(2));
        assertSame(p, p.setMinGuests(2));
        assertSame(p, p.setMaxGuests(3));
        assertSame(p, p.setStatus(ReservationStatus.CONFIRMED));
    }

    @Test
    void idsDateAndStatusSetters() {
        LocalDate date = LocalDate.of(2026, 4, 1);
        ReservationSearchParameters p = ReservationSearchParameters.builder()
                .setDate(date)
                .setCustomerId(12)
                .setSlotId(2)
                .setStatus(ReservationStatus.CONFIRMED);
        assertEquals(date, p.getDate().orElseThrow());
        assertEquals(12, p.getCustomerId().orElseThrow());
        assertEquals(2, p.getSlotId().orElseThrow());
        assertEquals(ReservationStatus.CONFIRMED, p.getStatus().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> ReservationSearchParameters.builder().setCustomerId(0));
        assertThrows(IllegalArgumentException.class, () -> ReservationSearchParameters.builder().setSlotId(-2));
    }

    @Test
    void dateRangeValidationAndBoundary() {
        LocalDate d = LocalDate.of(2026, 2, 10);
        ReservationSearchParameters equalDates = ReservationSearchParameters.builder().setStartDate(d).setEndDate(d);
        assertEquals(d, equalDates.getStartDate().orElseThrow());
        assertEquals(d, equalDates.getEndDate().orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> ReservationSearchParameters.builder().setEndDate(LocalDate.of(2026, 1, 1)).setStartDate(LocalDate.of(2026, 2, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> ReservationSearchParameters.builder().setStartDate(LocalDate.of(2026, 2, 1)).setEndDate(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void guestsValidationAndBoundary() {
        ReservationSearchParameters equalGuests = ReservationSearchParameters.builder().setMinGuests(4).setMaxGuests(4);
        assertEquals(4, equalGuests.getMinGuests().orElseThrow());
        assertEquals(4, equalGuests.getMaxGuests().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> ReservationSearchParameters.builder().setMinGuests(0));
        assertThrows(IllegalArgumentException.class, () -> ReservationSearchParameters.builder().setMaxGuests(0));
        assertThrows(IllegalArgumentException.class,
                () -> ReservationSearchParameters.builder().setMaxGuests(3).setMinGuests(4));
        assertThrows(IllegalArgumentException.class,
                () -> ReservationSearchParameters.builder().setMinGuests(5).setMaxGuests(4));
    }

    @Test
    void hasFiltersTransitionsIncludingNullReset() {
        ReservationSearchParameters p = ReservationSearchParameters.builder();
        assertFalse(p.hasFilters());
        p.setDate(LocalDate.of(2026, 1, 1));
        assertTrue(p.hasFilters());
        p.setDate(null);
        assertFalse(p.hasFilters());
        p.setDate(LocalDate.of(2026, 1, 1)).setStartDate(LocalDate.of(2026, 1, 1)).setEndDate(LocalDate.of(2026, 1, 2))
                .setCustomerId(1).setSlotId(2).setMinGuests(2).setMaxGuests(3).setStatus(ReservationStatus.CONFIRMED);
        assertTrue(p.hasFilters());
        p.setDate(null).setStartDate(null).setEndDate(null).setCustomerId(null).setSlotId(null)
                .setMinGuests(null).setMaxGuests(null).setStatus(null);
        assertFalse(p.hasFilters());
    }
}