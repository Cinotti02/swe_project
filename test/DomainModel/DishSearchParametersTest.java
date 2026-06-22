package DomainModel;

import DomainModel.search.DishSearchParameters;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DishSearchParametersTest {

    @Test
    void builder() {
        DishSearchParameters params = DishSearchParameters.builder();
        assertNotNull(params);
        assertFalse(params.hasFilters());
    }

    @Test
    void settersAreChainable() {
        DishSearchParameters params = DishSearchParameters.builder();
        assertSame(params, params.setCategoryId(1));
        assertSame(params, params.setOnlyAvailable(true));
        assertSame(params, params.setMinPrice(new BigDecimal("1")));
        assertSame(params, params.setMaxPrice(new BigDecimal("10")));
        assertSame(params, params.setNameContains("pasta"));
    }

    @Test
    void categoryIdValidationAndNullReset() {
        DishSearchParameters params = DishSearchParameters.builder().setCategoryId(3);
        assertEquals(3, params.getCategoryId().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> DishSearchParameters.builder().setCategoryId(0));
        assertThrows(IllegalArgumentException.class, () -> DishSearchParameters.builder().setCategoryId(-1));
        params.setCategoryId(null);
        assertTrue(params.getCategoryId().isEmpty());
    }

    @Test
    void onlyAvailableCanBeTrueFalseOrNull() {
        DishSearchParameters params = DishSearchParameters.builder().setOnlyAvailable(true);
        assertEquals(true, params.getOnlyAvailable().orElseThrow());
        params.setOnlyAvailable(false);
        assertEquals(false, params.getOnlyAvailable().orElseThrow());
        params.setOnlyAvailable(null);
        assertTrue(params.getOnlyAvailable().isEmpty());
    }

    @Test
    void minMaxPriceValidationAndNullReset() {
        DishSearchParameters params = DishSearchParameters.builder();
        params.setMinPrice(new BigDecimal("5.50")).setMaxPrice(new BigDecimal("15.00"));
        assertEquals(new BigDecimal("5.50"), params.getMinPrice().orElseThrow());
        assertEquals(new BigDecimal("15.00"), params.getMaxPrice().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> DishSearchParameters.builder().setMinPrice(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> DishSearchParameters.builder().setMaxPrice(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,
                () -> DishSearchParameters.builder().setMaxPrice(new BigDecimal("4")).setMinPrice(new BigDecimal("5")));
        assertThrows(IllegalArgumentException.class,
                () -> DishSearchParameters.builder().setMinPrice(new BigDecimal("5")).setMaxPrice(new BigDecimal("4")));
        params.setMinPrice(null).setMaxPrice(null);
        assertTrue(params.getMinPrice().isEmpty());
        assertTrue(params.getMaxPrice().isEmpty());
    }

    @Test
    void nameContainsTrimBlankAndNull() {
        DishSearchParameters params = DishSearchParameters.builder().setNameContains("  pasta  ");
        assertEquals("pasta", params.getNameContains().orElseThrow());
        params.setNameContains("   ");
        assertTrue(params.getNameContains().isEmpty());
        params.setNameContains(null);
        assertTrue(params.getNameContains().isEmpty());
    }

    @Test
    void hasFiltersTransitions() {
        DishSearchParameters params = DishSearchParameters.builder();
        assertFalse(params.hasFilters());
        params.setNameContains("pizza");
        assertTrue(params.hasFilters());
        params.setNameContains(null);
        assertFalse(params.hasFilters());
    }
}