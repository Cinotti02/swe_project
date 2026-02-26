package DomainModel.notification;

import DomainModel.user.User;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private User recipient;
    private String message;
    private TypeNotification type;        // CONFIRMATION, REMINDER, UPDATE, ALERT
    private StatusNotification status;    // SENT, READ, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime readAt;         // può essere null se non letta

    public Notification() {}

    public Notification(User recipient, String message, TypeNotification type) {
        setRecipient(recipient);
        setMessage(message);
        setType(type);
        this.status = StatusNotification.SENT;
        this.createdAt = LocalDateTime.now();
    }

    // ----------------- Getter & Setter -----------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        if (recipient == null)
            throw new IllegalArgumentException("Recipient cannot be null");
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message == null || message.isBlank())
            throw new IllegalArgumentException("Message cannot be empty");
        this.message = message;
    }

    public TypeNotification getType() {
        return type;
    }

    public void setType(TypeNotification type) {
        if (type == null)
            throw new IllegalArgumentException("Notification type cannot be null");
        this.type = type;
    }

    public StatusNotification getStatus() {
        return status;
    }

    public void setStatus(StatusNotification status) {
        if (status == null)
            throw new IllegalArgumentException("Notification status cannot be null");
        this.status = status;
        if (status == StatusNotification.READ && this.readAt == null)
            this.readAt = LocalDateTime.now();
        if (status != StatusNotification.READ)
            this.readAt = null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null)
            throw new IllegalArgumentException("Created at cannot be null");
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
        if (readAt != null)
            setStatus(StatusNotification.READ);
        else if (this.status == StatusNotification.READ)
            this.status = StatusNotification.SENT;
    }

// ----------------- Metodi di dominio -----------------

    public void markRead() {
        if (status == null || !status.isRead()) {
            this.status = StatusNotification.READ;
            this.readAt = LocalDateTime.now();
        }
    }

    public void markFailed() {
        this.status = StatusNotification.FAILED;
        this.readAt = null;
    }

    public boolean isRead() {
        return status != null && status.isRead();
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipient=" + (recipient != null ? recipient.getUsername() : "null") +
                ", type=" + type +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}