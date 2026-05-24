package com.example.sudoku.ui.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.sudoku.R;
import com.example.sudoku.data.preferences.PreferencesManager;

public class SettingsActivity extends BaseActivity {

    private static final String UI_PREFS = "sudoku_ui_settings";
    private static final String KEY_DEFAULT_NOTES = "default_notes_enabled";
    private static final String KEY_MISTAKE_ALERTS = "mistake_alerts_enabled";

    private PreferencesManager preferencesManager;
    private SharedPreferences uiPrefs;
    private Button soundButton;
    private Button vibrationButton;
    private Button themeButton;
    private Button mistakeAlertsButton;
    private Button notesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferencesManager = new PreferencesManager(this);
        uiPrefs = getSharedPreferences(UI_PREFS, MODE_PRIVATE);
        soundButton = findViewById(R.id.btn_sound);
        vibrationButton = findViewById(R.id.btn_vibration);
        themeButton = findViewById(R.id.btn_theme);
        mistakeAlertsButton = findViewById(R.id.btn_mistake_limit);
        notesButton = findViewById(R.id.btn_notes_help);

        soundButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.settings_sound_unavailable_message, Toast.LENGTH_SHORT).show());
        vibrationButton.setOnClickListener(v -> {
            preferencesManager.setVibrationEnabled(!preferencesManager.isVibrationEnabled());
            updatePreferenceLabels();
        });
        themeButton.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, ThemesActivity.class)));
        mistakeAlertsButton.setOnClickListener(v -> {
            boolean enabled = !uiPrefs.getBoolean(KEY_MISTAKE_ALERTS, true);
            uiPrefs.edit().putBoolean(KEY_MISTAKE_ALERTS, enabled).apply();
            updatePreferenceLabels();
        });
        notesButton.setOnClickListener(v -> {
            boolean enabled = !uiPrefs.getBoolean(KEY_DEFAULT_NOTES, false);
            uiPrefs.edit().putBoolean(KEY_DEFAULT_NOTES, enabled).apply();
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
        soundButton.setText(R.string.settings_sound_unavailable);
        soundButton.setContentDescription(getString(R.string.settings_audio_unavailable_content_description));
        vibrationButton.setText(getString(R.string.settings_vibration_state, stateLabel(preferencesManager.isVibrationEnabled())));
        themeButton.setText(R.string.settings_theme);
        mistakeAlertsButton.setText(getString(R.string.settings_mistake_popups_state,
                stateLabel(uiPrefs.getBoolean(KEY_MISTAKE_ALERTS, true))));
        notesButton.setText(getString(R.string.settings_notes_state,
                stateLabel(uiPrefs.getBoolean(KEY_DEFAULT_NOTES, false))));
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

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_privacy_title)
                .setMessage(R.string.settings_privacy_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
