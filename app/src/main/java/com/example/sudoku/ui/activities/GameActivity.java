package com.example.sudoku.ui.activities;

import android.app.AlertDialog;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.sudoku.R;
import com.example.sudoku.data.preferences.PreferencesManager;
import com.example.sudoku.domain.coach.HintResult;
import com.example.sudoku.domain.managers.InputResult;
import com.example.sudoku.domain.models.GameState;
import com.example.sudoku.ui.viewmodels.GameViewModel;
import com.example.sudoku.ui.views.NumberPadView;
import com.example.sudoku.ui.views.SudokuGridView;
import com.example.sudoku.ui.views.TimerView;
import com.example.sudoku.utils.Constants;

/**
 * The main Gameplay Activity.
 * Handles full lifecycle: pause saves game, resume restores timer.
 */
public class GameActivity extends BaseActivity {

    private GameViewModel viewModel;
    private SudokuGridView gridView;
    private TimerView timerView;
    private TextView mistakeText;
    private TextView hintText;
    private TextView progressChip;
    private TextView floatingFeedbackText;
    private ProgressBar progressBar;
    private View loadingPanel;
    private PreferencesManager preferencesManager;
    private NumberPadView numberPad;
    private boolean shouldApplyDefaultNotes;
    private boolean defaultNotesApplied;
    private boolean restoredSelectionApplied;
    private boolean endScreenShown;
    private Button btnNotes;
    private String currentDifficultyLabel = "";
    private long lastMotivationTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStoredThemeMode();
        setContentView(R.layout.activity_game);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        preferencesManager = new PreferencesManager(this);
        shouldApplyDefaultNotes = !getIntent().hasExtra(Constants.EXTRA_GAME_ID)
                && preferencesManager.isDefaultNotesEnabled();

        gridView = findViewById(R.id.sudoku_grid);
        gridView.setHighlightSameNumbers(preferencesManager.isHighlightSameNumberEnabled());
        gridView.setHighlightConflicts(preferencesManager.isHighlightConflictsEnabled());
        numberPad = findViewById(R.id.number_pad);
        timerView = findViewById(R.id.timer_view);
        mistakeText = findViewById(R.id.tv_mistakes);
        hintText = findViewById(R.id.tv_hints);
        progressChip = findViewById(R.id.tv_progress_chip);
        progressBar = findViewById(R.id.progress_bar_game);
        floatingFeedbackText = findViewById(R.id.tv_feedback_floating);
        loadingPanel = findViewById(R.id.loading_panel);

        // Control Buttons
        Button btnUndo = findViewById(R.id.btn_undo);
        Button btnRedo = findViewById(R.id.btn_redo);
        Button btnErase = findViewById(R.id.btn_erase);
        btnNotes = findViewById(R.id.btn_notes);
        Button btnHint = findViewById(R.id.btn_hint);
        android.view.View btnBack = findViewById(R.id.btn_back);
        android.view.View btnPause = findViewById(R.id.btn_pause);
        android.view.View btnSettings = findViewById(R.id.btn_game_settings);

        applyDuolingoPressEffect(btnBack, btnPause, btnSettings, btnUndo, btnRedo, btnErase, btnNotes, btnHint);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnPause.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnUndo.setOnClickListener(v -> viewModel.undo());
        btnRedo.setOnClickListener(v -> viewModel.redo());
        btnErase.setOnClickListener(v -> viewModel.erase());
        btnNotes.setOnClickListener(v -> viewModel.toggleNotesMode());
        btnHint.setOnClickListener(v -> viewModel.hint());

        viewModel.getNotesMode().observe(this, isNotes -> {
            boolean enabled = Boolean.TRUE.equals(isNotes);
            btnNotes.setSelected(enabled);
            btnNotes.setText(enabled
                    ? getString(R.string.control_pencil_on)
                    : getString(R.string.control_pencil));
            btnNotes.setTextColor(getColor(enabled ? R.color.accent_primary_pressed : R.color.text_muted));
            btnNotes.setBackgroundResource(enabled ? R.drawable.chip_accent : R.drawable.neo_icon_button);
        });

