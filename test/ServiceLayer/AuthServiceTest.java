package ServiceLayer;

import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ORM.UserDAO;
import ServiceLayer.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private FakeUserDAO dao;
    private AuthService service;

    @BeforeEach
    void setUp() {
        dao = new FakeUserDAO();
        service = new AuthService(dao);
    }

    @Test
    void authenticateAcceptsCorrectPasswordAndRejectsWrongOne() throws Exception {
        dao.user = user("secret");

        assertTrue(service.authenticate("mario@example.com", "secret").isPresent());
        assertTrue(service.authenticate("mario@example.com", "wrong").isEmpty());
    }

    @Test
    void authenticateRejectsBlankCredentials() {
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("", "secret"));
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("mario@example.com", " "));
    }

    @Test
    void registerHashesPasswordAndCreatesCustomer() throws Exception {
        User registered = service.registerClient(
                "mario", "mario@example.com", "secret", "Mario", "Rossi");

        assertSame(registered, dao.added);
        assertEquals(Role.CUSTOMER, registered.getRole());
        assertNotEquals("secret", registered.getPasswordHash());
        assertTrue(BCrypt.checkpw("secret", registered.getPasswordHash()));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        dao.emailExists = true;
        assertThrows(IllegalArgumentException.class, () ->
                service.registerClient("mario", "mario@example.com", "secret", "Mario", "Rossi"));
        assertNull(dao.added);
    }

    @Test
    void resetPasswordUpdatesStoredHash() throws Exception {
        dao.user = user("old");
        service.resetPasswordByEmail("mario@example.com", "new-secret");
        assertTrue(BCrypt.checkpw("new-secret", dao.updated.getPasswordHash()));
    }

    private User user(String rawPassword) {
        return new User("mario", new Email("mario@example.com"),
                BCrypt.hashpw(rawPassword, BCrypt.gensalt()),
                "Mario", "Rossi", Role.CUSTOMER);
    }

    private static class FakeUserDAO extends UserDAO {
        private User user;
        private User added;
        private User updated;
        private boolean emailExists;

        @Override
        public Optional<User> getUserByEmail(String email) {
            return Optional.ofNullable(user);
        }

        @Override
        public boolean emailExists(String email) {
            return emailExists;
        }

        @Override
        public void addUser(User user) {
            added = user;
            user.setId(1);
        }

        @Override
        public void updateUser(User user) {
            updated = user;
        }
    }
}
