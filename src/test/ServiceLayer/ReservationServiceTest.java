package test.ServiceLayer;

import DomainModel.notification.Notification;
import DomainModel.reservation.MergeTable;
import DomainModel.reservation.Reservation;
import DomainModel.reservation.ReservationStatus;
import DomainModel.reservation.Slot;
import DomainModel.reservation.Table;
import DomainModel.user.Role;
import DomainModel.user.User;
import DomainModel.valueObject.Email;
import ORM.NotificationDAO;
import ORM.ReservationDAO;
import ORM.SlotDAO;
import ORM.TableDAO;
import ServiceLayer.ReservationService;
import ServiceLayer.TableAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private FakeReservationDAO reservationDAO;
    private FakeTableDAO tableDAO;
    private FakeSlotDAO slotDAO;
    private FakeNotificationDAO notificationDAO;
    private ReservationService service;
    private User customer;
    private Slot slot;

    @BeforeEach
    void setUp() {
        reservationDAO = new FakeReservationDAO();
        tableDAO = new FakeTableDAO();
        slotDAO = new FakeSlotDAO();
        notificationDAO = new FakeNotificationDAO();
        service = new ReservationService(
                reservationDAO,
                tableDAO,
                slotDAO,
                notificationDAO,
                new TableAllocationService()
        );

        customer = user(5, Role.CUSTOMER);
        slot = new Slot(LocalTime.of(19, 0), LocalTime.of(21, 0));
        slot.setId(3);
        slotDAO.slot = slot;
    }

    @Test
    void createReservationPersistsReservationAndTablesAtomically() throws SQLException {
        Table table = table(10, 4);
        tableDAO.tables = List.of(table);

        Reservation reservation = service.createReservation(
                customer,
                LocalDate.of(2026, 7, 1),
                slot.getId(),
                4,
                "finestra"
        );

        assertTrue(reservationDAO.atomicSaveUsed);
        assertEquals(91, reservation.getId());
        assertEquals(List.of(table), reservationDAO.savedTables);
        assertEquals(1, reservation.getTables().size());
        assertEquals("RES-91", reservation.getTables().get(0).getMergedGroupId());
        assertEquals(1, notificationDAO.notifications.size());
    }

    @Test
    void createReservationRejectsClosedSlotBeforePersistence() {
        slot.setClosed(true);

        assertThrows(IllegalStateException.class, () ->
                service.createReservation(customer, LocalDate.now(), slot.getId(), 2, null));
        assertFalse(reservationDAO.atomicSaveUsed);
    }

    @Test
    void createReservationRejectsRequestWhenCapacityIsInsufficient() {
        tableDAO.tables = List.of(table(1, 2));

        assertThrows(IllegalStateException.class, () ->
                service.createReservation(customer, LocalDate.now(), slot.getId(), 5, null));
        assertFalse(reservationDAO.atomicSaveUsed);
    }

    @Test
    void cancelReservationChecksOwnershipAndAppliesDomainTransition() throws SQLException {
        Reservation reservation = reservation(customer, ReservationStatus.CREATED);
        reservationDAO.reservation = reservation;

        service.cancelReservation(reservation.getId(), customer);

        assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
        assertEquals(ReservationStatus.CANCELED, reservationDAO.updatedStatus);
        assertEquals(1, notificationDAO.notifications.size());
    }

    @Test
    void cancelReservationRejectsAnotherCustomer() {
        Reservation reservation = reservation(customer, ReservationStatus.CREATED);
        reservationDAO.reservation = reservation;

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelReservation(reservation.getId(), user(99, Role.CUSTOMER)));
        assertNull(reservationDAO.updatedStatus);
    }

    private User user(int id, Role role) {
        User user = new User(
                "user" + id,
                new Email("user" + id + "@example.com"),
                "hash",
                "Nome",
                "Cognome",
                role
        );
        user.setId(id);
        return user;
    }

    private Table table(int id, int seats) {
        Table table = new Table(id, seats, true, "sala");
        table.setId(id);
        return table;
    }

    private Reservation reservation(User owner, ReservationStatus status) {
        Reservation reservation = new Reservation(
                owner,
                LocalDate.now().atTime(slot.getStartTime()),
                slot,
                2,
                null
        );
        reservation.setId(55);
        if (status == ReservationStatus.CONFIRMED) {
            reservation.confirm();
        }
        return reservation;
    }

    private static class FakeReservationDAO extends ReservationDAO {
        private Reservation reservation;
        private List<Table> savedTables;
        private ReservationStatus updatedStatus;
        private boolean atomicSaveUsed;

        @Override
        public List<Integer> getReservedTableIds(LocalDate date, int slotId) {
            return List.of();
        }

        @Override
        public List<MergeTable> addReservationWithTables(Reservation reservation, List<Table> tables) {
            atomicSaveUsed = true;
            this.reservation = reservation;
            this.savedTables = List.copyOf(tables);
            reservation.setId(91);

            List<MergeTable> assignments = new ArrayList<>();
            for (Table table : tables) {
                assignments.add(new MergeTable(
                        reservation,
                        table,
                        table.getSeats(),
                        "RES-" + reservation.getId()
                ));
            }
            return assignments;
        }

        @Override
        public Optional<Reservation> getReservationById(int reservationId) {
            return Optional.ofNullable(reservation);
        }

        @Override
        public void updateStatus(int reservationId, ReservationStatus status) {
            updatedStatus = status;
        }
    }

    private static class FakeTableDAO extends TableDAO {
        private List<Table> tables = List.of();

        @Override
        public List<Table> getAvailableTables() {
            return tables;
        }
    }

    private static class FakeSlotDAO extends SlotDAO {
        private Slot slot;

        @Override
        public Optional<Slot> getSlotById(int slotId) {
            return Optional.ofNullable(slot);
        }
    }

    private static class FakeNotificationDAO extends NotificationDAO {
        private final List<Notification> notifications = new ArrayList<>();

        @Override
        public void addNotification(Notification notification) {
            notifications.add(notification);
        }
    }
}
