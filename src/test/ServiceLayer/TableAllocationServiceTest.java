package test.ServiceLayer;

import DomainModel.reservation.Table;
import ServiceLayer.TableAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableAllocationServiceTest {

    private TableAllocationService service;

    @BeforeEach
    void setUp() {
        service = new TableAllocationService();
    }

    @Test
    void effectiveSeatsSubtractsTwoSeatsForEachJoin() {
        assertEquals(0, service.effectiveSeats(List.of()));
        assertEquals(4, service.effectiveSeats(List.of(table(1, 4, true))));
        assertEquals(6, service.effectiveSeats(List.of(
                table(1, 4, true),
                table(2, 4, true)
        )));
        assertEquals(8, service.effectiveSeats(List.of(
                table(1, 4, true),
                table(2, 4, true),
                table(3, 4, true)
        )));
    }

    @Test
    void canHostUsesSingleAndJoinedTableRules() {
        Table fourSeats = table(1, 4, true);
        Table anotherFour = table(2, 4, true);

        assertTrue(service.canHost(List.of(fourSeats), 4));
        assertFalse(service.canHost(List.of(fourSeats), 5));
        assertTrue(service.canHost(List.of(fourSeats, anotherFour), 6));
        assertFalse(service.canHost(List.of(fourSeats, anotherFour), 7));
    }

    @Test
    void findBestCombinationPrefersFewestTablesThenLeastWaste() {
        Table two = table(1, 2, true);
        Table four = table(2, 4, true);
        Table six = table(3, 6, false);

        List<Table> result = service.findBestCombination(List.of(two, four, six), 5);

        assertEquals(List.of(six), result);
    }

    @Test
    void findBestCombinationIgnoresUnavailableTables() {
        Table unavailable = table(1, 8, true);
        unavailable.setAvailable(false);
        Table fourA = table(2, 4, true);
        Table fourB = table(3, 4, true);

        List<Table> result = service.findBestCombination(
                List.of(unavailable, fourA, fourB),
                6
        );

        assertEquals(List.of(fourA, fourB), result);
    }

    private Table table(int number, int seats, boolean joinable) {
        Table table = new Table(number, seats, joinable, "sala");
        table.setId(number);
        return table;
    }
}