        // Setup Grid Interaction
        gridView.setListener((row, col) -> viewModel.onCellClicked(row, col));

        // Setup Number Pad
        numberPad.setListener(number -> {
            if (number == 0) {
                viewModel.erase();
            } else {
                viewModel.onNumberInput(number);
            }
        });

        // Observe ViewModel
        setLoadingVisible(true);
        viewModel.getGrid().observe(this, grid -> {
            gridView.setGrid(grid);
            updateProgressUi(grid);
            updateNumberPadCompletion(grid);
            int selectedRow = viewModel.getSelectedRow();
            int selectedCol = viewModel.getSelectedCol();
            if (!restoredSelectionApplied && selectedRow >= 0 && selectedCol >= 0) {
                restoredSelectionApplied = true;
                gridView.selectCell(selectedRow, selectedCol);
            }
            updateDifficultyLabel();
            setLoadingVisible(false);
            if (shouldApplyDefaultNotes && !defaultNotesApplied
                    && !Boolean.TRUE.equals(viewModel.getNotesMode().getValue())) {
                defaultNotesApplied = true;
                viewModel.toggleNotesMode();
            }
        });

        viewModel.getMistakes().observe(this, mistakes -> {
            int count = mistakes != null ? mistakes : 0;
            mistakeText.setText(getString(R.string.game_mistakes_status, count));
            mistakeText.setContentDescription(getString(R.string.game_mistakes_content_description, count));
        });

        viewModel.getHintsUsed().observe(this, hintsUsed -> {
            int used = hintsUsed != null ? hintsUsed : 0;
            int remaining = Math.max(0, Constants.MAX_HINTS_PER_GAME - used);
            hintText.setText(getString(R.string.game_hints_status, used, remaining));
            hintText.setContentDescription(getString(R.string.game_hints_content_description, used, remaining));
            btnHint.setContentDescription(getString(R.string.game_hints_content_description, used, remaining));
        });

        viewModel.getInputResult().observe(this, result -> {
            if (result == null) return;
            if (result.type == InputResult.Type.WRONG_MOVE
                    && preferencesManager.isVibrationEnabled()
                    && preferencesManager.getHapticIntensity() > 0) {
                gridView.performHapticFeedback(HapticFeedbackConstants.REJECT);
            }
            if (result.type == InputResult.Type.CORRECT_MOVE || result.type == InputResult.Type.PUZZLE_COMPLETED) {
                gridView.animateCorrectInput();
                showFloatingFeedback(pickPositiveFeedback(result.type == InputResult.Type.PUZZLE_COMPLETED), true);
            } else if (result.type == InputResult.Type.WRONG_MOVE) {
                gridView.animateWrongInput();
                showFloatingFeedback(getString(R.string.game_feedback_try_again), false);
            } else if (result.type == InputResult.Type.NOTE_UPDATED) {
                showFloatingFeedback(getString(R.string.game_feedback_notes_saved), null);
            }
            if (result.type == InputResult.Type.LOCKED_CELL
                    || (result.type == InputResult.Type.WRONG_MOVE && areMistakeAlertsEnabled())) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getHintResult().observe(this, hint -> {
            if (hint == null) return;
            showHintDialog(hint);
        });

        viewModel.getGameState().observe(this, this::handleGameStateChange);

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // Handle back press with pause dialog
        setupBackPressHandler();

        // Apply theme to specific views
        applyTheme_Local(currentTheme);

        // Entrance animation for grid
        gridView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_up));

