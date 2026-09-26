package com.example.sudoku.domain.managers;

import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.domain.coach.CellPosition;
import com.example.sudoku.domain.coach.HintEngine;
import com.example.sudoku.domain.coach.HintResult;
import com.example.sudoku.domain.coach.StrategyDetector;
import com.example.sudoku.domain.coach.StrategyResult;
import com.example.sudoku.domain.engine.GameValidator;
import com.example.sudoku.domain.models.Cell;
import com.example.sudoku.domain.models.GameState;
import com.example.sudoku.domain.models.Grid;
import com.example.sudoku.utils.Constants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.function.LongSupplier;

/**
 * Single gameplay authority. UI sends intents here; this class mutates board state.
 */
public class GameStateManager {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private final GameRepository gameRepository;
    private final ScoreCalculator scoreCalculator;
    private final HintEngine hintEngine;
    private final StrategyDetector strategyDetector;
    private final LongSupplier clock;
    private final Stack<MoveRecord> undoStack = new Stack<>();
    private final Stack<MoveRecord> redoStack = new Stack<>();
    private final Map<String, Integer> strategySuccessCounts = new HashMap<>();

    private SavedGame currentGame;
    private int[][] currentGrid;
    private int[][] solutionGrid;
    private boolean[][] lockedCells;
    private boolean[][] conflicts;
    private boolean[][] hintHighlights;
    private boolean[][][] notes;
    private GameState currentState = GameState.IDLE;
    private long startTime;
    private long accumulatedTime;
    private int mistakes;
    private int hintPenaltyUnits;
    private int hintRequests;
    private int lastScore;
    private int lastStars;

    public GameStateManager() {
        this(null);
    }

    public GameStateManager(GameRepository gameRepository) {
        this(gameRepository, System::currentTimeMillis);
    }

    GameStateManager(GameRepository gameRepository, LongSupplier clock) {
        this.gameRepository = gameRepository;
        this.clock = clock;
        this.scoreCalculator = new ScoreCalculator();
        this.hintEngine = new HintEngine();
        this.strategyDetector = new StrategyDetector();
    }

    public synchronized void loadGame(SavedGame game) {
        currentGame = game;
        accumulatedTime = game.elapsedTimeMs;
        mistakes = game.mistakesMade;
        PersistedNotesState persistedNotes = decodePersistedNotesState(game.notes, game.hintsRemaining);
        hintPenaltyUnits = persistedNotes.hintPenaltyUnits;
        hintRequests = persistedNotes.hintRequests;
        currentGrid = GameValidator.stringToGrid(game.currentGrid);
        solutionGrid = GameValidator.stringToGrid(game.solutionGrid);
        lockedCells = parseLockedCells(game.lockedCells);
        conflicts = new boolean[9][9];
        hintHighlights = new boolean[9][9];
        notes = persistedNotes.notes;
        refreshConflicts();
        undoStack.clear();
        redoStack.clear();
        decodeMoveStack(game.undoStack, undoStack);
        decodeMoveStack(game.redoStack, redoStack);
        strategySuccessCounts.clear();
        if (game.isCompleted) {
            currentState = GameValidator.isCompleteSolution(currentGrid, solutionGrid)
                    ? GameState.WON
                    : GameState.GAME_OVER;
        } else {
            currentState = GameState.PAUSED;
        }
    }

    public synchronized void resumeGame() {
        if (currentGame == null) return;
        if (currentState == GameState.PLAYING || currentState == GameState.WON
                || currentState == GameState.GAME_OVER) {
            return;
        }
        startTime = clock.getAsLong();
        currentState = GameState.PLAYING;
    }

    public synchronized void pauseGame() {
        if (currentState == GameState.PLAYING) {
            accumulatedTime += clock.getAsLong() - startTime;
        }
        currentState = GameState.PAUSED;
        saveCurrentGame();
    }

    public synchronized void saveCurrentGame() {
        if (currentGame == null || currentGrid == null) return;
        snapshotIntoSavedGame();
        if (gameRepository != null) {
            gameRepository.updateSavedGame(currentGame);
        }
    }

