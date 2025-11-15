package DomainModel;

public enum PaymentMethod {
    ONLINE,
    IN_LOCO;

    public boolean isOnline() {
        return this == ONLINE;
    }

    public boolean isInLoco() {
        return this == IN_LOCO;
    }
}