        // Start or resume game
        if (!viewModel.hasActiveGame()) {
            if (getIntent().hasExtra(Constants.EXTRA_GAME_ID)) {
                String gameId = getIntent().getStringExtra(Constants.EXTRA_GAME_ID);
                viewModel.loadSavedGame(gameId);
            } else {
                int level = getIntent().getIntExtra(Constants.EXTRA_LEVEL_NUMBER, 1);
                boolean isDaily = getIntent().getBooleanExtra(Constants.EXTRA_IS_DAILY_CHALLENGE, false);
                viewModel.startNewGame(level, isDaily);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save game state when activity goes to background
        if (viewModel.getGameState().getValue() == GameState.PLAYING) {
            if (preferencesManager.isTimerEnabled()) {
                timerView.stop();
            }
            viewModel.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume timer if game was playing
        GameState state = viewModel.getGameState().getValue();
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            viewModel.resumeGame();
            if (preferencesManager.isTimerEnabled()) {
                timerView.setTime(viewModel.getElapsedTimeMs());
                timerView.start();
            }
        }
    }

    private void handleGameStateChange(GameState state) {
        if (state == null) return;

        switch (state) {
            case PLAYING:
                if (preferencesManager.isTimerEnabled()) {
                    timerView.setTime(viewModel.getElapsedTimeMs());
                    timerView.start();
                }
                break;
            case WON:
                if (preferencesManager.isTimerEnabled()) {
                    timerView.stop();
                }
                showVictoryDialog();
                break;
            case GAME_OVER:
                if (preferencesManager.isTimerEnabled()) {
                    timerView.stop();
                }
                showGameOverDialog();
                break;
            case PAUSED:
                if (preferencesManager.isTimerEnabled()) {
                    timerView.stop();
                }
                break;
            default:
                break;
        }
    }

    private void showVictoryDialog() {
        if (endScreenShown) return;
        endScreenShown = true;
        Intent intent = new Intent(this, CompletedActivity.class);
        intent.putExtra(Constants.EXTRA_COMPLETION_TIME, viewModel.getElapsedTimeMs());
        Integer mistakes = viewModel.getMistakes().getValue();
        Integer hintsUsed = viewModel.getHintsUsed().getValue();
        intent.putExtra(Constants.EXTRA_COMPLETION_MISTAKES, mistakes == null ? 0 : mistakes);
        intent.putExtra(Constants.EXTRA_COMPLETION_LEVEL, viewModel.getCurrentLevel());
        intent.putExtra("extra_completion_hints_used", hintsUsed == null ? 0 : hintsUsed);
        intent.putExtra("extra_completion_difficulty", currentDifficultyLabel);
        startActivity(intent);
        finish();
    }

    private void showGameOverDialog() {
        String summary = viewModel.getPostGameSummary().getValue();
        if (summary == null) summary = getString(R.string.game_over_default_message);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.game_over_title)
            .setMessage(summary)
            .setPositiveButton(R.string.game_action_retry, (d, w) -> {
                int level = viewModel.getCurrentLevel();
                viewModel.startNewGame(level, viewModel.isCurrentDailyChallenge());
            })
            .setNegativeButton(R.string.game_action_back_to_menu, (d, w) -> finish())
            .setCancelable(false)
            .show();
        styleDialogButtons(dialog);
    }

    /**
     * Handle back press via OnBackPressedDispatcher (non-deprecated).
     * Registered in onCreate via getOnBackPressedDispatcher().
     */
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this,
            new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (viewModel.getGameState().getValue() == GameState.PLAYING) {
                        timerView.stop();
                        viewModel.pauseGame();
                        AlertDialog dialog = new AlertDialog.Builder(GameActivity.this)
                            .setTitle(R.string.game_paused_title)
                            .setMessage(R.string.game_paused_message)
                            .setPositiveButton(R.string.game_action_resume, (d, w) -> {
                                viewModel.resumeGame();
                                if (preferencesManager.isTimerEnabled()) {
                                    timerView.start();
                                }
                            })
                            .setNegativeButton(R.string.game_action_quit, (d, w) -> {
                                viewModel.saveCurrentGame();
                                finish();
                            })
                            .setNeutralButton(R.string.game_action_restart, (d, w) -> {
                                int level = viewModel.getCurrentLevel();
                                viewModel.startNewGame(level, viewModel.isCurrentDailyChallenge());
                            })
                            .setCancelable(false)
                            .show();
                        styleDialogButtons(dialog);
                    } else {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
    }

