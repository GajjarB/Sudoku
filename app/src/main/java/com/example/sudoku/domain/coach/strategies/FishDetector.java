package com.example.sudoku.domain.coach.strategies;

import com.example.sudoku.domain.coach.CandidateElimination;
import com.example.sudoku.domain.coach.CandidateElimination.StrategyHit;
import com.example.sudoku.domain.coach.CandidateGrid;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.EliminationDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Fish family detector. For a fixed digit d and a chosen size K:
 *   - Pick K rows where d appears only in cells whose column union is K.
 *   - d can be eliminated from those columns in all other rows.
 * Symmetric column form. K=2 is X-Wing, K=3 Swordfish, K=4 Jellyfish.
 */
public class FishDetector implements EliminationDetector {

    private final int size;
    private final String strategyName;

    public FishDetector(int size, String strategyName) {
        this.size = size;
        this.strategyName = strategyName;
    }

    @Override
    public StrategyHit detect(CandidateGrid cg) {
        for (int d = 1; d <= Units.N; d++) {
            StrategyHit hit = scan(cg, d, true);
            if (hit.found) return hit;
            hit = scan(cg, d, false);
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }

    private StrategyHit scan(CandidateGrid cg, int digit, boolean rowBased) {
        int bit = 1 << digit;
        int[] lineMask = new int[Units.N];
        boolean[] eligible = new boolean[Units.N];
        for (int i = 0; i < Units.N; i++) {
            int mask = 0;
            for (int j = 0; j < Units.N; j++) {
                int r = rowBased ? i : j;
                int c = rowBased ? j : i;
                if (cg.isSolved(r, c)) continue;
                if ((cg.candidates(r, c) & bit) != 0) {
                    mask |= 1 << j;
                }
            }
            lineMask[i] = mask;
            int count = Integer.bitCount(mask);
            eligible[i] = count >= 2 && count <= size;
        }
        int[] combo = new int[size];
        return enumerate(cg, digit, rowBased, lineMask, eligible, combo, 0, 0);
    }

    private StrategyHit enumerate(CandidateGrid cg, int digit, boolean rowBased,
                                   int[] lineMask, boolean[] eligible,
                                   int[] combo, int start, int depth) {
        if (depth == size) {
            int union = 0;
            for (int i = 0; i < size; i++) union |= lineMask[combo[i]];
            if (Integer.bitCount(union) != size) return StrategyHit.none();

            int bit = 1 << digit;
            List<CandidateElimination> elims = new ArrayList<>();
            List<CellPosition> highlights = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int lineIdx = combo[i];
                int colsBits = lineMask[lineIdx];
                while (colsBits != 0) {
                    int lowBit = colsBits & -colsBits;
                    int j = Integer.numberOfTrailingZeros(lowBit);
                    int r = rowBased ? lineIdx : j;
                    int c = rowBased ? j : lineIdx;
                    highlights.add(new CellPosition(r, c));
                    colsBits &= ~lowBit;
                }
            }
            int cross = union;
            while (cross != 0) {
                int lowBit = cross & -cross;
                int j = Integer.numberOfTrailingZeros(lowBit);
                for (int i = 0; i < Units.N; i++) {
                    boolean isSelected = false;
                    for (int k = 0; k < size; k++) {
                        if (combo[k] == i) { isSelected = true; break; }
                    }
                    if (isSelected) continue;
                    int r = rowBased ? i : j;
                    int c = rowBased ? j : i;
                    if (cg.isSolved(r, c)) continue;
                    if ((cg.candidates(r, c) & bit) != 0) {
                        elims.add(new CandidateElimination(r, c, digit));
                    }
                }
                cross &= ~lowBit;
            }
            if (!elims.isEmpty()) {
                return StrategyHit.hit(strategyName, elims, highlights,
                        strategyName + " on digit " + digit + ".");
            }
            return StrategyHit.none();
        }
        for (int i = start; i < Units.N; i++) {
            if (!eligible[i]) continue;
            combo[depth] = i;
            StrategyHit hit = enumerate(cg, digit, rowBased, lineMask, eligible, combo, i + 1, depth + 1);
            if (hit.found) return hit;
        }
        return StrategyHit.none();
    }
}
