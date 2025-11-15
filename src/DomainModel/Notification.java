package DomainModel;

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

    public Notification(User recipient,
                        String message,
                        TypeNotification type) {

        if (recipient == null)
            throw new IllegalArgumentException("Recipient cannot be null");

        if (message == null || message.isBlank())
            throw new IllegalArgumentException("Message cannot be empty");

        if (type == null)
            throw new IllegalArgumentException("Notification type cannot be null");

        this.recipient = recipient;
        this.message = message;
        this.type = type;
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
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TypeNotification getType() {
        return type;
    }

    public void setType(TypeNotification type) {
        this.type = type;
    }

    public StatusNotification getStatus() {
        return status;
    }

    public void setStatus(StatusNotification status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
// ----------------- Metodi di dominio -----------------

    public void markRead() {
        if (!status.isRead()) {
            this.status = StatusNotification.READ;
            this.readAt = LocalDateTime.now();
        }
    }

    public boolean isRead() {
        return status.isRead();
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