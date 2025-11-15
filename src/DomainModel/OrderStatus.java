package DomainModel;

public enum OrderStatus {
    CREATED,
    PREPARING,
    READY,
    RETIRED,
    CANCELED;

    public boolean isCreated() {
        return this == CREATED;
    }

    public boolean isPreparing() {
        return this == PREPARING;
    }

    public boolean isReady() {
        return this == READY;
    }

    public boolean isRetired() {
        return this == RETIRED;
    }

    public boolean isCancelled() {
        return this == CANCELED;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CREATED    -> next == PREPARING || next == CANCELED;
            case PREPARING  -> next == READY || next == CANCELED;
            case READY      -> next == RETIRED || next == CANCELED;
            case RETIRED    -> false;
            case CANCELED  -> false;
        };
    }
}
