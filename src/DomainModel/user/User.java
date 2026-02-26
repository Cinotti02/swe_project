package DomainModel.user;

import DomainModel.valueObject.Email;

public class User {

    private int id;
    private String username;
    private Email email;          // puoi sostituire con Email value object se vuoi
    private String passwordHash;   // PASSWORD HASH con BCrypt
    private int fidelityPoints;
    private String name;
    private String surname;
    private Role role;             // enum Role (CUSTOMER, STAFF, OWNER)

    // -----------------------------------------------------
    // Costruttori
    // -----------------------------------------------------

    public User() {
        // costruttore vuoto per JDBC, DAO, ecc.
    }

    public User(String username, Email email, String passwordHash,
                String name, String surname, Role role) {
        setUsername(username);
        setEmail(email);
        setPasswordHash(passwordHash);
        setName(name);
        setSurname(surname);
        setRole(role);
        this.fidelityPoints = 0; // default iniziale
    }

    // -----------------------------------------------------
    // Getter e Setter
    // -----------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty");
        this.username = username;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        if (email == null)
            throw new IllegalArgumentException("Email cannot be null");
        this.email = email;
    }

    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank())
            throw new IllegalArgumentException("Password hash cannot be empty");
        this.passwordHash = passwordHash;
    }

    public int getFidelityPoints() {
        return fidelityPoints;
    }

    public void setFidelityPoints(int fidelityPoints) {
        if (fidelityPoints < 0)
            throw new IllegalArgumentException("Fidelity points cannot be negative");
        this.fidelityPoints = fidelityPoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        if (surname == null || surname.isBlank())
            throw new IllegalArgumentException("Surname cannot be empty");
        this.surname = surname;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null)
            throw new IllegalArgumentException("Role cannot be null");
    }

    // -----------------------------------------------------
    // Metodi di utilità
    // -----------------------------------------------------

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean isStaff() {
        return role == Role.STAFF;
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", role=" + role +
                '}';
    }
}
