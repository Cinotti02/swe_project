package DomainModel.valueObject;

public class Email {
    private final String value;

    public Email(String value) {
        if (value == null)
            throw new IllegalArgumentException("Invalid email format");
        String normalized = value.trim();
        if (normalized.isEmpty() || !normalized.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Invalid email format");

        this.value = normalized;
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Email))
            return false;
        Email email = (Email) o;
        return value.equalsIgnoreCase(email.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase().hashCode();
    }
}
