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
    private Button hapticButton;

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
        hapticButton = findViewById(R.id.btn_haptic_intensity);

        soundButton.setOnClickListener(v -> {
            preferencesManager.setSoundEnabled(!preferencesManager.isSoundEnabled());
            updatePreferenceLabels();
        });
        vibrationButton.setOnClickListener(v -> {
            preferencesManager.setVibrationEnabled(!preferencesManager.isVibrationEnabled());
            updatePreferenceLabels();
        });
        themeButton.setOnClickListener(v -> {
            preferencesManager.setTheme(nextTheme(preferencesManager.getTheme()));
            applyThemeMode(preferencesManager.getTheme());
            updatePreferenceLabels();
        });
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
        hapticButton.setOnClickListener(v -> {
            preferencesManager.setHapticIntensity((preferencesManager.getHapticIntensity() + 1) % 4);
            updatePreferenceLabels();
        });
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
        themeButton.setText(getString(R.string.settings_theme_state, themeLabel(preferencesManager.getTheme())));
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
        hapticButton.setText(getString(R.string.settings_haptic_state,
                hapticLabel(preferencesManager.getHapticIntensity())));
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

    private String hapticLabel(int intensity) {
        if (intensity <= 0) return getString(R.string.settings_haptic_off);
        if (intensity == 1) return getString(R.string.settings_haptic_light);
        if (intensity == 2) return getString(R.string.settings_haptic_medium);
        return getString(R.string.settings_haptic_strong);
    }

    private String nextTheme(String current) {
        if ("LIGHT".equals(current)) return "DARK";
        if ("DARK".equals(current)) return "SYSTEM";
        return "LIGHT";
    }

    private String themeLabel(String current) {
        if ("DARK".equals(current)) return getString(R.string.theme_dark);
        if ("SYSTEM".equals(current)) return getString(R.string.settings_theme_system);
        return getString(R.string.settings_theme_light);
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_privacy_title)
                .setMessage(R.string.settings_privacy_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