    public synchronized void saveCurrentGameSync() {
        if (currentGame == null || currentGrid == null) return;
        snapshotIntoSavedGame();
        if (gameRepository != null) {
            gameRepository.updateSavedGameSync(currentGame);
        }
    }

    private void snapshotIntoSavedGame() {
        currentGame.currentGrid = GameValidator.gridToString(currentGrid);
        currentGame.elapsedTimeMs = getElapsedTimeMs();
        currentGame.lastPlayedAt = clock.getAsLong();
        currentGame.mistakesMade = mistakes;
        currentGame.notes = encodePersistedNotesState(notes, hintRequests, hintPenaltyUnits);
        currentGame.undoStack = encodeMoveStack(undoStack);
        currentGame.redoStack = encodeMoveStack(redoStack);
    }

    public synchronized void setUiState(int selectedRow, int selectedCol, boolean notesMode) {
        if (currentGame == null) return;
        currentGame.selectedRow = selectedRow;
        currentGame.selectedCol = selectedCol;
        currentGame.notesMode = notesMode;
    }

    public synchronized InputResult processInput(int row, int col, int number, boolean noteMode) {
        if (!canAcceptInput(row, col) || number < 1 || number > 9) {
            return result(InputResult.Type.IGNORED, false, "Input ignored", Collections.emptyList());
        }

        if (lockedCells[row][col]) {
            return result(InputResult.Type.LOCKED_CELL, false, "This cell is locked.", Collections.emptyList());
        }

        clearHintHighlights();

        if (noteMode) {
            boolean[][][] beforeNotes = copyNotesState();
            int oldValue = currentGrid[row][col];
            notes[row][col][number] = !notes[row][col][number];
            pushMove(row, col, oldValue, oldValue, beforeNotes, copyNotesState());
            return result(InputResult.Type.NOTE_UPDATED, true, "Note updated.", Collections.emptyList());
        }

        int oldValue = currentGrid[row][col];
        boolean[][][] beforeNotes = copyNotesState();

        boolean correct = solutionGrid[row][col] == number;
        currentGrid[row][col] = number;
        clearNotes(row, col);

        StrategyResult strategy = strategyDetector.detectNextMove(hintGridWithCellCleared(row, col), solutionGrid);

        if (correct) {
            refreshConflicts();
            removeRelatedNotes(row, col, number);
            if (strategy.found) {
                strategySuccessCounts.put(strategy.strategyName,
                        strategySuccessCounts.getOrDefault(strategy.strategyName, 0) + 1);
            }
            if (GameValidator.isCompleteSolution(currentGrid, solutionGrid)) {
                pushMove(row, col, oldValue, number, beforeNotes, copyNotesState());
                return completeGame();
            }
            pushMove(row, col, oldValue, number, beforeNotes, copyNotesState());
            persistLightweight();
            return result(InputResult.Type.CORRECT_MOVE, true, "Correct move.", Collections.emptyList());
        }

        mistakes++;
        refreshConflicts();
        List<CellPosition> conflictCells = conflictPositions(row, col, number);
        pushMove(row, col, oldValue, number, beforeNotes, copyNotesState());

        if (mistakes >= Constants.MAX_MISTAKES_ALLOWED) {
            currentState = GameState.GAME_OVER;
            if (currentGame != null) {
                currentGame.isCompleted = true;
                currentGame.completedAt = clock.getAsLong();
            }
            saveCurrentGame();
            return result(InputResult.Type.GAME_OVER, false, "Too many mistakes.", conflictCells);
        }

        persistLightweight();
        return result(InputResult.Type.WRONG_MOVE, false, "That move conflicts with the solution.", conflictCells);
    }

