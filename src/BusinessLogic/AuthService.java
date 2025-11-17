package BusinessLogic;

import DomainModel.user.User;
import DomainModel.user.Role;
import DomainModel.valueObject.Email;
import ORM.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Servizio applicativo per l'autenticazione e la registrazione degli utenti.
 * Si occupa di:
 *  - autenticare un utente dato email e password
 *  - registrare un nuovo client (CUSTOMER)
 *
 * Non fa accesso diretto al DB (delegato a UserDAO)
 * Non fa input/output (questo è compito dei controller / UI).
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Prova ad autenticare un utente dato email e password.
     *
     * @param email      email inserita nel form di login
     * @param rawPassword password in chiaro inserita nel form (prima di eventuale hashing)
     * @return Optional con l'utente autenticato, vuoto se credenziali non valide
     */
    public Optional<User> authenticate(String email, String rawPassword) throws SQLException {
        // Validazione input utente
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be empty");

        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("Password cannot be empty");

        // Recupero utente
        Optional<User> maybeUser = userDAO.getUserByEmail(email);

        if (maybeUser.isEmpty())
            return Optional.empty(); // utente non trovato

        User user = maybeUser.get();

        // 2. verifico la password
        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash()))
            return Optional.empty();

        // 3. qui l'utente è autenticato
        // Il ruolo (CLIENT / STAFF / OWNER) può stare:
        //  - o in user.getRole() (enum Role)
        //  - oppure nel tipo concreto (Client, Staff, Owner)
        return Optional.of(user);
    }


     // Esempio di metodo di registrazione di un nuovo client (opzionale).
    public User registerClient(String username,
                               String email,
                               String rawPassword,
                               String name,
                               String surname) throws SQLException {

        // Validazioni base
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Controllo se esiste già un utente con questa email
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Validazione formato email tramite value object
        Email emailVO = new Email(email); // se formato errato, lancia IllegalArgumentException

        // Controllo se esiste già un utente con questa email
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Hash della password con BCrypt
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        // Creo l'utente di dominio
        User newUser = new User(
                username,
                emailVO,
                hashedPassword,
                name,
                surname,
                Role.CUSTOMER          // di default è un cliente
        );
        newUser.setFidelityPoints(0);

        // Salvo nel DB (imposta anche l'id)
        userDAO.addUser(newUser);

        return newUser;
    }

    /**
     * Esempio di metodo per cambiare password (opzionale).
     */
    public void changePassword(User user, String newRawPassword) throws SQLException {

        if (newRawPassword == null || newRawPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        String hashedPassword = BCrypt.hashpw(newRawPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);

        userDAO.updateUser(user);
    }
}