package com.example.sudoku.domain.engine;

import static com.example.sudoku.domain.engine.SudokuConstants.ALL_CANDIDATES;
import static com.example.sudoku.domain.engine.SudokuConstants.BOX_SIZE;
import static com.example.sudoku.domain.engine.SudokuConstants.GRID_SIZE;
import static com.example.sudoku.domain.engine.SudokuConstants.MAX_CLUES;
import static com.example.sudoku.domain.engine.SudokuConstants.MIN_UNIQUE_CLUES;
import static com.example.sudoku.domain.engine.SudokuConstants.TOTAL_CELLS;
import static com.example.sudoku.domain.engine.SudokuConstants.bitFor;
import static com.example.sudoku.domain.engine.SudokuConstants.boxIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.LongPredicate;

/**
 * Generates 9x9 Sudoku puzzles with a unique solution.
 *
 * Pipeline:
 *   1. Build a complete random solution via MRV backtracking with bitmask state.
 *   2. Carve holes in symmetric pairs using the chosen {@link SymmetryStrategy},
 *      keeping uniqueness invariant after every pair removal.
 *   3. Retry up to MAX_GENERATION_ATTEMPTS, return best clue-count match.
 *
 * Performance target: <100ms easy, <500ms expert on mid-range Android device.
 */
public class SudokuGenerator {

    private static final int MAX_GENERATION_ATTEMPTS = 60;
    private static final long DEFAULT_TIMEOUT_MS = 10_000L;

    private final Random random;
    private final SudokuSolver solver;

    public SudokuGenerator() {
        this(new Random());
    }

    /** Seeded generator — used for reproducible daily challenges. */
    public SudokuGenerator(long seed) {
        this(new Random(seed));
    }

    SudokuGenerator(Random random) {
        this.random = random;
        this.solver = new SudokuSolver();
    }

    /**
     * Convenience for callers that only know clue count. Throws on failure.
     */
    public Puzzle generate(int clueCount) {
        GenerationResult result = generate(clueCount, SymmetryStrategy.ROTATIONAL_180,
                DEFAULT_TIMEOUT_MS, CancellationToken.NONE, hash -> false);
        if (!result.isSuccess()) {
            throw new GenerationFailedException(result.message() != null
                    ? result.message()
                    : "Generation failed: " + result.status());
        }
        return result.puzzle();
    }

    public GenerationResult generate(Difficulty difficulty) {
        return generate(difficulty.targetClues(), SymmetryStrategy.ROTATIONAL_180,
                DEFAULT_TIMEOUT_MS, CancellationToken.NONE, hash -> false);
    }

    /**
     * Full generation contract.
     *
     * @param clueCount      desired clues; must be 17..81
     * @param symmetry       hole-carving symmetry
     * @param timeoutMs      soft wall-clock budget
     * @param cancellation   cooperative cancel
     * @param dedupCheck     called with the canonical hash of each candidate;
     *                       return true to reject (already seen)
     */
    public GenerationResult generate(int clueCount,
                                     SymmetryStrategy symmetry,
                                     long timeoutMs,
                                     CancellationToken cancellation,
                                     LongPredicate dedupCheck) {
        validateClueCount(clueCount);
        long deadline = System.currentTimeMillis() + timeoutMs;

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            if (cancellation.isCancelled()) {
                return GenerationResult.cancelled();
            }
            if (System.currentTimeMillis() > deadline) {
                return GenerationResult.timeout("Generation exceeded " + timeoutMs + "ms");
            }

            int[][] solution = generateCompleteSolution();
            int[][] puzzle = carve(solution, clueCount, symmetry, cancellation);

            if (puzzle == null
                    || !isValidCompleteSolution(solution)
                    || !isPuzzleConsistentWithSolution(puzzle, solution)
                    || !solver.hasUniqueSolution(puzzle)) {
                continue;
            }

            int actualClues = countClues(puzzle);
            if (actualClues != clueCount) {
                continue;
            }

            String puzzleStr = gridToString(puzzle);
            long canonical = PuzzleCanonicalizer.canonicalHash(puzzleStr);
            if (dedupCheck.test(canonical)) {
                continue;
            }

            Puzzle candidate = new Puzzle(puzzleStr, gridToString(solution), actualClues, canonical);
            return GenerationResult.success(candidate);
        }

