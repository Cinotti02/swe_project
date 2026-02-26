package DomainModel.valueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;

    public Money(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.amount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public Money(BigDecimal amount) {
        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount; }

    public Money add(Money other) {
        if (other == null)
            throw new IllegalArgumentException("Other money cannot be null");
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(int qty) {
        if (qty < 0)
            throw new IllegalArgumentException("Quantity cannot be negative");
        return new Money(this.amount.multiply(BigDecimal.valueOf(qty)));
    }

    @Override
    public String toString() {
        return amount.toString() + "€";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }
}
