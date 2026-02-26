package DomainModel.search;

import java.math.BigDecimal;
import java.util.Optional;

public class DishSearchParameters {
    private Integer categoryId;
    private Boolean onlyAvailable;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String nameContains;

    private DishSearchParameters() {
    }

    public static DishSearchParameters builder() {
        return new DishSearchParameters();
    }

    public DishSearchParameters setCategoryId(Integer categoryId) {
        if (categoryId != null && categoryId <= 0)
            throw new IllegalArgumentException("Category id must be positive");
        this.categoryId = categoryId;
        return this;
    }

    public DishSearchParameters setOnlyAvailable(Boolean onlyAvailable) {
        this.onlyAvailable = onlyAvailable;
        return this;
    }

    public DishSearchParameters setMinPrice(BigDecimal minPrice) {
        if (minPrice != null && minPrice.signum() < 0)
            throw new IllegalArgumentException("Min price cannot be negative");
        if (maxPrice != null && minPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        this.minPrice = minPrice;
        return this;
    }

    public DishSearchParameters setMaxPrice(BigDecimal maxPrice) {
        if (maxPrice != null && maxPrice.signum() < 0)
            throw new IllegalArgumentException("Max price cannot be negative");
        if (minPrice != null && maxPrice != null && maxPrice.compareTo(minPrice) < 0)
            throw new IllegalArgumentException("Max price cannot be lower than min price");
        this.maxPrice = maxPrice;
        return this;
    }

    public DishSearchParameters setNameContains(String nameContains) {
        this.nameContains = (nameContains != null) ? nameContains.trim() : null;
        if (this.nameContains != null && this.nameContains.isEmpty())
            this.nameContains = null;
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