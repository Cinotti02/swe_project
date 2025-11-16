package DomainModel.menu;

public class Category {
    int id;
    String name;
    String description;
    private boolean active;      // se è visibile/ordinabile oppure no

    public Category() {}

    public Category(String name, String description) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        this.name = name;
        this.description = description;
        this.active = true;
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
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ---------------- Metodi di dominio ----------------

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
