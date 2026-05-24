package com.example.sudoku.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
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
    private View loadingPanel;
    private PreferencesManager preferencesManager;
    private boolean shouldApplyDefaultNotes;
    private boolean defaultNotesApplied;
    private boolean restoredSelectionApplied;
    private boolean endScreenShown;
    private android.widget.Button btnNotes;

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
        NumberPadView numberPad = findViewById(R.id.number_pad);
        timerView = findViewById(R.id.timer_view);
        mistakeText = findViewById(R.id.tv_mistakes);
        hintText = findViewById(R.id.tv_hints);
        loadingPanel = findViewById(R.id.loading_panel);

        // Control Buttons
        android.widget.Button btnUndo = findViewById(R.id.btn_undo);
        android.widget.Button btnRedo = findViewById(R.id.btn_redo);
        android.widget.Button btnErase = findViewById(R.id.btn_erase);
        btnNotes = findViewById(R.id.btn_notes);
        android.widget.Button btnHint = findViewById(R.id.btn_hint);
        android.view.View btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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
        intent.putExtra(Constants.EXTRA_COMPLETION_MISTAKES, mistakes == null ? 0 : mistakes);
        intent.putExtra(Constants.EXTRA_COMPLETION_LEVEL, viewModel.getCurrentLevel());
        startActivity(intent);
        finish();
    }

    private void showGameOverDialog() {
        String summary = viewModel.getPostGameSummary().getValue();
        if (summary == null) summary = getString(R.string.game_over_default_message);
        new AlertDialog.Builder(this)
            .setTitle(R.string.game_over_title)
            .setMessage(summary)
            .setPositiveButton(R.string.game_action_retry, (d, w) -> {
                int level = viewModel.getCurrentLevel();
                viewModel.startNewGame(level, viewModel.isCurrentDailyChallenge());
            })
            .setNegativeButton(R.string.game_action_back_to_menu, (d, w) -> finish())
            .setCancelable(false)
            .show();
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
                        new AlertDialog.Builder(GameActivity.this)
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

        ((android.widget.Button) findViewById(R.id.btn_undo)).setTextColor(textMuted);
        ((android.widget.Button) findViewById(R.id.btn_redo)).setTextColor(textMuted);
        ((android.widget.Button) findViewById(R.id.btn_erase)).setTextColor(textMuted);
        ((android.widget.Button) findViewById(R.id.btn_hint)).setTextColor(textMuted);
        ((android.widget.Button) findViewById(R.id.btn_notes)).setTextColor(textMuted);

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
            difficulty.setText(R.string.game_mode_daily_challenge);
            return;
        }
        String variantLabel = variantLabel(viewModel.getCurrentVariant());
        int level = viewModel.getCurrentLevel();
        if (level <= 5) {
            difficulty.setText(getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_easy), variantLabel));
        } else if (level <= 20) {
            difficulty.setText(getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_medium), variantLabel));
        } else if (level <= 50) {
            difficulty.setText(getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_hard), variantLabel));
        } else if (level <= 200) {
            difficulty.setText(getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_expert), variantLabel));
        } else {
            difficulty.setText(getString(R.string.game_difficulty_with_variant, getString(R.string.difficulty_master), variantLabel));
        }
    }

    private String variantLabel(String variant) {
        if (PreferencesManager.BOARD_VARIANT_4X4.equals(variant)) return "4x4";
        if (PreferencesManager.BOARD_VARIANT_6X6.equals(variant)) return "6x6";
        if (PreferencesManager.BOARD_VARIANT_12X12.equals(variant)) return "12x12";
        if (PreferencesManager.BOARD_VARIANT_16X16.equals(variant)) return "16x16";
        return "9x9";
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
        hintText.setText(message.toString());
        content.findViewById(R.id.btn_hint_more).setOnClickListener(v ->
                hintText.setText(message + "\n\nNext step: scan the highlighted unit and remove impossible candidates."));
        content.findViewById(R.id.btn_hint_apply).setOnClickListener(v -> dialog.dismiss());
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
