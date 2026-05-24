package com.example.sudoku.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.sudoku.R;
import com.example.sudoku.data.preferences.PreferencesManager;

public class SettingsActivity extends BaseActivity {

    private PreferencesManager preferencesManager;
    private Button soundButton;
    private Button vibrationButton;
    private Button themeButton;
    private Button mistakeAlertsButton;
    private Button notesButton;
    private Button timerButton;
    private Button highlightConflictsButton;
    private Button highlightSameButton;
    private Button boardPresetButton;
    private Button inputModeButton;
    private Button hapticButton;
    private Button dailyRemindersButton;
    private Button quietHoursButton;
    private Button highContrastButton;
    private Button colorBlindButton;
    private Button leftHandedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferencesManager = new PreferencesManager(this);
        soundButton = findViewById(R.id.btn_sound);
        vibrationButton = findViewById(R.id.btn_vibration);
        themeButton = findViewById(R.id.btn_theme);
        mistakeAlertsButton = findViewById(R.id.btn_mistake_limit);
        notesButton = findViewById(R.id.btn_notes_help);
        timerButton = findViewById(R.id.btn_timer);
        highlightConflictsButton = findViewById(R.id.btn_highlight_conflicts);
        highlightSameButton = findViewById(R.id.btn_highlight_same);
        boardPresetButton = findViewById(R.id.btn_board_preset);
        inputModeButton = findViewById(R.id.btn_input_mode);
        hapticButton = findViewById(R.id.btn_haptic_intensity);
        dailyRemindersButton = findViewById(R.id.btn_daily_reminders);
        quietHoursButton = findViewById(R.id.btn_quiet_hours);
        highContrastButton = findViewById(R.id.btn_high_contrast);
        colorBlindButton = findViewById(R.id.btn_color_blind);
        leftHandedButton = findViewById(R.id.btn_left_handed);

