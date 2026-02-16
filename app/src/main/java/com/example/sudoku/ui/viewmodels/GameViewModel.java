package com.example.sudoku.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.domain.models.Cell;
import com.example.sudoku.domain.models.GameState;
import com.example.sudoku.domain.models.Grid;

import java.util.Stack;

/**
 * ViewModel for the Game Activity.
 * Handles game state, timer, interaction, and updates the UI.
 */
public class GameViewModel extends AndroidViewModel {

    private final GameRepository gameRepository;
    private final LevelRepository levelRepository;
    
    // UI State
    private final MutableLiveData<Grid> gridLiveData = new MutableLiveData<>();
    private final MutableLiveData<GameState> gameStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> timeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> mistakesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notesModeLiveData = new MutableLiveData<>(false);
    
    // In-memory game state helpers
    private int currentRow = -1;
    private int currentCol = -1;
    private final Stack<Move> history = new Stack<>();

    private int[][] solutionGrid;
    private int currentLevel = 1;
    
    public GameViewModel(@NonNull Application application) {
        super(application);
        SudokuDatabase db = SudokuDatabase.getInstance(application);
        this.gameRepository = new GameRepository(db.gameDao());
        this.levelRepository = new LevelRepository(db.levelProgressDao());
        
        // Initialize state
        gameStateLiveData.setValue(GameState.IDLE);
    }
    
    private static class Move {
        int row, col, oldValue, newValue;
        boolean wasNote;
        
        Move(int r, int c, int oldV, int newV, boolean note) {
            row = r; col = c; oldValue = oldV; newValue = newV; wasNote = note;
        }
    }
    
    public void startNewGame(int level) {
        this.currentLevel = level;
        this.history.clear();
        this.notesModeLiveData.setValue(false);
        loadingLiveData.setValue(true);
        // Async generation
        new Thread(() -> {
            com.example.sudoku.domain.engine.SudokuGenerator generator = new com.example.sudoku.domain.engine.SudokuGenerator(System.currentTimeMillis());
            int clues = 30; 
            if (level > 1) clues = 25; 
            
            com.example.sudoku.domain.engine.SudokuGenerator.Puzzle puzzle = generator.generate(clues);
            
            // Store solution for validation
            this.solutionGrid = com.example.sudoku.utils.GridUtils.parseGrid(puzzle.solution);
            
            // Convert Puzzle grid to int[][]
            int[][] currentGrid = com.example.sudoku.utils.GridUtils.parseGrid(puzzle.puzzle);
            boolean[][] lockedState = new boolean[9][9];
            
            for(int r=0; r<9; r++) {
                for(int c=0; c<9; c++) {
                    if(currentGrid[r][c] != 0) {
                        lockedState[r][c] = true;
                    }
                }
            }
            
            Grid newGrid = new Grid(currentGrid, lockedState);
            gridLiveData.postValue(newGrid);
            gameStateLiveData.postValue(GameState.PLAYING);
            loadingLiveData.postValue(false);
            
            // Reset mistakes
            mistakesLiveData.postValue(0);
        }).start();
    }
    
    public void loadSavedGame(String gameId) {
        // Load from repo (simplified)
        // In real app, we parse the SavedGame strings back to Grid
    }
    
    public void onCellClicked(int row, int col) {
        currentRow = row;
        currentCol = col;
    }
    
    public void onNumberInput(int number) {
        if (currentRow == -1 || currentCol == -1) return;
        if (gameStateLiveData.getValue() != GameState.PLAYING) return;
        
        Grid currentGrid = gridLiveData.getValue();
        if (currentGrid == null) return;
        
        Cell cell = currentGrid.getCell(currentRow, currentCol);
        if (cell != null && !cell.isLocked()) {
            boolean isNote = Boolean.TRUE.equals(notesModeLiveData.getValue());
            
            if (isNote) {
                // Toggle Note
                if (cell.getNotes().contains(number)) {
                    cell.removeNote(number);
                } else {
                    cell.addNote(number);
                }
                // We don't track notes in undo history for simplicity, or we could.
                gridLiveData.setValue(currentGrid);
            } else {
                // Input Value
                int oldValue = cell.getValue();
                if (oldValue == number) return; // No change
                
                // Validate input
                if (solutionGrid != null && solutionGrid[currentRow][currentCol] == number) {
                    // Correct
                    cell.setValue(number);
                    cell.setCorrect(true);
                    cell.clearNotes(); // Clear notes in this cell
                    
                    history.push(new Move(currentRow, currentCol, oldValue, number, false));
                    gridLiveData.setValue(currentGrid);
                    
                    checkGameCompletion(currentGrid);
                } else {
                    // Wrong input
                    int mistakes = mistakesLiveData.getValue() != null ? mistakesLiveData.getValue() : 0;
                    mistakes++;
                    mistakesLiveData.setValue(mistakes);
                    
                    if (mistakes >= 3) {
                        gameStateLiveData.setValue(GameState.GAME_OVER);
                    }
                }
            }
        }
    }
    
    public void undo() {
        if (history.isEmpty()) return;
        Move move = history.pop();
        
        Grid grid = gridLiveData.getValue();
        if (grid != null) {
            Cell cell = grid.getCell(move.row, move.col);
            cell.setValue(move.oldValue);
            // If we undid a correct move, set correct/notes?
            // Simplified: Just set value.
            gridLiveData.setValue(grid);
        }
    }
    
    public void erase() {
        if (currentRow == -1 || currentCol == -1) return;
        Grid grid = gridLiveData.getValue();
        if (grid != null) {
            Cell cell = grid.getCell(currentRow, currentCol);
            if (!cell.isLocked()) {
                int oldValue = cell.getValue();
                if (oldValue != 0) {
                    history.push(new Move(currentRow, currentCol, oldValue, 0, false));
                    cell.setValue(0);
                    gridLiveData.setValue(grid);
                }
            }
        }
    }
    
    public void hint() {
        if (gameStateLiveData.getValue() != GameState.PLAYING) return;
        Grid grid = gridLiveData.getValue();
        if (grid == null) return;
        
        // Find a random empty cell
        // Simple scan for first empty
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Cell cell = grid.getCell(r, c);
                if (cell.getValue() == 0) {
                    // Fill it
                    int correctVal = solutionGrid[r][c];
                    cell.setValue(correctVal);
                    cell.setCorrect(true);
                    cell.clearNotes();
                    currentRow = r;
                    currentCol = c; // Move focus
                    gridLiveData.setValue(grid);
                    checkGameCompletion(grid);
                    return;
                }
            }
        }
    }
    
    private void checkGameCompletion(Grid grid) {
        boolean isFull = true;
        for(int r=0; r<9; r++) {
            for(int c=0; c<9; c++) {
                if (grid.getCell(r, c).getValue() == 0) {
                    isFull = false;
                    break;
                }
            }
        }
        
        if (isFull) {
            gameStateLiveData.setValue(GameState.WON);
            // Unlock next level
            new Thread(() -> {
                levelRepository.unlockLevel(currentLevel + 1);
            }).start();
        }
    }
    
    public void toggleNotesMode() {
        boolean current = Boolean.TRUE.equals(notesModeLiveData.getValue());
        notesModeLiveData.setValue(!current);
    }
    
    // Getters for LiveData
    public LiveData<Grid> getGrid() { return gridLiveData; }
    public LiveData<GameState> getGameState() { return gameStateLiveData; }
    public LiveData<String> getTime() { return timeLiveData; }
    public LiveData<Integer> getMistakes() { return mistakesLiveData; }
    public LiveData<Boolean> getNotesMode() { return notesModeLiveData; }
}
