package com.example.sudoku.domain.managers;

import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.models.Grid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InputResult {
    public enum Type {
        LOCKED_CELL,
        NOTE_UPDATED,
        CORRECT_MOVE,
        WRONG_MOVE,
        ERASED,
        UNDONE,
        REDONE,
        PUZZLE_COMPLETED,
        GAME_OVER,
        IGNORED
    }

    public final Type type;
    public final Grid grid;
    public final boolean correct;
    public final boolean completed;
    public final int mistakes;
    public final int hintsUsed;
    public final int score;
    public final int stars;
    public final String message;
    public final List<CellPosition> highlightCells;

    private InputResult(Type type, Grid grid, boolean correct, boolean completed,
                        int mistakes, int hintsUsed, int score, int stars,
                        String message, List<CellPosition> highlightCells) {
        this.type = type;
        this.grid = grid;
        this.correct = correct;
        this.completed = completed;
        this.mistakes = mistakes;
        this.hintsUsed = hintsUsed;
        this.score = score;
        this.stars = stars;
        this.message = message;
        this.highlightCells = Collections.unmodifiableList(new ArrayList<>(highlightCells));
    }

    public static InputResult of(Type type, Grid grid, boolean correct, boolean completed,
                                 int mistakes, int hintsUsed, int score, int stars,
                                 String message, List<CellPosition> highlightCells) {
        return new InputResult(type, grid, correct, completed, mistakes, hintsUsed,
                score, stars, message, highlightCells);
    }
}
