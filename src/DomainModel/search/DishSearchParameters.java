package DomainModel.search;

import java.math.BigDecimal;
import java.util.Optional;

public class DishSearchParameters {
    private Integer categoryId;
    private Boolean onlyAvailable;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String nameContains;

    public static DishSearchParameters builder() {
        return new DishSearchParameters();
    }

    public DishSearchParameters getIdCategory(Integer categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public DishSearchParameters getOnlyAvailable(Boolean onlyAvailable) {
        this.onlyAvailable = onlyAvailable;
        return this;
    }

    public DishSearchParameters getMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
        return this;
    }

    public DishSearchParameters getMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public DishSearchParameters getNameContains(String nameContains) {
        this.nameContains = nameContains;
        return this;
    }

    public Optional<Integer> getCategoryId() {
        return Optional.ofNullable(categoryId);
    }
    public Optional<Boolean> getOnlyAvailable() {
        return Optional.ofNullable(onlyAvailable);
    }
    public Optional<BigDecimal> getMinPrice() {
        return Optional.ofNullable(minPrice);
    }
    public Optional<BigDecimal> getMaxPrice() {
        return Optional.ofNullable(maxPrice);
    }
    public Optional<String> getNameContains() {return Optional.ofNullable(nameContains);}

    public boolean hasFilters() {
        return categoryId != null
                || onlyAvailable != null
                || minPrice != null
                || maxPrice != null
                || nameContains != null;
    }
}