    private void applyTheme_Local(com.example.sudoku.utils.ThemeUtils.Theme theme) {
        int textMuted = getColor(R.color.text_muted);
        int textPrimary = getColor(R.color.text_primary);

        gridView.updateTheme(theme.accentColor, theme.backgroundColor, theme.primaryTextColor, theme.gridLineColor);

        ((Button) findViewById(R.id.btn_undo)).setTextColor(textMuted);
        ((Button) findViewById(R.id.btn_redo)).setTextColor(textMuted);
        ((Button) findViewById(R.id.btn_erase)).setTextColor(textMuted);
        ((Button) findViewById(R.id.btn_hint)).setTextColor(textMuted);
        ((Button) findViewById(R.id.btn_notes)).setTextColor(textMuted);

        ((NumberPadView) findViewById(R.id.number_pad)).updateTheme(theme.accentColor, theme.secondaryColor);

        mistakeText.setTextColor(getColor(R.color.accent_danger));
        hintText.setTextColor(getColor(R.color.accent_warning));
        timerView.setTextColor(textPrimary);
        timerView.setVisibility(preferencesManager.isTimerEnabled() ? View.VISIBLE : View.GONE);
    }

    private void setLoadingVisible(boolean visible) {
        if (loadingPanel != null) {
            loadingPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (gridView != null) {
            gridView.setEnabled(!visible);
        }
    }

    private boolean areMistakeAlertsEnabled() {
        return preferencesManager.isMistakeAlertsEnabled();
    }

    private void updateDifficultyLabel() {
        TextView difficulty = findViewById(R.id.tv_difficulty);
        if (viewModel.isCurrentDailyChallenge()) {
            currentDifficultyLabel = getString(R.string.game_mode_daily_challenge);
            difficulty.setText(currentDifficultyLabel);
            return;
        }
        String variantLabel = variantLabel(viewModel.getCurrentVariant());
        int level = viewModel.getCurrentLevel();
        if (level <= 5) {
            currentDifficultyLabel = getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_easy), variantLabel);
        } else if (level <= 20) {
            currentDifficultyLabel = getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_medium), variantLabel);
        } else if (level <= 50) {
            currentDifficultyLabel = getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_hard), variantLabel);
        } else if (level <= 200) {
            currentDifficultyLabel = getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_expert), variantLabel);
        } else {
            currentDifficultyLabel = getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_master), variantLabel);
        }
        difficulty.setText(currentDifficultyLabel);
    }

    private String variantLabel(String variant) {
        if (PreferencesManager.BOARD_VARIANT_4X4.equals(variant)) return "4x4";
        if (PreferencesManager.BOARD_VARIANT_6X6.equals(variant)) return "6x6";
        if (PreferencesManager.BOARD_VARIANT_12X12.equals(variant)) return "12x12";
        if (PreferencesManager.BOARD_VARIANT_16X16.equals(variant)) return "16x16";
        return "9x9";
    }

    private void updateProgressUi(com.example.sudoku.domain.models.Grid grid) {
        if (grid == null || progressBar == null || progressChip == null) return;
        int filled = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                com.example.sudoku.domain.models.Cell cell = grid.getCell(row, col);
                if (cell != null && cell.getValue() > 0) {
                    filled++;
                }
            }
        }
        int progress = Math.min(100, Math.round((filled * 100f) / 81f));
        progressBar.animate().cancel();
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), progress)
                .setDuration(220)
                .start();
        progressChip.setText(getString(R.string.game_progress_percent, progress));
    }

    private void updateNumberPadCompletion(com.example.sudoku.domain.models.Grid grid) {
        if (grid == null || numberPad == null) return;
        int[] counts = new int[10];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                com.example.sudoku.domain.models.Cell cell = grid.getCell(row, col);
                if (cell != null && cell.getValue() > 0 && cell.getValue() <= 9) {
                    counts[cell.getValue()]++;
                }
            }
        }
        boolean[] completed = new boolean[10];
        for (int i = 1; i <= 9; i++) {
            completed[i] = counts[i] >= 9;
        }
        numberPad.setCompletedNumbers(completed);
    }

    private String pickPositiveFeedback(boolean completed) {
        if (completed) return getString(R.string.game_feedback_complete);
        long now = System.currentTimeMillis();
        if (now - lastMotivationTimestamp < 1100) {
            return getString(R.string.game_feedback_nice);
        }
        lastMotivationTimestamp = now;
        int idx = (int) (now % 5);
        switch (idx) {
            case 0: return getString(R.string.game_feedback_nice);
            case 1: return getString(R.string.game_feedback_great_move);
            case 2: return getString(R.string.game_feedback_keep_going);
            case 3: return getString(R.string.game_feedback_almost_there);
            default: return getString(R.string.game_feedback_perfect);
        }
    }

    private void showFloatingFeedback(String text, Boolean positive) {
        if (floatingFeedbackText == null || text == null || text.isEmpty()) return;
        floatingFeedbackText.animate().cancel();
        floatingFeedbackText.setText(text);
        if (Boolean.TRUE.equals(positive)) {
            floatingFeedbackText.setBackgroundResource(R.drawable.chip_accent);
            floatingFeedbackText.setTextColor(getColor(R.color.accent_primary_pressed));
        } else if (Boolean.FALSE.equals(positive)) {
            floatingFeedbackText.setBackgroundResource(R.drawable.chip_danger);
            floatingFeedbackText.setTextColor(getColor(R.color.accent_danger));
        } else {
            floatingFeedbackText.setBackgroundResource(R.drawable.chip_warning);
            floatingFeedbackText.setTextColor(getColor(R.color.accent_warning));
        }
        floatingFeedbackText.setVisibility(View.VISIBLE);
        floatingFeedbackText.setAlpha(0f);
        floatingFeedbackText.setTranslationY(dpToPx(14));
        floatingFeedbackText.setScaleX(0.94f);
        floatingFeedbackText.setScaleY(0.94f);
        floatingFeedbackText.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(140)
                .withEndAction(() -> floatingFeedbackText.animate()
                        .alpha(0f)
                        .translationY(-dpToPx(8))
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setStartDelay(520)
                        .setDuration(220)
                        .withEndAction(() -> floatingFeedbackText.setVisibility(View.GONE))
                        .start())
                .start();
    }

    private void styleDialogButtons(AlertDialog dialog) {
        if (dialog == null) return;
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        applyDuolingoPressEffect(positive, negative, neutral);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void showHintDialog(HintResult hint) {
        StringBuilder message = new StringBuilder();
        message.append(hint.shortText);
        if (hint.longText != null && !hint.longText.equals(hint.shortText)) {
            message.append("\n\n").append(hint.longText);
        }
        if (hint.available) {
            if (hint.strategyName != null && !hint.strategyName.isEmpty()) {
                message.append("\n\nStrategy: ").append(hint.strategyName.replace('_', ' '));
            }
            if (hint.targetRow >= 0 && hint.targetCol >= 0) {
                message.append("\nCell: R").append(hint.targetRow + 1)
                        .append("C").append(hint.targetCol + 1);
            }
            if (hint.value > 0) {
                message.append("\nValue: ").append(hint.value);
            }
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_hint, null);
        TextView hintText = content.findViewById(R.id.tv_hint_text);
        Button moreButton = content.findViewById(R.id.btn_hint_more);
        Button applyButton = content.findViewById(R.id.btn_hint_apply);
        hintText.setText(message.toString());
        applyDuolingoPressEffect(moreButton, applyButton);
        moreButton.setOnClickListener(v ->
                hintText.setText(message + "\n\nNext step: scan the highlighted unit and remove impossible candidates."));
        applyButton.setOnClickListener(v -> dialog.dismiss());
        content.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in));
        dialog.setContentView(content);
        dialog.show();
    }

    private void applyStoredThemeMode() {
        String theme = new PreferencesManager(this).getTheme();
        if ("DARK".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("SYSTEM".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