        return GenerationResult.failed(
                "Unable to generate a unique Sudoku puzzle after "
                        + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    // ─── solution generation ────────────────────────────────────────────

    private int[][] generateCompleteSolution() {
        int[][] grid = new int[GRID_SIZE][GRID_SIZE];
        int[] rowMasks = new int[GRID_SIZE];
        int[] colMasks = new int[GRID_SIZE];
        int[] boxMasks = new int[GRID_SIZE];
        fillGrid(grid, rowMasks, colMasks, boxMasks);
        return grid;
    }

    private boolean fillGrid(int[][] grid, int[] rowMasks, int[] colMasks, int[] boxMasks) {
        int[] cell = nextEmptyCellMrv(grid, rowMasks, colMasks, boxMasks);
        if (cell == null) return true;

        int row = cell[0];
        int col = cell[1];
        int candidates = cell[2];

        List<Integer> order = shuffledCandidates(candidates);
        for (int num : order) {
            int bit = bitFor(num);
            grid[row][col] = num;
            rowMasks[row] |= bit;
            colMasks[col] |= bit;
            boxMasks[boxIndex(row, col)] |= bit;

            if (fillGrid(grid, rowMasks, colMasks, boxMasks)) return true;

            grid[row][col] = 0;
            rowMasks[row] &= ~bit;
            colMasks[col] &= ~bit;
            boxMasks[boxIndex(row, col)] &= ~bit;
        }
        return false;
    }

    private int[] nextEmptyCellMrv(int[][] grid, int[] rowMasks, int[] colMasks, int[] boxMasks) {
        int bestRow = -1;
        int bestCol = -1;
        int bestCandidates = 0;
        int bestCount = Integer.MAX_VALUE;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] != 0) continue;
                int used = rowMasks[row] | colMasks[col] | boxMasks[boxIndex(row, col)];
                int candidates = ALL_CANDIDATES & ~used;
                int count = Integer.bitCount(candidates);
                if (count < bestCount) {
                    bestRow = row;
                    bestCol = col;
                    bestCandidates = candidates;
                    bestCount = count;
                    if (count <= 1) return new int[]{bestRow, bestCol, bestCandidates};
                }
            }
        }
        return bestRow == -1 ? null : new int[]{bestRow, bestCol, bestCandidates};
    }

    private List<Integer> shuffledCandidates(int candidates) {
        List<Integer> out = new ArrayList<>(Integer.bitCount(candidates));
        int bits = candidates;
        while (bits != 0) {
            int low = bits & -bits;
            out.add(Integer.numberOfTrailingZeros(low));
            bits &= ~low;
        }
        Collections.shuffle(out, random);
        return out;
    }

    // ─── hole carving ───────────────────────────────────────────────────

    private int[][] carve(int[][] solution,
                          int clueCount,
                          SymmetryStrategy symmetry,
                          CancellationToken cancellation) {
        int[][] puzzle = copyGrid(solution);
        int cellsToRemove = TOTAL_CELLS - clueCount;

        List<Integer> positions = new ArrayList<>(TOTAL_CELLS);
        for (int i = 0; i < TOTAL_CELLS; i++) positions.add(i);
        Collections.shuffle(positions, random);

        int removed = 0;
        for (int pos : positions) {
            if (cancellation.isCancelled()) return null;
            if (removed >= cellsToRemove) break;

            int row = pos / GRID_SIZE;
            int col = pos % GRID_SIZE;
            List<int[]> partners = symmetry.partners(row, col);

            boolean anyAlreadyEmpty = false;
            for (int[] p : partners) {
                if (puzzle[p[0]][p[1]] == 0) {
                    anyAlreadyEmpty = true;
                    break;
                }
            }
            if (anyAlreadyEmpty) continue;
            if (removed + partners.size() > cellsToRemove) {
                // If the whole partner set is too large, try subset of partners if possible
                // (e.g. just the cell itself if it's not already removed)
                if (partners.size() > 1) {
                    partners = Collections.singletonList(new int[]{row, col});
                    if (puzzle[row][col] == 0) continue;
                    if (removed + 1 > cellsToRemove) continue;
                } else {
                    continue;
                }
            }

            int[] backup = new int[partners.size()];
            for (int i = 0; i < partners.size(); i++) {
                int[] p = partners.get(i);
                backup[i] = puzzle[p[0]][p[1]];
                puzzle[p[0]][p[1]] = 0;
            }

            if (solver.hasUniqueSolution(puzzle)) {
                removed += partners.size();
            } else {
                for (int i = 0; i < partners.size(); i++) {
                    int[] p = partners.get(i);
                    puzzle[p[0]][p[1]] = backup[i];
                }
            }
        }
        return puzzle;
    }

    // ─── helpers ────────────────────────────────────────────────────────

    private static int countClues(int[][] grid) {
        int clues = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] != 0) clues++;
            }
        }
        return clues;
    }

    private static String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder(TOTAL_CELLS);
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) sb.append(grid[r][c]);
        }
        return sb.toString();
    }

    private static int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int r = 0; r < GRID_SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, GRID_SIZE);
        }
        return copy;
    }

    private static boolean isValidCompleteSolution(int[][] grid) {
        for (int row = 0; row < GRID_SIZE; row++) {
            int rowMask = 0;
            int colMask = 0;
            for (int col = 0; col < GRID_SIZE; col++) {
                int rowValue = grid[row][col];
                int colValue = grid[col][row];
                if (rowValue < 1 || rowValue > 9 || colValue < 1 || colValue > 9) {
                    return false;
                }
                rowMask |= bitFor(rowValue);
                colMask |= bitFor(colValue);
            }
            if (rowMask != ALL_CANDIDATES || colMask != ALL_CANDIDATES) {
                return false;
            }
        }

        for (int boxStartRow = 0; boxStartRow < GRID_SIZE; boxStartRow += BOX_SIZE) {
            for (int boxStartCol = 0; boxStartCol < GRID_SIZE; boxStartCol += BOX_SIZE) {
                int boxMask = 0;
                for (int row = boxStartRow; row < boxStartRow + BOX_SIZE; row++) {
                    for (int col = boxStartCol; col < boxStartCol + BOX_SIZE; col++) {
                        boxMask |= bitFor(grid[row][col]);
                    }
                }
                if (boxMask != ALL_CANDIDATES) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isPuzzleConsistentWithSolution(int[][] puzzle, int[][] solution) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = puzzle[row][col];
                if (value != 0 && value != solution[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void validateClueCount(int clueCount) {
        if (clueCount < MIN_UNIQUE_CLUES || clueCount > MAX_CLUES) {
            throw new IllegalArgumentException("clueCount must be between "
                    + MIN_UNIQUE_CLUES + " and " + MAX_CLUES + ", got " + clueCount);
        }
        // BOX_SIZE used to silence the unused-import warning while keeping
        // a single source of truth for engine constants.
        if (BOX_SIZE * BOX_SIZE != GRID_SIZE) {
            throw new IllegalStateException("Box size invariant violated");
        }
    }

    /**
     * Generated puzzle payload.
     */
    public static class Puzzle {
        public final String puzzle;
        public final String solution;
        public final int clueCount;
        public final long canonicalHash;

        public Puzzle(String puzzle, String solution, int clueCount) {
            this(puzzle, solution, clueCount, PuzzleCanonicalizer.canonicalHash(puzzle));
        }

        public Puzzle(String puzzle, String solution, int clueCount, long canonicalHash) {
            this.puzzle = puzzle;
            this.solution = solution;
            this.clueCount = clueCount;
            this.canonicalHash = canonicalHash;
        }
    }
}
