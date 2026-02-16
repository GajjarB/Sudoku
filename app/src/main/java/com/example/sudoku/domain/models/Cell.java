package com.example.sudoku.domain.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single cell in the Sudoku grid.
 */
public class Cell {
    private int row;
    private int col;
    private int value; // 0 for empty
    private boolean isLocked; // True if part of initial puzzle
    private boolean isCorrect; // True if value matches solution
    private List<Integer> notes; // Pencil marks

    public Cell(int row, int col, int value, boolean isLocked) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.isLocked = isLocked;
        this.isCorrect = true; // Default assumption until validated
        this.notes = new ArrayList<>();
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getValue() { return value; }
    public boolean isLocked() { return isLocked; }
    public boolean isCorrect() { return isCorrect; }
    public List<Integer> getNotes() { return notes; }

    public void setValue(int value) {
        this.value = value;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    
    public void addNote(int number) {
        if (!notes.contains(number)) notes.add(number);
    }
    
    public void removeNote(int number) {
        notes.remove(Integer.valueOf(number));
    }
    
    public void clearNotes() {
        notes.clear();
    }
}
