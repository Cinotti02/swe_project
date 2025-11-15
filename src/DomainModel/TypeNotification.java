package DomainModel;

public enum TypeNotification {
    CONFIRMATION,
    REMINDER,
    UPDATE,
    ALERT;

    public boolean isConfirmed() {
        return this == CONFIRMATION;
    }
    public boolean isReminder() {
        return this == REMINDER;
    }
    public boolean isUpdate() {
        return this == UPDATE;
    }
    public boolean isAlert() {
        return this == ALERT;
    }
}
