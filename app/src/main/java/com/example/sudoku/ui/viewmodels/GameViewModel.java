package com.example.sudoku.ui.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.dao.PuzzlePoolDao;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.DailyChallenge;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.data.local.entities.PuzzlePoolEntry;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.data.repository.LevelRepository;
import com.example.sudoku.data.repository.UserRepository;
import com.example.sudoku.data.preferences.PreferencesManager;
import com.example.sudoku.domain.ai.BehavioralAnalyzer;
import com.example.sudoku.domain.coach.HintResult;
import com.example.sudoku.domain.engine.Difficulty;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.domain.managers.DailyChallengeManager;
import com.example.sudoku.domain.managers.GameStateManager;
import com.example.sudoku.domain.managers.InputResult;
import com.example.sudoku.domain.models.GameState;
import com.example.sudoku.domain.models.Grid;
import com.example.sudoku.utils.Constants;
import com.example.sudoku.utils.GridUtils;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * ViewModel for game screen. Delegates gameplay rules to GameStateManager.
 */
public class GameViewModel extends AndroidViewModel {

    private static final String TAG = "GameViewModel";
    private static final String KEY_CURRENT_ROW = "current_row";
    private static final String KEY_CURRENT_COL = "current_col";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_HINT_LEVEL = "hint_level";
    private static final String KEY_GAME_STARTED = "game_started";

    private final GameRepository gameRepository;
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final PuzzlePoolDao puzzlePoolDao;
    private final DailyChallengeManager dailyChallengeManager;
    private final ExecutorService backgroundExecutor;
    private final GameStateManager stateManager;
    private final SavedStateHandle savedStateHandle;
    private static final String VARIANT_CLASSIC = PreferencesManager.BOARD_VARIANT_9X9;
    private static final long AUTOSAVE_DEBOUNCE_MS = 500L;

    private final Handler autoSaveHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoSaveRunnable = this::performAutoSave;

    private final MutableLiveData<Grid> gridLiveData = new MutableLiveData<>();
    private final MutableLiveData<GameState> gameStateLiveData = new MutableLiveData<>(GameState.IDLE);
    private final MutableLiveData<Integer> mistakesLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> hintsUsedLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> notesModeLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<InputResult> inputResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<HintResult> hintResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> postGameSummaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final BehavioralAnalyzer behavioralAnalyzer;
    private final PreferencesManager preferencesManager;
    private long lastMoveTimeMs = 0;

    private int currentRow = -1;
    private int currentCol = -1;
    private int currentLevel = 1;
    private int hintLevel = 1;
    private boolean gameStarted;
    private boolean currentDailyChallenge;
    private String currentDailyDateKey = "";
    private String currentVariant = PreferencesManager.BOARD_VARIANT_9X9;
    private boolean completionRecorded;

    public GameViewModel(@NonNull Application application) {
        this(application, new SavedStateHandle());
    }

