package com.example.sudoku.domain.coach;

import com.example.sudoku.domain.coach.strategies.BoxLineReductionDetector;
import com.example.sudoku.domain.coach.strategies.FishDetector;
import com.example.sudoku.domain.coach.strategies.HiddenPairDetector;
import com.example.sudoku.domain.coach.strategies.HiddenSubsetDetector;
import com.example.sudoku.domain.coach.strategies.NakedPairDetector;
import com.example.sudoku.domain.coach.strategies.NakedSubsetDetector;
import com.example.sudoku.domain.coach.strategies.PointingPairDetector;
import com.example.sudoku.domain.coach.strategies.XWingDetector;
import com.example.sudoku.domain.coach.strategies.XYWingDetector;
import com.example.sudoku.domain.coach.strategies.XYZWingDetector;
import com.example.sudoku.domain.coach.strategies.UniqueRectangleDetector;
import com.example.sudoku.domain.coach.strategies.SimpleColoringDetector;
import com.example.sudoku.domain.coach.strategies.ForcingChainDetector;
import com.example.sudoku.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Technique-only solver. Each iteration:
 *   1. Place any naked single (cell with exactly one candidate).
 *   2. Place any hidden single (digit appearing in only one cell of a unit).
 *   3. If no placement was made, run elimination detectors in order of
 *      increasing difficulty. Apply the first hit and loop.
 *
 * Returns when the grid is solved or no progress can be made.
 */
public class LogicalSolver {

    private static final int N = CandidateGrid.N;
    private static final int B = CandidateGrid.B;
    private static final int MAX_ITERATIONS = 1000;

    private final List<EliminationDetector> eliminators = Arrays.asList(
            new NakedPairDetector(),
            new HiddenPairDetector(),
            new PointingPairDetector(),
            new BoxLineReductionDetector(),
            new NakedSubsetDetector(3, Constants.STRATEGY_NAKED_TRIPLE),
            new HiddenSubsetDetector(3, Constants.STRATEGY_HIDDEN_TRIPLE),
            new XWingDetector(),
            new NakedSubsetDetector(4, Constants.STRATEGY_NAKED_QUAD),
            new HiddenSubsetDetector(4, Constants.STRATEGY_HIDDEN_QUAD),
            new FishDetector(3, Constants.STRATEGY_SWORDFISH),
            new XYWingDetector(),
            new FishDetector(4, Constants.STRATEGY_JELLYFISH),
            new XYZWingDetector(),
            new UniqueRectangleDetector(),
            new SimpleColoringDetector(),
            new ForcingChainDetector());

    public Result solve(int[][] grid, int[][] solution) {
        CandidateGrid cg = CandidateGrid.from(grid);
        Map<String, Integer> usage = new HashMap<>();
        int steps = 0;

        while (!cg.isComplete() && steps < MAX_ITERATIONS) {
            if (placeNakedSingle(cg, usage, solution)) { steps++; continue; }
            if (placeHiddenSingle(cg, usage, solution)) { steps++; continue; }

            boolean pruned = false;
            for (EliminationDetector detector : eliminators) {
                CandidateElimination.StrategyHit hit = detector.detect(cg);
                if (!hit.found) continue;
                for (CandidateElimination e : hit.eliminations) {
                    cg.eliminate(e.row, e.col, e.value);
                }
                usage.merge(hit.strategyName, 1, Integer::sum);
                steps++;
                pruned = true;
                break;
            }
            if (!pruned) {
                return Result.incomplete(cg.gridCopy(), usage, steps);
            }
        }
        boolean done = cg.isComplete();
        return done
                ? Result.solved(cg.gridCopy(), usage, steps)
                : Result.incomplete(cg.gridCopy(), usage, steps);
    }

    private boolean placeNakedSingle(CandidateGrid cg, Map<String, Integer> usage, int[][] solution) {
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (cg.isSolved(r, c)) continue;
                int cand = cg.candidates(r, c);
                if (Integer.bitCount(cand) == 1) {
                    int value = Integer.numberOfTrailingZeros(cand);
                    if (solution != null && solution[r][c] != 0 && solution[r][c] != value) {
                        // Candidate disagrees with known solution — bail out
                        return false;
                    }
                    cg.place(r, c, value);
                    usage.merge(Constants.STRATEGY_NAKED_SINGLE, 1, Integer::sum);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeHiddenSingle(CandidateGrid cg, Map<String, Integer> usage, int[][] solution) {
        for (int d = 1; d <= N; d++) {
            int bit = 1 << d;
            // rows
            for (int r = 0; r < N; r++) {
                int cells = 0;
                int colHit = -1;
                for (int c = 0; c < N; c++) {
                    if (cg.isSolved(r, c)) continue;
                    if ((cg.candidates(r, c) & bit) != 0) {
                        cells++;
                        colHit = c;
                    }
                }
                if (cells == 1) {
                    if (solution == null || solution[r][colHit] == 0 || solution[r][colHit] == d) {
                        cg.place(r, colHit, d);
                        usage.merge(Constants.STRATEGY_HIDDEN_SINGLE, 1, Integer::sum);
                        return true;
                    }
                }
            }
            // cols
            for (int c = 0; c < N; c++) {
                int cells = 0;
                int rowHit = -1;
                for (int r = 0; r < N; r++) {
                    if (cg.isSolved(r, c)) continue;
                    if ((cg.candidates(r, c) & bit) != 0) {
                        cells++;
                        rowHit = r;
                    }
                }
                if (cells == 1) {
                    if (solution == null || solution[rowHit][c] == 0 || solution[rowHit][c] == d) {
                        cg.place(rowHit, c, d);
                        usage.merge(Constants.STRATEGY_HIDDEN_SINGLE, 1, Integer::sum);
                        return true;
                    }
                }
            }
            // boxes
            for (int box = 0; box < N; box++) {
                int br = (box / B) * B;
                int bc = (box % B) * B;
                int cells = 0;
                int rowHit = -1;
                int colHit = -1;
                for (int r = br; r < br + B; r++) {
                    for (int c = bc; c < bc + B; c++) {
                        if (cg.isSolved(r, c)) continue;
                        if ((cg.candidates(r, c) & bit) != 0) {
                            cells++;
                            rowHit = r;
                            colHit = c;
                        }
                    }
                }
                if (cells == 1) {
                    if (solution == null || solution[rowHit][colHit] == 0
                            || solution[rowHit][colHit] == d) {
                        cg.place(rowHit, colHit, d);
                        usage.merge(Constants.STRATEGY_HIDDEN_SINGLE, 1, Integer::sum);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static final class Result {
        public final boolean solved;
        public final int[][] grid;
        public final Map<String, Integer> strategyUsage;
        public final int totalSteps;

        private Result(boolean solved, int[][] grid, Map<String, Integer> usage, int steps) {
            this.solved = solved;
            this.grid = grid;
            this.strategyUsage = Map.copyOf(usage);
            this.totalSteps = steps;
        }

        static Result solved(int[][] grid, Map<String, Integer> usage, int steps) {
            return new Result(true, grid, usage, steps);
        }

        static Result incomplete(int[][] grid, Map<String, Integer> usage, int steps) {
            return new Result(false, grid, usage, steps);
        }
    }
}