    public synchronized InputResult erase(int row, int col) {
        if (!canAcceptInput(row, col)) {
            return result(InputResult.Type.IGNORED, false, "Input ignored", Collections.emptyList());
        }
        if (lockedCells[row][col]) {
            return result(InputResult.Type.LOCKED_CELL, false, "This cell is locked.", Collections.emptyList());
        }
        boolean[][][] beforeNotes = copyNotesState();
        int oldValue = currentGrid[row][col];
        currentGrid[row][col] = 0;
        clearNotes(row, col);
        pushMove(row, col, oldValue, 0, beforeNotes, copyNotesState());
        refreshConflicts();
        clearHintHighlights();
        persistLightweight();
        return result(InputResult.Type.ERASED, true, "Cell erased.", Collections.emptyList());
    }

    public synchronized InputResult undo() {
        if (currentState == GameState.WON || currentState == GameState.GAME_OVER
                || undoStack.isEmpty() || currentGrid == null) {
            return result(InputResult.Type.IGNORED, false, "Nothing to undo.", Collections.emptyList());
        }
        MoveRecord move = undoStack.pop();
        redoStack.push(move);
        currentGrid[move.row][move.col] = move.beforeValue;
        // If the undone move was a wrong placement, decrement mistakes
        if (move.beforeValue != move.afterValue && move.afterValue != 0 && solutionGrid != null
                && move.row >= 0 && move.row < 9 && move.col >= 0 && move.col < 9
                && move.afterValue != solutionGrid[move.row][move.col]) {
            mistakes = Math.max(0, mistakes - 1);
        }
        if (currentState == GameState.GAME_OVER && mistakes < Constants.MAX_MISTAKES_ALLOWED) {
            currentState = GameState.PLAYING;
            startTime = clock.getAsLong();
            if (currentGame != null) {
                currentGame.isCompleted = false;
                currentGame.completedAt = 0;
            }
        }
        restoreNotes(move.beforeNotesState, move.row, move.col, move.beforeNotes);
        refreshConflicts();
        clearHintHighlights();
        persistLightweight();
        return result(InputResult.Type.UNDONE, true, "Move undone.", Collections.emptyList());
    }

    public synchronized InputResult redo() {
        if (isTerminalState() || redoStack.isEmpty() || currentGrid == null) {
            return result(InputResult.Type.IGNORED, false, "Nothing to redo.", Collections.emptyList());
        }
        MoveRecord move = redoStack.pop();
        undoStack.push(move);
        currentGrid[move.row][move.col] = move.afterValue;

        // If the redone move was a wrong placement, increment mistakes
        if (move.beforeValue != move.afterValue && move.afterValue != 0 && solutionGrid != null
                && move.row >= 0 && move.row < 9 && move.col >= 0 && move.col < 9
                && move.afterValue != solutionGrid[move.row][move.col]) {
            mistakes++;
            if (mistakes >= Constants.MAX_MISTAKES_ALLOWED) {
                currentState = GameState.GAME_OVER;
                if (currentGame != null) {
                    currentGame.isCompleted = true;
                    currentGame.completedAt = clock.getAsLong();
                }
            }
        }

        restoreNotes(move.afterNotesState, move.row, move.col, move.afterNotes);
        refreshConflicts();
        clearHintHighlights();
        persistLightweight();
        return result(InputResult.Type.REDONE, true, "Move redone.", Collections.emptyList());
    }

