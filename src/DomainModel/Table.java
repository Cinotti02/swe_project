package DomainModel;

public class Table {

    private int id;          // opzionale, necessario per DAO/DB
    private int number;      // numero identificativo del tavolo
    private int seats;       // numero di posti a sedere
    private boolean available;
    private boolean joinable;   // se può essere unito ad altri
    private String location;

    public Table() {}

    public Table(int number, int seats, boolean joinable, String location) {
        if (number <= 0)
            throw new IllegalArgumentException("Table number must be positive");
        if (seats <= 0)
            throw new IllegalArgumentException("Seats must be > 0");

        this.number = number;
        this.seats = seats;
        this.joinable = joinable;
        this.location = location;
        this.available = true; // di default il tavolo è disponibile
    }

    // ---------------------- Getter / Setter ----------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        if (number <= 0)
            throw new IllegalArgumentException("Table number must be positive");
        this.number = number;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        if (seats <= 0)
            throw new IllegalArgumentException("Seats must be > 0");
        this.seats = seats;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isJoinable() {
        return joinable;
    }

    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.isBlank())
            throw new IllegalArgumentException("Location cannot be empty");
        this.location = location;
    }

    // ---------------------- Utility ----------------------

    public boolean canFitAlone(int guests) {
        return guests <= seats;
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                ", seats=" + seats +
                ", available=" + available +
                ", joinable=" + joinable +
                ", location='" + location + '\'' +
                '}';
    }
}