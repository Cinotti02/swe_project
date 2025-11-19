package DomainModel.valueObject;

public class Email {
    private final String value;

    public Email(String value) {
        if (value == null || !value.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
            throw new IllegalArgumentException("Invalid email format");
        this.value = value;
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
