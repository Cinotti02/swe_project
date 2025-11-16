package DomainModel.order;

public enum OrderStatus {
    CREATED,
    PREPARING,
    READY,
    RETIRED,
    CANCELLED;

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
        return this == CANCELLED;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CREATED    -> next == PREPARING || next == CANCELLED;
            case PREPARING  -> next == READY || next == CANCELLED;
            case READY      -> next == RETIRED || next == CANCELLED;
            case RETIRED    -> false;
            case CANCELLED  -> false;
        };
    }
}
