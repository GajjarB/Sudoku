package com.example.sudoku.domain.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for Cell model: value, notes, locked state.
 */
public class CellTest {

    @Test
    public void constructor_setsInitialValues() {
        Cell cell = new Cell(3, 5, 7, true);
        assertEquals(3, cell.getRow());
        assertEquals(5, cell.getCol());
        assertEquals(7, cell.getValue());
        assertTrue(cell.isLocked());
        assertTrue(cell.isCorrect()); // Default
        assertTrue(cell.getNotes().isEmpty());
    }

    @Test
    public void setValue_updatesValue() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.setValue(5);
        assertEquals(5, cell.getValue());
    }

    @Test
    public void setCorrect_updatesCorrectFlag() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.setCorrect(false);
        assertFalse(cell.isCorrect());
    }

    @Test
    public void addNote_addsToNotesList() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.addNote(3);
        cell.addNote(7);
        assertEquals(2, cell.getNotes().size());
        assertTrue(cell.getNotes().contains(3));
        assertTrue(cell.getNotes().contains(7));
    }

    @Test
    public void addNote_duplicateIgnored() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.addNote(3);
        cell.addNote(3);
        assertEquals(1, cell.getNotes().size());
    }

    @Test
    public void removeNote_removesFromList() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.addNote(3);
        cell.addNote(7);
        cell.removeNote(3);
        assertEquals(1, cell.getNotes().size());
        assertFalse(cell.getNotes().contains(3));
    }

    @Test
    public void removeNote_nonExistent_noOp() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.addNote(3);
        cell.removeNote(7); // Not present
        assertEquals(1, cell.getNotes().size());
    }

    @Test
    public void clearNotes_emptiesList() {
        Cell cell = new Cell(0, 0, 0, false);
        cell.addNote(1);
        cell.addNote(2);
        cell.addNote(3);
        cell.clearNotes();
        assertTrue(cell.getNotes().isEmpty());
    }
}
