package com.example.sudoku.domain.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Hole-carving symmetry. Drives which cells are removed together so the
 * resulting puzzle looks intentional.
 */
public enum SymmetryStrategy {
    NONE {
        @Override
        public List<int[]> partners(int row, int col) {
            return List.of(new int[]{row, col});
        }
    },
    ROTATIONAL_180 {
        @Override
        public List<int[]> partners(int row, int col) {
            return dedup(new int[]{row, col}, new int[]{N - 1 - row, N - 1 - col});
        }
    },
    ROTATIONAL_90 {
        @Override
        public List<int[]> partners(int row, int col) {
            return dedup(
                    new int[]{row, col},
                    new int[]{col, N - 1 - row},
                    new int[]{N - 1 - row, N - 1 - col},
                    new int[]{N - 1 - col, row});
        }
    },
    DIAGONAL {
        @Override
        public List<int[]> partners(int row, int col) {
            return dedup(new int[]{row, col}, new int[]{col, row});
        }
    },
    HORIZONTAL {
        @Override
        public List<int[]> partners(int row, int col) {
            return dedup(new int[]{row, col}, new int[]{N - 1 - row, col});
        }
    },
    VERTICAL {
        @Override
        public List<int[]> partners(int row, int col) {
            return dedup(new int[]{row, col}, new int[]{row, N - 1 - col});
        }
    };

    private static final int N = SudokuConstants.GRID_SIZE;

    public abstract List<int[]> partners(int row, int col);

    static List<int[]> dedup(int[]... cells) {
        List<int[]> out = new ArrayList<>(cells.length);
        for (int[] c : cells) {
            boolean dup = false;
            for (int[] existing : out) {
                if (existing[0] == c[0] && existing[1] == c[1]) {
                    dup = true;
                    break;
                }
            }
            if (!dup) out.add(c);
        }
        return out;
    }
}
