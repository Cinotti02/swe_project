package BusinessLogic;

import DomainModel.Table;
import java.util.ArrayList;
import java.util.List;

public class TableAllocationService {

    /**
     * Calcola i posti effettivi combinando più tavoli.
     * Regola: ogni giunzione fa perdere 2 posti.
     * Esempi:
     * - 2 tavoli da 4: 4 + 4 - 2 = 6
     * - 3 tavoli da 4: 4 + 4 + 4 - 2 - 2 = 8
     */
    public int effectiveSeats(List<Table> tables) {
        if (tables == null || tables.isEmpty()) return 0;

        int sum = tables.stream()
                .mapToInt(Table::getSeats)
                .sum();

        int joints = tables.size() - 1;
        return sum - 2 * joints;
    }

    /**
     * Ritorna true se la combinazione di tavoli può ospitare quel numero di ospiti.
     */
    public boolean canHost(List<Table> tables, int guests) {
        return effectiveSeats(tables) >= guests;
    }

    // Alg calcolo miglior combinazione di tavoli è complesso e dipende da vari fattori.
    public List<Table> findBestCombination(List<Table> available, int guests) {
        if (available == null || available.isEmpty() || guests <= 0) {
            return null; // controllo sul input > 0
        }

        // Filtra tavoli utilizzabili
        List<Table> joinableTables = available.stream()
                .filter(Table::isAvailable)
                .filter(Table::isJoinable)
                .toList();

        List<Table> best = null;
        int bestWaste = Integer.MAX_VALUE;

        int n = joinableTables.size();

        // Genera tutte le combinazioni (subset)
        for (int mask = 1; mask < (1 << n); mask++) {

            List<Table> combination = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    combination.add(joinableTables.get(i));
                }
            }

            int seats = effectiveSeats(combination);
            if (seats < guests) continue;

            int waste = seats - guests;

            if (best == null) {
                best = combination;
                bestWaste = waste;
            } else {
                if (combination.size() < best.size()) {
                    best = combination;
                    bestWaste = waste;
                } else if (combination.size() == best.size() && waste < bestWaste) {
                    best = combination;
                    bestWaste = waste;
                }
            }
        }

        return best; // può essere null se nessuna combinazione funziona
    }
}
