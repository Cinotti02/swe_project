package DomainModel;

import java.time.LocalTime;

public class Slot {

    private int id;
    private LocalTime startTime;   // ora di inizio (es. 19:30)
    private LocalTime endTime;     // ora di fine   (es. 21:30)
    private boolean closed;        // se lo slot è chiuso/non prenotabile

    public Slot() {}

    public Slot(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null)
            throw new IllegalArgumentException("Start and end time cannot be null");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("End time must be after start time");

        this.startTime = startTime;
        this.endTime = endTime;
        this.closed = false; // di default lo slot è prenotabile
    }

    // ---------------- Getter & Setter ----------------

    public int getId() {
        return id;
    }

    public void setId(int id) { // impostato dal DAO
        this.id = id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        if (startTime == null)
            throw new IllegalArgumentException("Start time cannot be null");
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        if (endTime == null)
            throw new IllegalArgumentException("End time cannot be null");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("End time must be after start time");
        this.endTime = endTime;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    // ---------------- Metodi di dominio ----------------

    /** Ritorna true se l'ora passata cade dentro lo slot. */
    public boolean contains(LocalTime time) {
        if (time == null) return false;
        return ( !time.isBefore(startTime) && time.isBefore(endTime) );
    }

    /** Controlla se due slot si sovrappongono. */
    public boolean overlaps(Slot other) {
        if (other == null) return false;
        return this.startTime.isBefore(other.endTime)
                && other.startTime.isBefore(this.endTime);
    }

    @Override
    public String toString() {
        return "Slot{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", closed=" + closed +
                '}';
    }
}