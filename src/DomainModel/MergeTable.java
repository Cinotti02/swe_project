package DomainModel;

public class MergeTable {

    private int id;
    private Reservation reservation;  // prenotazione a cui appartiene il gruppo di tavoli
    private Table table;              // singolo tavolo della combinazione
    private int seatsAssigned;        // posti assegnati su questo tavolo
    private String mergedGroupId;     // identificatore del gruppo di tavoli uniti

    public MergeTable() {}

    public MergeTable(Reservation reservation,
                      Table table,
                      int seatsAssigned,
                      String mergedGroupId) {

        if (reservation == null)
            throw new IllegalArgumentException("Reservation cannot be null");
        if (table == null)
            throw new IllegalArgumentException("Table cannot be null");
        if (seatsAssigned <= 0)
            throw new IllegalArgumentException("Seats assigned must be > 0");
        if (mergedGroupId == null || mergedGroupId.isBlank())
            throw new IllegalArgumentException("Merged group id cannot be empty");

        this.reservation = reservation;
        this.table = table;
        this.seatsAssigned = seatsAssigned;
        this.mergedGroupId = mergedGroupId;
    }

    // ---------------- Getter & Setter ----------------

    public int getId() {
        return id;
    }

    public void setId(int id) {   // impostato dal DAO
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        if (reservation == null)
            throw new IllegalArgumentException("Reservation cannot be null");
        this.reservation = reservation;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        if (table == null)
            throw new IllegalArgumentException("Table cannot be null");
        this.table = table;
    }

    public int getSeatsAssigned() {
        return seatsAssigned;
    }

    public void setSeatsAssigned(int seatsAssigned) {
        if (seatsAssigned <= 0)
            throw new IllegalArgumentException("Seats assigned must be > 0");
        this.seatsAssigned = seatsAssigned;
    }

    public String getMergedGroupId() {
        return mergedGroupId;
    }

    public void setMergedGroupId(String mergedGroupId) {
        if (mergedGroupId == null || mergedGroupId.isBlank())
            throw new IllegalArgumentException("Merged group id cannot be empty");
        this.mergedGroupId = mergedGroupId;
    }

    // --------------- Utility ---------------

    public int getTableNumber() {
        return table != null ? table.getNumber() : -1;
    }

    @Override
    public String toString() {
        return "MergeTable{" +
                "id=" + id +
                ", reservationId=" + (reservation != null ? reservation.getId() : null) +
                ", tableNumber=" + (table != null ? table.getNumber() : null) +
                ", seatsAssigned=" + seatsAssigned +
                ", mergedGroupId='" + mergedGroupId + '\'' +
                '}';
    }
}