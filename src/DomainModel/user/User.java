package DomainModel.user;

import DomainModel.valueObject.Email;

public class User {

    private int id;
    private String username;
    private Email email;          // puoi sostituire con Email value object se vuoi
    private String passwordHash;   // PASSWORD HASH con BCrypt
    private int fidalityPoints;
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
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.fidalityPoints = 0; // default iniziale
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
        this.username = username;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getFidalityPoints() {
        return fidalityPoints;
    }

    public void setFidalityPoints(int fidalityPoints) {
        this.fidalityPoints = fidalityPoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
