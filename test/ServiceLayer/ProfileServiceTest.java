package ServiceLayer;

import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ORM.UserDAO;
import ServiceLayer.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServiceTest {

    private FakeUserDAO dao;
    private ProfileService service;
    private User user;

    @BeforeEach
    void setUp() {
        dao = new FakeUserDAO();
        service = new ProfileService(dao);
        user = new User("mario", new Email("mario@example.com"), "hash",
                "Mario", "Rossi", Role.CUSTOMER);
        user.setId(4);
        dao.user = user;
    }

    @Test
    void getProfileReturnsUserOrFailsClearly() throws Exception {
        assertSame(user, service.getProfile(4));
        dao.user = null;
        assertThrows(IllegalArgumentException.class, () -> service.getProfile(99));
    }

    @Test
    void updatePersonalInfoMutatesAndPersistsUser() throws Exception {
        service.updatePersonalInfo(user, "nuovo", "Luigi", "Verdi");
        assertEquals("nuovo", user.getUsername());
        assertEquals("Luigi", user.getName());
        assertEquals("Verdi", user.getSurname());
        assertSame(user, dao.updated);
    }

    @Test
    void updateEmailUsesEmailValueObjectAndPersists() throws Exception {
        service.updateEmail(user, "NEW@example.com");
        assertEquals("NEW@example.com", user.getEmailValue());
        assertSame(user, dao.updated);
        assertThrows(IllegalArgumentException.class,
                () -> service.updateEmail(user, "invalid"));
    }

    @Test
    void fidelityPointsNeverBecomeNegative() throws Exception {
        user.setFidelityPoints(5);
        service.addFidelityPoints(user, -20);
        assertEquals(0, user.getFidelityPoints());
        assertSame(user, dao.updated);
    }

    private static class FakeUserDAO extends UserDAO {
        private User user;
        private User updated;

        @Override
        public Optional<User> getUserById(int id) {
            return Optional.ofNullable(user);
        }

        @Override
        public void updateUser(User user) {
            updated = user;
        }
    }
}
