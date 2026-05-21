package com.example.sudoku.domain.coach;

import java.util.Objects;

public class CellPosition {
    public final int row;
    public final int col;

    public CellPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CellPosition)) return false;
        CellPosition other = (CellPosition) obj;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
