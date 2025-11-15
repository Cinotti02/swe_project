package DomainModel;

public enum StatusNotification {
    SENT,
    READ,
    FAILED;

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isRead() {
        return this == READ;
    }

    public boolean isFailed() {
        return this == FAILED;
    }
}