    public synchronized HintResult requestHint(int hintLevel) {
        if (currentState != GameState.PLAYING || currentGrid == null || solutionGrid == null) {
            return HintResult.unavailable("No active puzzle.");
        }
        if (currentGame != null && currentGame.hintsRemaining <= 0) {
            return HintResult.unavailable("No hints remaining.");
        }

        HintResult hint = hintEngine.getHint(hintGrid(), solutionGrid, hintLevel);
        if (!hint.available) return hint;

        hintRequests++;
        hintPenaltyUnits += hint.hintLevel;
        markHintHighlights(hint.highlightCells);
        if (currentGame != null) {
            currentGame.hintsRemaining = Math.max(0, currentGame.hintsRemaining - 1);
        }

        if (hint.applied && canAcceptInput(hint.targetRow, hint.targetCol)
                && !lockedCells[hint.targetRow][hint.targetCol]) {
            int oldValue = currentGrid[hint.targetRow][hint.targetCol];
            boolean[][][] beforeNotes = copyNotesState();
            currentGrid[hint.targetRow][hint.targetCol] = hint.value;
            clearNotes(hint.targetRow, hint.targetCol);
            removeRelatedNotes(hint.targetRow, hint.targetCol, hint.value);
            pushMove(hint.targetRow, hint.targetCol, oldValue, hint.value,
                    beforeNotes, copyNotesState());
            refreshConflicts();
            if (GameValidator.isCompleteSolution(currentGrid, solutionGrid)) {
                completeGame();
            } else {
                persistLightweight();
            }
        }

        return hint;
    }

