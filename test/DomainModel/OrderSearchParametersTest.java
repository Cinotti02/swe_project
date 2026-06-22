package DomainModel;

import DomainModel.order.OrderStatus;
import DomainModel.order.PaymentMethod;
import DomainModel.search.OrderSearchParameters;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderSearchParametersTest {

    @Test
    void defaultStateHasNoFiltersAndEmptyOptionals() {
        OrderSearchParameters p = OrderSearchParameters.builder();
        assertFalse(p.hasFilters());
        assertTrue(p.getCustomerId().isEmpty());
        assertTrue(p.getStatus().isEmpty());
        assertTrue(p.getPaymentMethod().isEmpty());
        assertTrue(p.getCategoryId().isEmpty());
        assertTrue(p.getStartDate().isEmpty());
        assertTrue(p.getEndDate().isEmpty());
        assertTrue(p.getMinTotalAmount().isEmpty());
        assertTrue(p.getMaxTotalAmount().isEmpty());
    }

    @Test
    void settersAreChainable() {
        OrderSearchParameters p = OrderSearchParameters.builder();
        assertSame(p, p.setCustomerId(1));
        assertSame(p, p.setStatus(OrderStatus.CREATED));
        assertSame(p, p.setPaymentMethod(PaymentMethod.IN_LOCO));
        assertSame(p, p.setCategoryId(1));
        assertSame(p, p.setStartDate(LocalDate.of(2026, 1, 1)));
        assertSame(p, p.setEndDate(LocalDate.of(2026, 1, 2)));
        assertSame(p, p.setMinTotalAmount(new BigDecimal("1")));
        assertSame(p, p.setMaxTotalAmount(new BigDecimal("2")));
    }

    @Test
    void idAndEnumSetters() {
        OrderSearchParameters p = OrderSearchParameters.builder()
                .setCustomerId(10)
                .setStatus(OrderStatus.READY)
                .setPaymentMethod(PaymentMethod.ONLINE)
                .setCategoryId(2);
        assertEquals(10, p.getCustomerId().orElseThrow());
        assertEquals(OrderStatus.READY, p.getStatus().orElseThrow());
        assertEquals(PaymentMethod.ONLINE, p.getPaymentMethod().orElseThrow());
        assertEquals(2, p.getCategoryId().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> OrderSearchParameters.builder().setCustomerId(0));
        assertThrows(IllegalArgumentException.class, () -> OrderSearchParameters.builder().setCategoryId(-1));
    }

    @Test
    void dateRangeValidationAndBoundary() {
        LocalDate d = LocalDate.of(2026, 2, 10);
        OrderSearchParameters equalDates = OrderSearchParameters.builder().setStartDate(d).setEndDate(d);
        assertEquals(d, equalDates.getStartDate().orElseThrow());
        assertEquals(d, equalDates.getEndDate().orElseThrow());

        assertThrows(IllegalArgumentException.class,
                () -> OrderSearchParameters.builder().setEndDate(LocalDate.of(2026, 1, 1)).setStartDate(LocalDate.of(2026, 1, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> OrderSearchParameters.builder().setStartDate(LocalDate.of(2026, 1, 2)).setEndDate(LocalDate.of(2026, 1, 1)));
    }

    void totalAmountValidationAndBoundary() {
        BigDecimal v = new BigDecimal("20");
        OrderSearchParameters equal = OrderSearchParameters.builder().setMinTotalAmount(v).setMaxTotalAmount(v);
        assertEquals(v, equal.getMinTotalAmount().orElseThrow());
        assertEquals(v, equal.getMaxTotalAmount().orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> OrderSearchParameters.builder().setMinTotalAmount(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,
                () -> OrderSearchParameters.builder().setMaxTotalAmount(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,
                () -> OrderSearchParameters.builder().setMaxTotalAmount(new BigDecimal("10")).setMinTotalAmount(new BigDecimal("20")));
        assertThrows(IllegalArgumentException.class,
                () -> OrderSearchParameters.builder().setMinTotalAmount(new BigDecimal("20")).setMaxTotalAmount(new BigDecimal("10")));
    }

    void hasFiltersTransitionsIncludingNullReset() {
        OrderSearchParameters p = OrderSearchParameters.builder();
        assertFalse(p.hasFilters());
        p.setStatus(OrderStatus.CREATED);
        assertTrue(p.hasFilters());
        p.setStatus(null);
        assertFalse(p.hasFilters());
        p.setCustomerId(1).setStatus(OrderStatus.CREATED).setPaymentMethod(PaymentMethod.IN_LOCO)
                .setCategoryId(2).setStartDate(LocalDate.of(2026, 1, 1)).setEndDate(LocalDate.of(2026, 1, 2))
                .setMinTotalAmount(new BigDecimal("10")).setMaxTotalAmount(new BigDecimal("20"));
        assertTrue(p.hasFilters());
        p.setCustomerId(null).setStatus(null).setPaymentMethod(null).setCategoryId(null)
                .setStartDate(null).setEndDate(null).setMinTotalAmount(null).setMaxTotalAmount(null);
        assertFalse(p.hasFilters());
    }
}