    public GameViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;
        SudokuDatabase db = SudokuDatabase.getInstance(application);
        gameRepository = new GameRepository(db.gameDao());
        levelRepository = new LevelRepository(db.levelProgressDao());
        userRepository = new UserRepository(db.userProfileDao());
        puzzlePoolDao = db.puzzlePoolDao();
        dailyChallengeManager = new DailyChallengeManager(db.dailyChallengeDao(), gameRepository);
        backgroundExecutor = AppExecutors.getInstance().io();
        stateManager = new GameStateManager(gameRepository);
        behavioralAnalyzer = new BehavioralAnalyzer(db.gameDao());
        preferencesManager = new PreferencesManager(application);
        restoreSavedUiState();
    }

    public void startNewGame(int level) {
        startNewGame(level, false);
    }

    public void startNewGame(int level, boolean isDailyChallenge) {
        gameStarted = true;
        savedStateHandle.set(KEY_GAME_STARTED, true);
        currentLevel = level;
        hintLevel = 1;
        currentRow = -1;
        currentCol = -1;
        currentDailyChallenge = isDailyChallenge;
        currentVariant = resolveActiveVariant(preferencesManager.getBoardVariant());
        completionRecorded = false;
        persistUiState();
        loadingLiveData.setValue(true);
        notesModeLiveData.setValue(false);

        backgroundExecutor.execute(() -> {
            try {
                levelRepository.ensureLevelsInitializedSync();
                userRepository.ensureProfileExistsSync("default_user");

                if (isDailyChallenge) {
                    startOrResumeDailyGame();
                    return;
                }

                int playableLevel = resolvePlayableLevel(level);
                currentLevel = playableLevel;
                savedStateHandle.set(KEY_CURRENT_LEVEL, currentLevel);
                Difficulty difficulty = difficultyForLevel(playableLevel);
                SudokuGenerator.Puzzle puzzle = takeFromPoolOrGenerate(difficulty, playableLevel, currentVariant);
                SavedGame savedGame = new SavedGame(UUID.randomUUID().toString(),
                        playableLevel, puzzle.puzzle, puzzle.solution, lockedCellsFor(puzzle.puzzle));
                savedGame.isDailyChallenge = false;
                savedGame.difficulty = difficulty.name();
                savedGame.variant = currentVariant;
                gameRepository.saveGameSync(savedGame);
                gameRepository.deleteIncompleteGamesForLevelExceptSync(playableLevel, savedGame.gameId);

                loadIntoStateManager(savedGame, true);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start new game", e);
                gameStarted = false;
                savedStateHandle.set(KEY_GAME_STARTED, false);
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Could not generate puzzle. Please try again.");
            }
        });
    }

    public void loadSavedGame(String gameId) {
        gameStarted = true;
        savedStateHandle.set(KEY_GAME_STARTED, true);
        loadingLiveData.setValue(true);
        hintLevel = 1;
        persistUiState();

        backgroundExecutor.execute(() -> {
            try {
                SavedGame game = gameRepository.getSavedGame(gameId);
                if (game == null) {
                    Log.w(TAG, "No saved game found for id: " + gameId);
                    gameStarted = false;
                    savedStateHandle.set(KEY_GAME_STARTED, false);
                    loadingLiveData.postValue(false);
                    return;
                }

                currentLevel = game.levelNumber;
                currentDailyChallenge = game.isDailyChallenge;
                currentVariant = resolveActiveVariant(game.variant);
                currentDailyDateKey = game.isDailyChallenge
                        ? dailyChallengeManager.getDateKey(game.startedAt)
                        : "";
                currentRow = game.selectedRow;
                currentCol = game.selectedCol;
                completionRecorded = false;
                notesModeLiveData.postValue(game.notesMode);
                savedStateHandle.set(KEY_CURRENT_LEVEL, currentLevel);
                loadIntoStateManager(game, !game.isCompleted);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load saved game", e);
                gameStarted = false;
                savedStateHandle.set(KEY_GAME_STARTED, false);
                loadingLiveData.postValue(false);
                errorLiveData.postValue("Could not load saved game.");
            }
        });
    }

    public void onCellClicked(int row, int col) {
        currentRow = row;
        currentCol = col;
        hintLevel = 1;
        persistUiState();
        stateManager.setUiState(currentRow, currentCol, Boolean.TRUE.equals(notesModeLiveData.getValue()));
        scheduleAutoSave();
    }

    public void onNumberInput(int number) {
        boolean noteMode = Boolean.TRUE.equals(notesModeLiveData.getValue());
        InputResult result = stateManager.processInput(currentRow, currentCol, number, noteMode);
        handleInputResult(result);
        if (result != null
                && result.type != InputResult.Type.IGNORED
                && result.type != InputResult.Type.LOCKED_CELL) {
            boolean correct = result.type == InputResult.Type.CORRECT_MOVE
                    || result.type == InputResult.Type.PUZZLE_COMPLETED;
            recordBehavior(currentRow, currentCol, number, correct);
        }
        hintLevel = 1;
        persistUiState();
        scheduleAutoSave();
    }

    private void recordBehavior(int row, int col, int number, boolean correct) {
        long now = System.currentTimeMillis();
        long thinkingTime = lastMoveTimeMs > 0 ? now - lastMoveTimeMs : 0;
        lastMoveTimeMs = now;
        String gameId = stateManager.getCurrentGameId();
        if (gameId == null || gameId.isEmpty()) return;
        // Capture current grid on the calling thread before dispatching to IO
        Grid gridSnapshot = stateManager.getGridSnapshot();
        String currentGridStr = "000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        if (gridSnapshot != null) {
            int[][] arr = gridSnapshot.toIntArray();
            StringBuilder sb = new StringBuilder(81);
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    sb.append(arr[r][c]);
                }
            }
            currentGridStr = sb.toString();
        }
        final long thinking = thinkingTime;
        final int levelCapture = currentLevel;
        final String gridStr = currentGridStr;
        backgroundExecutor.execute(() -> {
            SavedGame snap = new SavedGame(gameId, levelCapture, "", "", "");
            snap.currentGrid = gridStr;
            behavioralAnalyzer.recordCellInteraction(
                    gameId, row, col, number, correct, thinking, snap);
        });
    }

    public void undo() {
        handleInputResult(stateManager.undo());
        hintLevel = 1;
        persistUiState();
        scheduleAutoSave();
    }

    public void redo() {
        handleInputResult(stateManager.redo());
        hintLevel = 1;
        persistUiState();
        scheduleAutoSave();
    }

    public void erase() {
        handleInputResult(stateManager.erase(currentRow, currentCol));
        hintLevel = 1;
        persistUiState();
        scheduleAutoSave();
    }

    public void hint() {
        HintResult hint = stateManager.requestHint(hintLevel);
        hintResultLiveData.setValue(hint);
        if (hint.available) {
            hintLevel = hint.hintLevel >= 4 ? 1 : hint.hintLevel + 1;
            persistUiState();
        }
        publishState();
        if (stateManager.getCurrentState() == GameState.WON) {
            gameStateLiveData.setValue(GameState.WON);
            publishPostGameSummary();
            recordCompletedGame();
        }
    }

    public void toggleNotesMode() {
        boolean current = Boolean.TRUE.equals(notesModeLiveData.getValue());
        notesModeLiveData.setValue(!current);
        stateManager.setUiState(currentRow, currentCol, !current);
        scheduleAutoSave();
    }

    public void saveCurrentGame() {
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        stateManager.saveCurrentGame();
    }

    public void pauseGame() {
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        stateManager.pauseGame();
        publishState();
    }

    private void scheduleAutoSave() {
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        autoSaveHandler.postDelayed(autoSaveRunnable, AUTOSAVE_DEBOUNCE_MS);
    }

    private void performAutoSave() {
        backgroundExecutor.execute(() -> {
            try {
                stateManager.saveCurrentGame();
            } catch (Exception e) {
                Log.e(TAG, "Auto-save failed", e);
            }
        });
    }

    public void resumeGame() {
        stateManager.resumeGame();
        publishState();
    }

    public long getElapsedTimeMs() {
        return stateManager.getElapsedTimeMs();
    }

    private void handleInputResult(InputResult result) {
        if (result == null) return;
        inputResultLiveData.setValue(result);
        publishState();
        if (result.type == InputResult.Type.PUZZLE_COMPLETED) {
            publishPostGameSummary();
            recordCompletedGame();
        } else if (result.type == InputResult.Type.GAME_OVER) {
            publishPostGameSummary();
            recordLostGame();
        }
    }

    private void publishState() {
        gridLiveData.postValue(stateManager.getGridSnapshot());
        gameStateLiveData.postValue(stateManager.getCurrentState());
        mistakesLiveData.postValue(stateManager.getMistakes());
        hintsUsedLiveData.postValue(stateManager.getHintsUsed());
    }

    private void publishPostGameSummary() {
        String summary = "Time: " + formatTime(stateManager.getElapsedTimeMs())
                + "\nMistakes: " + stateManager.getMistakes()
                + "\nHints Used: " + stateManager.getHintsUsed()
                + "\nScore: " + stateManager.getLastScore()
                + "\nStars: " + stateManager.getLastStars()
                + "\n\nCoach Tip:\n" + stateManager.getCoachTip();
        postGameSummaryLiveData.postValue(summary);
    }

    private void recordCompletedGame() {
        if (completionRecorded) return;
        completionRecorded = true;
        backgroundExecutor.execute(() -> {
            try {
                int score = stateManager.getLastScore();
                int stars = stateManager.getLastStars();
                long timeMs = stateManager.getElapsedTimeMs();
                if (stateManager.isDailyChallenge()) {
                    String dateKey = currentDailyDateKey == null || currentDailyDateKey.isEmpty()
                            ? dailyChallengeManager.getTodayKey()
                            : currentDailyDateKey;
                    dailyChallengeManager.completeChallengeSync(dateKey, score, timeMs);
                } else {
                    levelRepository.completeLevelSync(currentLevel, stars, score, timeMs);
                }
                recordHistory(true, score, stars, timeMs);
                int xp = Math.max(0, score / 10);
                if (stateManager.isDailyChallenge()) {
                    xp *= Constants.DAILY_CHALLENGE_XP_MULTIPLIER;
                }
                int profileLevel = stateManager.isDailyChallenge()
                        ? levelRepository.getHighestUnlockedLevelSync()
                        : Math.min(Constants.MAX_LEVEL, currentLevel + 1);
                userRepository.recordGameResultSync("default_user", true, stars,
                        stateManager.getHintsUsed(), timeMs, xp, profileLevel,
                        System.currentTimeMillis());
            } catch (Exception e) {
                Log.e(TAG, "Failed to record completed game", e);
            }
        });
    }

    private void recordLostGame() {
        if (completionRecorded) return;
        completionRecorded = true;
        backgroundExecutor.execute(() -> {
            try {
                long timeMs = stateManager.getElapsedTimeMs();
                recordHistory(false, 0, 0, timeMs);
                userRepository.recordGameResultSync("default_user", false, 0,
                        stateManager.getHintsUsed(), timeMs, 0, currentLevel,
                        System.currentTimeMillis());
            } catch (Exception e) {
                Log.e(TAG, "Failed to record lost game", e);
            }
        });
    }

    private void recordHistory(boolean won, int score, int stars, long timeMs) {
        GameHistory history = new GameHistory();
        history.gameId = stateManager.getCurrentGameId();
        history.levelNumber = currentLevel;
        history.playedAt = System.currentTimeMillis();
        history.totalTimeMs = timeMs;
        history.finalScore = score;
        history.xpEarned = won ? Math.max(0, score / 10) : 0;
        history.starsEarned = stars;
        history.hintsUsed = stateManager.getHintsUsed();
        history.mistakesMade = stateManager.getMistakes();
        history.avgDecisionTimeMs = 0;
        history.accuracyRate = won ? 1.0 : 0.0;
        history.isDailyChallenge = stateManager.isDailyChallenge();
        history.isPerfectGame = won && stateManager.getMistakes() == 0 && stateManager.getHintsUsed() == 0;
        gameRepository.recordGameHistorySync(history);
    }

    public boolean hasActiveGame() {
        return gameStarted || gridLiveData.getValue() != null || stateManager.getCurrentState() != GameState.IDLE;
    }

    private void restoreSavedUiState() {
        Integer restoredRow = savedStateHandle.get(KEY_CURRENT_ROW);
        Integer restoredCol = savedStateHandle.get(KEY_CURRENT_COL);
        Integer restoredLevel = savedStateHandle.get(KEY_CURRENT_LEVEL);
        Integer restoredHintLevel = savedStateHandle.get(KEY_HINT_LEVEL);
        Boolean restoredGameStarted = savedStateHandle.get(KEY_GAME_STARTED);

        currentRow = restoredRow != null ? restoredRow : -1;
        currentCol = restoredCol != null ? restoredCol : -1;
        currentLevel = restoredLevel != null ? restoredLevel : 1;
        hintLevel = restoredHintLevel != null ? restoredHintLevel : 1;
        gameStarted = Boolean.TRUE.equals(restoredGameStarted);
    }

    private void persistUiState() {
        savedStateHandle.set(KEY_CURRENT_ROW, currentRow);
        savedStateHandle.set(KEY_CURRENT_COL, currentCol);
        savedStateHandle.set(KEY_CURRENT_LEVEL, currentLevel);
        savedStateHandle.set(KEY_HINT_LEVEL, hintLevel);
        savedStateHandle.set(KEY_GAME_STARTED, gameStarted);
        stateManager.setUiState(currentRow, currentCol, Boolean.TRUE.equals(notesModeLiveData.getValue()));
    }

    private Difficulty difficultyForLevel(int level) {
        if (level <= 5) return Difficulty.EASY;
        if (level <= 20) return Difficulty.MEDIUM;
        if (level <= 50) return Difficulty.HARD;
        if (level <= 200) return Difficulty.EXPERT;
        return Difficulty.MASTER;
    }

    private SudokuGenerator.Puzzle takeFromPoolOrGenerate(Difficulty difficulty, int level, String variant) {
        PuzzlePoolEntry pooled = puzzlePoolDao.consumeOne(difficulty.name(), variant);
        if (pooled != null) {
            return new SudokuGenerator.Puzzle(
                    pooled.puzzle, pooled.solution, pooled.clueCount, pooled.canonicalHash);
        }
        SudokuGenerator generator = new SudokuGenerator(System.currentTimeMillis());
        return generator.generate(difficulty.targetClues());
    }

    private void startOrResumeDailyGame() {
        long startOfDay = dailyChallengeManager.getStartOfTodayMillis();
        long endOfDay = dailyChallengeManager.getEndOfTodayMillis();
        SavedGame existing = gameRepository.getDailyGameForDaySync(startOfDay, endOfDay);
        if (existing != null) {
            currentLevel = existing.levelNumber;
            currentVariant = resolveActiveVariant(existing.variant);
            currentDailyDateKey = dailyChallengeManager.getDateKey(existing.startedAt);
            currentRow = existing.selectedRow;
            currentCol = existing.selectedCol;
            notesModeLiveData.postValue(existing.notesMode);
            savedStateHandle.set(KEY_CURRENT_LEVEL, currentLevel);
            loadIntoStateManager(existing, !existing.isCompleted);
            return;
        }

        DailyChallenge challenge = dailyChallengeManager.getOrCreateDailyChallengeSync();
        int dailyLevel = resolvePlayableLevel(1);
        currentLevel = dailyLevel;
        currentDailyDateKey = challenge.dateKey;
        savedStateHandle.set(KEY_CURRENT_LEVEL, currentLevel);

        SavedGame savedGame = new SavedGame(UUID.randomUUID().toString(),
                dailyLevel, challenge.puzzleGrid, challenge.solutionGrid,
                lockedCellsFor(challenge.puzzleGrid));
        savedGame.isDailyChallenge = true;
        savedGame.difficulty = Difficulty.MEDIUM.name();
        savedGame.variant = currentVariant;
        gameRepository.saveGameSync(savedGame);
        gameRepository.deleteIncompleteDailyGamesForDayExceptSync(
                startOfDay, endOfDay, savedGame.gameId);
        loadIntoStateManager(savedGame, true);
    }

    private int resolvePlayableLevel(int requestedLevel) {
        int clamped = Math.max(1, Math.min(Constants.MAX_LEVEL, requestedLevel));
        if (levelRepository.isLevelUnlockedSync(clamped)) {
            return clamped;
        }
        return levelRepository.getHighestUnlockedLevelSync();
    }

    private String resolveActiveVariant(String preferredVariant) {
        if (PreferencesManager.BOARD_VARIANT_9X9.equals(preferredVariant)) {
            return PreferencesManager.BOARD_VARIANT_9X9;
        }
        return VARIANT_CLASSIC;
    }

    private String lockedCellsFor(String puzzleGrid) {
        int[][] currentGrid = GridUtils.parseGrid(puzzleGrid);
        StringBuilder lockedStr = new StringBuilder(81);
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                lockedStr.append(currentGrid[row][col] != 0 ? '1' : '0');
            }
        }
        return lockedStr.toString();
    }

    private void loadIntoStateManager(SavedGame savedGame, boolean shouldResume) {
        stateManager.loadGame(savedGame);
        stateManager.setUiState(currentRow, currentCol, Boolean.TRUE.equals(notesModeLiveData.getValue()));
        if (shouldResume) {
            stateManager.resumeGame();
        }
        publishState();
        loadingLiveData.postValue(false);
    }

    private String formatTime(long elapsedMs) {
        int totalSeconds = (int) (elapsedMs / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        try {
            stateManager.saveCurrentGame();
        } catch (Exception ignored) {
        }
    }

    public LiveData<Grid> getGrid() { return gridLiveData; }
    public LiveData<GameState> getGameState() { return gameStateLiveData; }
    public LiveData<Integer> getMistakes() { return mistakesLiveData; }
    public LiveData<Integer> getHintsUsed() { return hintsUsedLiveData; }
    public LiveData<Boolean> getNotesMode() { return notesModeLiveData; }
    public LiveData<InputResult> getInputResult() { return inputResultLiveData; }
    public LiveData<HintResult> getHintResult() { return hintResultLiveData; }
    public LiveData<String> getPostGameSummary() { return postGameSummaryLiveData; }
    public LiveData<String> getError() { return errorLiveData; }
    public int getCurrentLevel() { return currentLevel; }
    public int getSelectedRow() { return stateManager.getSavedSelectedRow(); }
    public int getSelectedCol() { return stateManager.getSavedSelectedCol(); }
    public boolean isCurrentDailyChallenge() { return currentDailyChallenge; }
    public String getCurrentVariant() { return currentVariant; }
}