    public synchronized Grid getGridSnapshot() {
        if (currentGrid == null || lockedCells == null) return null;
        Grid grid = new Grid(currentGrid, lockedCells);
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Cell cell = grid.getCell(row, col);
                cell.setConflict(conflicts[row][col]);
                cell.setHintHighlighted(hintHighlights[row][col]);
                cell.setCorrect(!conflicts[row][col]);
                List<Integer> cellNotes = new ArrayList<>();
                for (int value = 1; value <= 9; value++) {
                    if (notes[row][col][value]) cellNotes.add(value);
                }
                cell.setNotes(cellNotes);
            }
        }
        return grid;
    }

    public synchronized GameState getCurrentState() {
        return currentState;
    }

    public synchronized int getMistakes() {
        return mistakes;
    }

    public synchronized int getHintsUsed() {
        return hintRequests;
    }

    public synchronized int getLastScore() {
        return lastScore;
    }

    public synchronized int getLastStars() {
        return lastStars;
    }

    public synchronized long getElapsedTimeMs() {
        if (currentState == GameState.PLAYING) {
            return accumulatedTime + (clock.getAsLong() - startTime);
        }
        return accumulatedTime;
    }

    public synchronized String getCurrentGameId() {
        return currentGame != null ? currentGame.gameId : "";
    }

    public synchronized boolean isDailyChallenge() {
        return currentGame != null && currentGame.isDailyChallenge;
    }

    public int getSavedSelectedRow() {
        return currentGame != null ? currentGame.selectedRow : -1;
    }

    public int getSavedSelectedCol() {
        return currentGame != null ? currentGame.selectedCol : -1;
    }

    public boolean getSavedNotesMode() {
        return currentGame != null && currentGame.notesMode;
    }

    public String getCoachTip() {
        if (strategySuccessCounts.isEmpty()) {
            return "Use notes to narrow candidates before asking for the final answer.";
        }
        String strongest = "";
        int best = 0;
        for (Map.Entry<String, Integer> entry : strategySuccessCounts.entrySet()) {
            if (entry.getValue() > best) {
                strongest = entry.getKey();
                best = entry.getValue();
            }
        }
        return "Strongest skill: " + strongest.replace('_', ' ') + ". Keep looking for that pattern before guessing.";
    }

    private InputResult completeGame() {
        accumulatedTime = getElapsedTimeMs();
        currentState = GameState.WON;
        lastScore = scoreCalculator.calculateScore(currentGame.levelNumber, accumulatedTime, mistakes, hintPenaltyUnits);
        lastStars = scoreCalculator.calculateStars(lastScore, currentGame.levelNumber);
        if (currentGame != null) {
            currentGame.isCompleted = true;
            currentGame.completedAt = clock.getAsLong();
        }
        saveCurrentGame();
        return result(InputResult.Type.PUZZLE_COMPLETED, true, "Puzzle completed.", Collections.emptyList());
    }

    private InputResult result(InputResult.Type type, boolean correct, String message, List<CellPosition> highlights) {
        return InputResult.of(type, getGridSnapshot(), correct,
                currentState == GameState.WON, mistakes, hintRequests, lastScore, lastStars,
                message, highlights);
    }

    private boolean canAcceptInput(int row, int col) {
        return currentState == GameState.PLAYING
                && currentGrid != null
                && row >= 0 && row < 9
                && col >= 0 && col < 9;
    }

    private int[][] hintGridWithCellCleared(int row, int col) {
        int[][] copy = hintGrid();
        copy[row][col] = 0;
        return copy;
    }

    private int[][] hintGrid() {
        int[][] copy = copyGrid(currentGrid);
        if (solutionGrid == null || lockedCells == null) return copy;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (!lockedCells[row][col] && copy[row][col] != 0
                        && copy[row][col] != solutionGrid[row][col]) {
                    copy[row][col] = 0;
                }
            }
        }
        return copy;
    }

    private void clearHintHighlights() {
        hintHighlights = new boolean[9][9];
    }

    private void refreshConflicts() {
        conflicts = new boolean[9][9];
        if (currentGrid == null || solutionGrid == null || lockedCells == null) return;

        int[] rowConflicts = new int[9];
        int[] colConflicts = new int[9];
        int[] boxConflicts = new int[9];

        // 1st pass: identify all wrong cells and record their values in the conflicts caches
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = currentGrid[row][col];
                if (!lockedCells[row][col] && value != 0 && value != solutionGrid[row][col]) {
                    conflicts[row][col] = true;
                    int mask = 1 << value;
                    rowConflicts[row] |= mask;
                    colConflicts[col] |= mask;
                    int boxIdx = (row / 3) * 3 + (col / 3);
                    boxConflicts[boxIdx] |= mask;
                }
            }
        }

        // 2nd pass: mark any cell that shares a value with a wrong cell in the same row/col/box
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = currentGrid[row][col];
                if (value != 0 && !conflicts[row][col]) {
                    int mask = 1 << value;
                    int boxIdx = (row / 3) * 3 + (col / 3);
                    if ((rowConflicts[row] & mask) != 0 ||
                        (colConflicts[col] & mask) != 0 ||
                        (boxConflicts[boxIdx] & mask) != 0) {
                        conflicts[row][col] = true;
                    }
                }
            }
        }
    }

    private void markHintHighlights(List<CellPosition> cells) {
        hintHighlights = new boolean[9][9];
        for (CellPosition pos : cells) {
            if (pos.row >= 0 && pos.row < 9 && pos.col >= 0 && pos.col < 9) {
                hintHighlights[pos.row][pos.col] = true;
            }
        }
    }

    private List<CellPosition> conflictPositions(int row, int col, int number) {
        List<CellPosition> positions = new ArrayList<>();
        for (int[] conflict : GameValidator.getConflictingCells(currentGrid, row, col, number)) {
            positions.add(new CellPosition(conflict[0], conflict[1]));
        }
        positions.add(new CellPosition(row, col));
        return positions;
    }

    private void removeRelatedNotes(int row, int col, int number) {
        for (int i = 0; i < 9; i++) {
            notes[row][i][number] = false;
            notes[i][col][number] = false;
        }
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                notes[r][c][number] = false;
            }
        }
    }

    private void clearNotes(int row, int col) {
        notes[row][col] = new boolean[10];
    }

    private void pushMove(int row, int col, int beforeValue, int afterValue,
                          boolean[][][] beforeNotesState, boolean[][][] afterNotesState) {
        undoStack.push(MoveRecord.of(row, col, beforeValue, afterValue,
                beforeNotesState[row][col], afterNotesState[row][col],
                beforeNotesState, afterNotesState));
        redoStack.clear();
    }

    private void restoreNotes(boolean[][][] state, int row, int col, boolean[] cellNotes) {
        if (state != null) {
            notes = copyNotesState(state);
        } else {
            notes[row][col] = cellNotes.clone();
        }
    }

    private boolean[][][] copyNotesState() {
        return copyNotesState(notes);
    }

    private static boolean[][][] copyNotesState(boolean[][][] source) {
        boolean[][][] copy = new boolean[9][9][10];
        if (source == null) return copy;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (source[row][col] != null) {
                    System.arraycopy(source[row][col], 0, copy[row][col], 0, 10);
                }
            }
        }
        return copy;
    }

    private void persistLightweight() {
        if (currentGame == null) return;
        snapshotIntoSavedGame();
    }

    private boolean isTerminalState() {
        return currentState == GameState.WON || currentState == GameState.GAME_OVER;
    }

    private static String encodePersistedNotesState(boolean[][][] notes, int hintRequests,
                                                    int hintPenaltyUnits) {
        return "v2:" + hintRequests + ":" + hintPenaltyUnits + ":" + encodeNotes(notes);
    }

    private static PersistedNotesState decodePersistedNotesState(String encodedNotes,
                                                                 int hintsRemaining) {
        int fallbackHintRequests = Math.max(0, Constants.MAX_HINTS_PER_GAME - hintsRemaining);
        int fallbackHintPenaltyUnits = fallbackHintRequests;
        if (encodedNotes != null && encodedNotes.startsWith("v2:")) {
            String[] parts = encodedNotes.split(":", 4);
            if (parts.length == 4) {
                try {
                    int requests = Math.max(0, Integer.parseInt(parts[1]));
                    int penalty = Math.max(0, Integer.parseInt(parts[2]));
                    return new PersistedNotesState(decodeNotes(parts[3]), requests, penalty);
                } catch (NumberFormatException e) {
                    // Fall through to legacy decoding below.
                }
            }
        }
        return new PersistedNotesState(decodeNotes(encodedNotes),
                fallbackHintRequests, fallbackHintPenaltyUnits);
    }

    /**
     * Encode notes as 81 three-hex-digit masks, one 9-bit mask per cell.
     * Performance optimization: Avoids StringBuilder and String.format in loop.
     * Uses direct bitwise shifts and static HEX_ARRAY lookups to prevent GC overhead.
     */
    private static String encodeNotes(boolean[][][] notes) {
        if (notes == null) return "";
        char[] chars = new char[81 * 3];
        int idx = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int mask = 0;
                for (int v = 1; v <= 9; v++) {
                    if (notes[r][c][v]) mask |= (1 << v);
                }
                chars[idx++] = HEX_ARRAY[(mask >>> 8) & 0x0F];
                chars[idx++] = HEX_ARRAY[(mask >>> 4) & 0x0F];
                chars[idx++] = HEX_ARRAY[mask & 0x0F];
            }
        }
        return new String(chars);
    }

    private static boolean[][][] decodeNotes(String encoded) {
        boolean[][][] notes = new boolean[9][9][10];
        if (encoded == null || encoded.length() != 81 * 3) return notes;
        int idx = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                try {
                    int mask = Integer.parseInt(encoded.substring(idx, idx + 3), 16);
                    for (int v = 1; v <= 9; v++) {
                        if ((mask & (1 << v)) != 0) notes[r][c][v] = true;
                    }
                } catch (NumberFormatException e) {
                    return new boolean[9][9][10];
                }
                idx += 3;
            }
        }
        return notes;
    }

    private static String encodeMoveStack(Stack<MoveRecord> stack) {
        if (stack == null || stack.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(stack.size() * 20);
        for (MoveRecord m : stack) {
            int mask = 0;
            int beforeMask = 0;
            int afterMask = 0;
            for (int v = 1; v <= 9; v++) {
                if (m.beforeNotes[v]) beforeMask |= (1 << v);
                if (m.afterNotes[v]) afterMask |= (1 << v);
            }
            sb.append(m.row).append(',').append(m.col).append(',')
                    .append(m.beforeValue).append(',').append(m.afterValue).append(',')
                    .append(beforeMask).append(',').append(afterMask);
            if (m.beforeNotesState != null && m.afterNotesState != null) {
                sb.append(',').append(encodeNotes(m.beforeNotesState))
                        .append(',').append(encodeNotes(m.afterNotesState));
            }
            sb.append(';');
        }
        return sb.toString();
    }

    private static void decodeMoveStack(String encoded, Stack<MoveRecord> target) {
        target.clear();
        if (encoded == null || encoded.isEmpty()) return;
        String[] entries = encoded.split(";");
        for (String entry : entries) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(",");
            if (parts.length != 4 && parts.length != 6 && parts.length != 8) continue;
            try {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                int beforeValue = Integer.parseInt(parts[2]);
                int afterValue = parts.length == 6 ? Integer.parseInt(parts[3]) : beforeValue;
                if (parts.length == 8) {
                    afterValue = Integer.parseInt(parts[3]);
                }
                int beforeMask = Integer.parseInt(parts.length >= 6 ? parts[4] : parts[3]);
                int afterMask = parts.length >= 6 ? Integer.parseInt(parts[5]) : beforeMask;
                if (row < 0 || row >= 9 || col < 0 || col >= 9
                        || beforeValue < 0 || beforeValue > 9
                        || afterValue < 0 || afterValue > 9) {
                    continue;
                }
                boolean[] beforeNotes = new boolean[10];
                boolean[] afterNotes = new boolean[10];
                for (int v = 1; v <= 9; v++) {
                    if ((beforeMask & (1 << v)) != 0) beforeNotes[v] = true;
                    if ((afterMask & (1 << v)) != 0) afterNotes[v] = true;
                }
                boolean[][][] beforeState = parts.length == 8 ? decodeNotes(parts[6]) : null;
                boolean[][][] afterState = parts.length == 8 ? decodeNotes(parts[7]) : null;
                target.push(new MoveRecord(row, col, beforeValue, afterValue,
                        beforeNotes, afterNotes, beforeState, afterState));
            } catch (NumberFormatException e) {
                // skip malformed entry, continue restoring rest
            }
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }

    public boolean canRedo() { return !redoStack.isEmpty(); }

    private boolean[][] parseLockedCells(String lockedStr) {
        boolean[][] locked = new boolean[9][9];
        if (lockedStr == null || lockedStr.length() != 81) return locked;
        for (int i = 0; i < 81; i++) {
            locked[i / 9][i % 9] = lockedStr.charAt(i) == '1';
        }
        return locked;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[9][9];
        for (int row = 0; row < 9; row++) {
            System.arraycopy(grid[row], 0, copy[row], 0, 9);
        }
        return copy;
    }

    private static class PersistedNotesState {
        final boolean[][][] notes;
        final int hintRequests;
        final int hintPenaltyUnits;

        PersistedNotesState(boolean[][][] notes, int hintRequests, int hintPenaltyUnits) {
            this.notes = notes;
            this.hintRequests = hintRequests;
            this.hintPenaltyUnits = hintPenaltyUnits;
        }
    }

    private static class MoveRecord {
        final int row;
        final int col;
        final int beforeValue;
        final int afterValue;
        final boolean[] beforeNotes;
        final boolean[] afterNotes;
        final boolean[][][] beforeNotesState;
        final boolean[][][] afterNotesState;

        private MoveRecord(int row, int col, int beforeValue, int afterValue,
                           boolean[] beforeNotes, boolean[] afterNotes,
                           boolean[][][] beforeNotesState, boolean[][][] afterNotesState) {
            this.row = row;
            this.col = col;
            this.beforeValue = beforeValue;
            this.afterValue = afterValue;
            this.beforeNotes = beforeNotes;
            this.afterNotes = afterNotes;
            this.beforeNotesState = beforeNotesState;
            this.afterNotesState = afterNotesState;
        }

        static MoveRecord of(int row, int col, int beforeValue, int afterValue,
                             boolean[] beforeNotes, boolean[] afterNotes,
                             boolean[][][] beforeNotesState, boolean[][][] afterNotesState) {
            return new MoveRecord(row, col, beforeValue, afterValue,
                    beforeNotes, afterNotes, beforeNotesState, afterNotesState);
        }
    }
}