        soundButton.setOnClickListener(v -> {
            preferencesManager.setSoundEnabled(!preferencesManager.isSoundEnabled());
            updatePreferenceLabels();
        });
        vibrationButton.setOnClickListener(v -> {
            preferencesManager.setVibrationEnabled(!preferencesManager.isVibrationEnabled());
            updatePreferenceLabels();
        });
        themeButton.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, ThemesActivity.class)));
        mistakeAlertsButton.setOnClickListener(v -> {
            preferencesManager.setMistakeAlertsEnabled(!preferencesManager.isMistakeAlertsEnabled());
            updatePreferenceLabels();
        });
        notesButton.setOnClickListener(v -> {
            preferencesManager.setDefaultNotesEnabled(!preferencesManager.isDefaultNotesEnabled());
            updatePreferenceLabels();
        });
        timerButton.setOnClickListener(v -> {
            preferencesManager.setTimerEnabled(!preferencesManager.isTimerEnabled());
            updatePreferenceLabels();
        });
        highlightConflictsButton.setOnClickListener(v -> {
            preferencesManager.setHighlightConflictsEnabled(!preferencesManager.isHighlightConflictsEnabled());
            updatePreferenceLabels();
        });
        highlightSameButton.setOnClickListener(v -> {
            preferencesManager.setHighlightSameNumberEnabled(!preferencesManager.isHighlightSameNumberEnabled());
            updatePreferenceLabels();
        });
        boardPresetButton.setOnClickListener(v -> {
            preferencesManager.setBoardVariant(nextBoardVariant(preferencesManager.getBoardVariant()));
            Toast.makeText(this, R.string.settings_board_preset_saved_message, Toast.LENGTH_SHORT).show();
            updatePreferenceLabels();
        });
        inputModeButton.setOnClickListener(v -> {
            String next = PreferencesManager.INPUT_MODE_CELL_FIRST.equals(preferencesManager.getInputMode())
                    ? PreferencesManager.INPUT_MODE_DIGIT_FIRST
                    : PreferencesManager.INPUT_MODE_CELL_FIRST;
            preferencesManager.setInputMode(next);
            updatePreferenceLabels();
        });
        hapticButton.setOnClickListener(v -> {
            preferencesManager.setHapticIntensity((preferencesManager.getHapticIntensity() + 1) % 4);
            updatePreferenceLabels();
        });
        dailyRemindersButton.setOnClickListener(v -> {
            preferencesManager.setDailyRemindersEnabled(!preferencesManager.isDailyRemindersEnabled());
            updatePreferenceLabels();
        });
        quietHoursButton.setOnClickListener(v -> {
            preferencesManager.setQuietHoursEnabled(!preferencesManager.isQuietHoursEnabled());
            updatePreferenceLabels();
        });
        highContrastButton.setOnClickListener(v -> {
            preferencesManager.setHighContrastEnabled(!preferencesManager.isHighContrastEnabled());
            updatePreferenceLabels();
        });
        colorBlindButton.setOnClickListener(v -> {
            preferencesManager.setColorBlindPaletteEnabled(!preferencesManager.isColorBlindPaletteEnabled());
            updatePreferenceLabels();
        });
        leftHandedButton.setOnClickListener(v -> {
            preferencesManager.setLeftHandedModeEnabled(!preferencesManager.isLeftHandedModeEnabled());
            updatePreferenceLabels();
        });
        findViewById(R.id.btn_premium_settings).setOnClickListener(v ->
                startActivity(new android.content.Intent(this, PremiumActivity.class)));
        findViewById(R.id.btn_privacy).setOnClickListener(v -> showPrivacyDialog());
        findViewById(R.id.btn_about).setOnClickListener(v ->
                Toast.makeText(this, R.string.settings_about_message, Toast.LENGTH_SHORT).show());

        applyThemeMode(preferencesManager.getTheme());
        updatePreferenceLabels();
    }

    private void updatePreferenceLabels() {
        soundButton.setText(getString(R.string.settings_sound_state, stateLabel(preferencesManager.isSoundEnabled())));
        soundButton.setContentDescription(getString(R.string.settings_sound_content_description));
        vibrationButton.setText(getString(R.string.settings_vibration_state, stateLabel(preferencesManager.isVibrationEnabled())));
        themeButton.setText(R.string.settings_theme);
        mistakeAlertsButton.setText(getString(R.string.settings_mistake_popups_state,
                stateLabel(preferencesManager.isMistakeAlertsEnabled())));
        notesButton.setText(getString(R.string.settings_notes_state,
                stateLabel(preferencesManager.isDefaultNotesEnabled())));
        timerButton.setText(getString(R.string.settings_timer_state,
                stateLabel(preferencesManager.isTimerEnabled())));
        highlightConflictsButton.setText(getString(R.string.settings_highlight_conflicts_state,
                stateLabel(preferencesManager.isHighlightConflictsEnabled())));
        highlightSameButton.setText(getString(R.string.settings_highlight_same_state,
                stateLabel(preferencesManager.isHighlightSameNumberEnabled())));
        boardPresetButton.setText(getString(R.string.settings_board_preset_state,
                boardVariantLabel(preferencesManager.getBoardVariant())));
        inputModeButton.setText(getString(R.string.settings_input_mode_state,
                inputModeLabel(preferencesManager.getInputMode())));
        hapticButton.setText(getString(R.string.settings_haptic_state,
                hapticLabel(preferencesManager.getHapticIntensity())));
        dailyRemindersButton.setText(getString(R.string.settings_daily_reminders_state,
                stateLabel(preferencesManager.isDailyRemindersEnabled())));
        quietHoursButton.setText(getString(R.string.settings_quiet_hours_state,
                stateLabel(preferencesManager.isQuietHoursEnabled())));
        highContrastButton.setText(getString(R.string.settings_high_contrast_state,
                stateLabel(preferencesManager.isHighContrastEnabled())));
        colorBlindButton.setText(getString(R.string.settings_color_blind_state,
                stateLabel(preferencesManager.isColorBlindPaletteEnabled())));
        leftHandedButton.setText(getString(R.string.settings_left_handed_state,
                stateLabel(preferencesManager.isLeftHandedModeEnabled())));
    }

    private void applyThemeMode(String theme) {
        if ("DARK".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("SYSTEM".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    private String stateLabel(boolean enabled) {
        return getString(enabled ? R.string.settings_state_on : R.string.settings_state_off);
    }

    private String inputModeLabel(String mode) {
        return getString(PreferencesManager.INPUT_MODE_DIGIT_FIRST.equals(mode)
                ? R.string.settings_input_digit_first
                : R.string.settings_input_cell_first);
    }

    private String hapticLabel(int intensity) {
        if (intensity <= 0) return getString(R.string.settings_haptic_off);
        if (intensity == 1) return getString(R.string.settings_haptic_light);
        if (intensity == 2) return getString(R.string.settings_haptic_medium);
        return getString(R.string.settings_haptic_strong);
    }

    private String boardVariantLabel(String variant) {
        if (PreferencesManager.BOARD_VARIANT_4X4.equals(variant)) return "4x4";
        if (PreferencesManager.BOARD_VARIANT_6X6.equals(variant)) return "6x6";
        if (PreferencesManager.BOARD_VARIANT_12X12.equals(variant)) return "12x12";
        if (PreferencesManager.BOARD_VARIANT_16X16.equals(variant)) return "16x16";
        return "9x9";
    }

    private String nextBoardVariant(String current) {
        if (PreferencesManager.BOARD_VARIANT_4X4.equals(current)) return PreferencesManager.BOARD_VARIANT_6X6;
        if (PreferencesManager.BOARD_VARIANT_6X6.equals(current)) return PreferencesManager.BOARD_VARIANT_9X9;
        if (PreferencesManager.BOARD_VARIANT_9X9.equals(current)) return PreferencesManager.BOARD_VARIANT_12X12;
        if (PreferencesManager.BOARD_VARIANT_12X12.equals(current)) return PreferencesManager.BOARD_VARIANT_16X16;
        return PreferencesManager.BOARD_VARIANT_4X4;
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_privacy_title)
                .setMessage(R.string.settings_privacy_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
