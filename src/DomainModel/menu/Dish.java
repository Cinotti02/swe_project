package DomainModel.menu;

import DomainModel.valueObject.Money;

public class Dish {
    private int id;
    private String name;
    private String description;
    private Money price;
    private Category category;
    private boolean available;

    public Dish(){}

    public Dish(String name, String description, Money price, Category category) {
        setName(name);
        setDescription(description);
        setPrice(price);
        setCategory(category);
        this.available = true;
    }

    public Dish(String name,
                String description,
                double price,
                Category category) {
        this(name, description, new Money(price), category);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        this.description = description;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        if(price == null) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.price = price;
    }

    public void setPrice(double amount) {
        this.price = new Money(amount);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category == null)
            throw new IllegalArgumentException("Category cannot be null");
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // ------------ Metodi di dominio ------------

    public void markUnavailable() {
        this.available = false;
    }

    public void markAvailable() {
        this.available = true;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + (category != null ? category.getName() : "null") +
                ", available=" + available +
                '}';
    }
}
