package DomainModel.reservation;

public enum ReservationStatus {
    CREATED,
    CONFIRMED,
    CHECKED_IN,
    COMPLETED,
    NO_SHOW,
    CANCELED;

    public boolean isCreated() {
        return this == CREATED;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
    public boolean isCheckedIn() {
        return this == CHECKED_IN;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isNoShow() {
        return this == NO_SHOW;
    }

    public boolean isCanceled() {
        return this == CANCELED;
    }

    public boolean canTransitionTo(ReservationStatus next) {
        return switch (this) {
            case CREATED -> (next == CONFIRMED || next == CANCELED);
            case CONFIRMED -> (next == CHECKED_IN || next == CANCELED || next == NO_SHOW);
            case CHECKED_IN -> (next == COMPLETED);
            case COMPLETED, NO_SHOW, CANCELED -> false;
        };
    }
}
