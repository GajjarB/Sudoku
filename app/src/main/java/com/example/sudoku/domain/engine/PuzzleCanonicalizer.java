package com.example.sudoku.domain.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Reduce a puzzle to a canonical form so duplicates can be detected.
 *
 * Canonicalization considers Sudoku-preserving transformations:
 *   - Digit relabeling (1..9 permutation)
 *   - Row permutation within a band (3 rows per band)
 *   - Column permutation within a stack (3 cols per stack)
 *   - Band permutation (swap 3-row blocks)
 *   - Stack permutation (swap 3-col blocks)
 *   - Transposition
 *
 * Two puzzles that differ only by these operations are considered the
 * same puzzle and should not both be served to a player.
 *
 * For pragmatic performance this implementation:
 *   - Relabels digits so first non-empty cell in reading order is 1, next new digit 2, ...
 *   - Sorts rows inside each band, sorts cols inside each stack
 *   - Tries transposition and keeps lexicographic min
 *
 * This is not a full minimum-orbit canonical form (NP-hard in worst case)
 * but is enough to catch the overwhelming majority of duplicates produced
 * by random generation.
 */
public final class PuzzleCanonicalizer {

    private static final int N = SudokuConstants.GRID_SIZE;
    private static final int BAND = SudokuConstants.BOX_SIZE;

    private PuzzleCanonicalizer() {}

    public static String canonicalForm(String puzzle) {
        if (puzzle == null || puzzle.length() != N * N) {
            throw new IllegalArgumentException("puzzle must be 81 chars");
        }

        int[][] grid = stringToGrid(puzzle);
        int[][] best = minRepresentation(grid);
        int[][] transposed = transpose(grid);
        int[][] transBest = minRepresentation(transposed);

        return compare(best, transBest) <= 0 ? gridToString(best) : gridToString(transBest);
    }

    public static long canonicalHash(String puzzle) {
        String canonical = canonicalForm(puzzle);
        long hash = 1125899906842597L;
        for (int i = 0; i < canonical.length(); i++) {
            hash = 31 * hash + canonical.charAt(i);
        }
        return hash;
    }

    private static int[][] minRepresentation(int[][] grid) {
        int[][] sorted = sortBandsAndStacks(grid);
        return relabel(sorted);
    }

    private static int[][] sortBandsAndStacks(int[][] grid) {
        int[][] result = deepCopy(grid);

        // Sort rows inside each band
        for (int band = 0; band < N; band += BAND) {
            Integer[] order = {band, band + 1, band + 2};
            int finalBand = band;
            Arrays.sort(order, Comparator.comparing(r -> rowKey(result, r)));
            int[][] swap = new int[BAND][N];
            for (int i = 0; i < BAND; i++) swap[i] = result[order[i]].clone();
            for (int i = 0; i < BAND; i++) result[finalBand + i] = swap[i];
        }

        // Sort cols inside each stack
        for (int stack = 0; stack < N; stack += BAND) {
            Integer[] order = {stack, stack + 1, stack + 2};
            final int finalStack = stack;
            Arrays.sort(order, Comparator.comparing(c -> colKey(result, c)));
            int[][] swap = new int[N][BAND];
            for (int r = 0; r < N; r++) {
                for (int i = 0; i < BAND; i++) swap[r][i] = result[r][order[i]];
            }
            for (int r = 0; r < N; r++) {
                for (int i = 0; i < BAND; i++) result[r][finalStack + i] = swap[r][i];
            }
        }

        // Sort bands
        List<int[][]> bands = new ArrayList<>();
        for (int b = 0; b < N; b += BAND) {
            int[][] band = new int[BAND][N];
            for (int i = 0; i < BAND; i++) band[i] = result[b + i].clone();
            bands.add(band);
        }
        bands.sort(Comparator.comparing(PuzzleCanonicalizer::bandKey));
        int[][] sorted = new int[N][N];
        for (int b = 0; b < bands.size(); b++) {
            for (int i = 0; i < BAND; i++) sorted[b * BAND + i] = bands.get(b)[i];
        }

        // Sort stacks
        List<int[][]> stacks = new ArrayList<>();
        for (int s = 0; s < N; s += BAND) {
            int[][] stack = new int[N][BAND];
            for (int r = 0; r < N; r++) {
                for (int i = 0; i < BAND; i++) stack[r][i] = sorted[r][s + i];
            }
            stacks.add(stack);
        }
        stacks.sort(Comparator.comparing(PuzzleCanonicalizer::stackKey));
        int[][] out = new int[N][N];
        for (int s = 0; s < stacks.size(); s++) {
            for (int r = 0; r < N; r++) {
                for (int i = 0; i < BAND; i++) out[r][s * BAND + i] = stacks.get(s)[r][i];
            }
        }

        return out;
    }

    private static int[][] relabel(int[][] grid) {
        int[] map = new int[10];
        int next = 1;
        int[][] out = new int[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int v = grid[r][c];
                if (v == 0) {
                    out[r][c] = 0;
                } else {
                    if (map[v] == 0) map[v] = next++;
                    out[r][c] = map[v];
                }
            }
        }
        return out;
    }

    private static String rowKey(int[][] grid, int row) {
        StringBuilder sb = new StringBuilder(N);
        for (int c = 0; c < N; c++) sb.append(grid[row][c]);
        return sb.toString();
    }

    private static String colKey(int[][] grid, int col) {
        StringBuilder sb = new StringBuilder(N);
        for (int r = 0; r < N; r++) sb.append(grid[r][col]);
        return sb.toString();
    }

    private static String bandKey(int[][] band) {
        StringBuilder sb = new StringBuilder(BAND * N);
        for (int[] row : band) for (int v : row) sb.append(v);
        return sb.toString();
    }

    private static String stackKey(int[][] stack) {
        StringBuilder sb = new StringBuilder(N * BAND);
        for (int[] row : stack) for (int v : row) sb.append(v);
        return sb.toString();
    }

    private static int[][] transpose(int[][] grid) {
        int[][] t = new int[N][N];
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) t[c][r] = grid[r][c];
        return t;
    }

    private static int[][] deepCopy(int[][] grid) {
        int[][] copy = new int[N][N];
        for (int r = 0; r < N; r++) copy[r] = grid[r].clone();
        return copy;
    }

    private static int compare(int[][] a, int[][] b) {
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (a[r][c] != b[r][c]) return Integer.compare(a[r][c], b[r][c]);
            }
        }
        return 0;
    }

    private static int[][] stringToGrid(String s) {
        int[][] g = new int[N][N];
        for (int i = 0; i < N * N; i++) g[i / N][i % N] = s.charAt(i) - '0';
        return g;
    }

    private static String gridToString(int[][] g) {
        StringBuilder sb = new StringBuilder(N * N);
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) sb.append(g[r][c]);
        return sb.toString();
    }
}
