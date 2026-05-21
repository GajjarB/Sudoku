package com.example.sudoku.domain.managers;

import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.domain.models.GameState;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.atomic.AtomicLong;

public class GameStateManagerTest {

    private GameStateManager manager;

    @Before
    public void setUp() {
        manager = new GameStateManager();
    }

    @Test
    public void processInput_lockedCell_returnsLocked() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();

        InputResult result = manager.processInput(0, 1, 2, false);

        assertEquals(InputResult.Type.LOCKED_CELL, result.type);
    }

    @Test
    public void processInput_noteMode_togglesNote() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();

        InputResult result = manager.processInput(0, 0, 1, true);

        assertEquals(InputResult.Type.NOTE_UPDATED, result.type);
        assertTrue(result.grid.getCell(0, 0).getNotes().contains(1));
    }

    @Test
    public void processInput_correctFinalMove_completesPuzzle() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();

        InputResult result = manager.processInput(0, 0, 1, false);

        assertEquals(InputResult.Type.PUZZLE_COMPLETED, result.type);
        assertEquals(GameState.WON, manager.getCurrentState());
        assertTrue(result.score > 0);
    }

    @Test
    public void processInput_wrongMove_incrementsMistake() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();

        InputResult result = manager.processInput(0, 0, 9, false);

        assertEquals(InputResult.Type.WRONG_MOVE, result.type);
        assertEquals(1, result.mistakes);
        assertTrue(result.grid.getCell(0, 0).isConflict());
    }

    @Test
    public void wrongMove_conflictPersistsAfterNoteUpdate() {
        manager.loadGame(gameWithTwoEditableCells());
        manager.resumeGame();

        manager.processInput(0, 0, 9, false);
        InputResult result = manager.processInput(0, 1, 3, true);

        assertEquals(InputResult.Type.NOTE_UPDATED, result.type);
        assertTrue(result.grid.getCell(0, 0).isConflict());
    }

    @Test
    public void processInput_thirdWrongMove_gameOver() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();

        manager.processInput(0, 0, 9, false);
        manager.processInput(0, 0, 8, false);
        InputResult result = manager.processInput(0, 0, 7, false);

        assertEquals(InputResult.Type.GAME_OVER, result.type);
        assertEquals(GameState.GAME_OVER, manager.getCurrentState());
    }

    @Test
    public void undo_afterCorrectMove_restoresEmptyCell() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();
        manager.processInput(0, 0, 1, true);

        InputResult result = manager.undo();

        assertEquals(InputResult.Type.UNDONE, result.type);
        assertFalse(result.grid.getCell(0, 0).getNotes().contains(1));
    }

    @Test
    public void pauseGame_elapsedTimeDoesNotDoubleCountPausedSegment() {
        AtomicLong now = new AtomicLong(1_000L);
        manager = new GameStateManager(null, now::get);
        manager.loadGame(gameWithOneEditableCell());

        manager.resumeGame();
        now.set(6_000L);
        manager.pauseGame();
        now.set(16_000L);

        assertEquals(5_000L, manager.getElapsedTimeMs());
    }

    @Test
    public void resumeAfterPause_continuesElapsedTime() {
        AtomicLong now = new AtomicLong(1_000L);
        manager = new GameStateManager(null, now::get);
        manager.loadGame(gameWithOneEditableCell());

        manager.resumeGame();
        now.set(4_000L);
        manager.pauseGame();
        now.set(10_000L);
        manager.resumeGame();
        now.set(13_000L);

        // 3s before pause + 3s after resume = 6s
        assertEquals(6_000L, manager.getElapsedTimeMs());
    }

    @Test
    public void completedGame_timerStops() {
        AtomicLong now = new AtomicLong(1_000L);
        manager = new GameStateManager(null, now::get);
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();

        now.set(5_000L);
        manager.processInput(0, 0, 1, false);
        long winTime = manager.getElapsedTimeMs();
        now.set(60_000L);

        assertEquals(GameState.WON, manager.getCurrentState());
        assertEquals(winTime, manager.getElapsedTimeMs());
    }

    @Test
    public void redo_afterUndo_restoresMove() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();
        manager.processInput(0, 0, 1, true);
        manager.undo();

        InputResult redo = manager.redo();

        assertEquals(InputResult.Type.REDONE, redo.type);
        assertTrue(redo.grid.getCell(0, 0).getNotes().contains(1));
    }

    @Test
    public void undoRedo_correctMoveRestoresAutoRemovedPeerNotes() {
        manager.loadGame(gameWithTwoEditableCells());
        manager.resumeGame();
        manager.processInput(0, 1, 1, true);

        InputResult move = manager.processInput(0, 0, 1, false);
        assertEquals(InputResult.Type.CORRECT_MOVE, move.type);
        assertFalse(move.grid.getCell(0, 1).getNotes().contains(1));

        InputResult undo = manager.undo();
        assertTrue(undo.grid.getCell(0, 1).getNotes().contains(1));

        InputResult redo = manager.redo();
        assertFalse(redo.grid.getCell(0, 1).getNotes().contains(1));
    }

    @Test
    public void savedUndoStackRestoresAutoRemovedPeerNotesAfterReload() {
        SavedGame game = gameWithTwoEditableCells();
        manager.loadGame(game);
        manager.resumeGame();
        manager.processInput(0, 1, 1, true);
        manager.processInput(0, 0, 1, false);
        manager.pauseGame();

        GameStateManager restored = new GameStateManager();
        restored.loadGame(game);
        restored.resumeGame();

        InputResult undo = restored.undo();
        assertTrue(undo.grid.getCell(0, 1).getNotes().contains(1));
    }

    @Test
    public void undo_afterWon_doesNotMutateGrid() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();
        manager.processInput(0, 0, 1, false);

        InputResult undo = manager.undo();

        assertEquals(InputResult.Type.IGNORED, undo.type);
        assertEquals(GameState.WON, manager.getCurrentState());
        assertEquals(1, undo.grid.getCell(0, 0).getValue());
    }

    @Test
    public void undo_afterGameOver_doesNotMutateGrid() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();
        manager.processInput(0, 0, 9, false);
        manager.processInput(0, 0, 8, false);
        manager.processInput(0, 0, 7, false);

        InputResult undo = manager.undo();

        assertEquals(InputResult.Type.IGNORED, undo.type);
        assertEquals(GameState.GAME_OVER, manager.getCurrentState());
        assertEquals(7, undo.grid.getCell(0, 0).getValue());
    }

    @Test
    public void appliedHint_canBeUndone() {
        manager.loadGame(gameWithTwoEditableCells());
        manager.resumeGame();

        assertTrue(manager.requestHint(4).applied);
        InputResult undo = manager.undo();

        assertEquals(InputResult.Type.UNDONE, undo.type);
        assertEquals(0, undo.grid.getCell(0, 0).getValue());
    }

    @Test
    public void wrongGuess_doesNotBlockAppliedHint() {
        manager.loadGame(gameWithTwoEditableCells());
        manager.resumeGame();
        manager.processInput(0, 0, 9, false);

        assertTrue(manager.requestHint(4).applied);
    }

    @Test
    public void saveAndLoad_preservesHintRequestsAndLevelPenalty() {
        AtomicLong now = new AtomicLong(1_000L);
        manager = new GameStateManager(null, now::get);
        SavedGame game = gameWithTwoEditableCells();
        manager.loadGame(game);
        manager.resumeGame();

        assertTrue(manager.requestHint(4).applied);
        manager.pauseGame();

        GameStateManager restored = new GameStateManager(null, now::get);
        restored.loadGame(game);
        restored.resumeGame();

        assertEquals(1, restored.getHintsUsed());
        InputResult complete = restored.processInput(0, 1, 2, false);

        assertEquals(InputResult.Type.PUZZLE_COMPLETED, complete.type);
        assertEquals(600, complete.score);
    }

    @Test
    public void redo_emptyStack_returnsIgnored() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();
        assertEquals(InputResult.Type.IGNORED, manager.redo().type);
    }

    @Test
    public void newMoveClearsRedoStack() {
        manager.loadGame(gameWithOneEditableCell());
        manager.resumeGame();
        manager.processInput(0, 0, 1, true);
        manager.undo();
        manager.processInput(0, 0, 5, true);

        assertFalse(manager.canRedo());
    }

    @Test
    public void saveAndLoad_roundTripsNotesAndUndo() {
        com.example.sudoku.data.local.entities.SavedGame game = gameWithOneEditableCell();
        manager.loadGame(game);
        manager.resumeGame();
        manager.processInput(0, 0, 3, true);
        manager.processInput(0, 0, 7, true);
        manager.pauseGame();

        GameStateManager restored = new GameStateManager();
        restored.loadGame(game);

        assertTrue(restored.canUndo());
        assertTrue(restored.getGridSnapshot().getCell(0, 0).getNotes().contains(3));
        assertTrue(restored.getGridSnapshot().getCell(0, 0).getNotes().contains(7));
    }

    @Test
    public void pauseGame_repeatedPauseIsIdempotent() {
        AtomicLong now = new AtomicLong(1_000L);
        manager = new GameStateManager(null, now::get);
        manager.loadGame(gameWithOneEditableCell());

        manager.resumeGame();
        now.set(6_000L);
        manager.pauseGame();
        now.set(11_000L);
        manager.pauseGame();

        assertEquals(5_000L, manager.getElapsedTimeMs());
    }

    private SavedGame gameWithOneEditableCell() {
        String solution = "123456789456789123789123456214365897365897214897214365531642978642978531978531642";
        String puzzle = "0" + solution.substring(1);
        String locked = "0" + "1".repeat(80);
        return new SavedGame("test-game", 1, puzzle, solution, locked);
    }

    private SavedGame gameWithTwoEditableCells() {
        String solution = "123456789456789123789123456214365897365897214897214365531642978642978531978531642";
        String puzzle = "00" + solution.substring(2);
        String locked = "00" + "1".repeat(79);
        return new SavedGame("test-game", 1, puzzle, solution, locked);
    }
}
