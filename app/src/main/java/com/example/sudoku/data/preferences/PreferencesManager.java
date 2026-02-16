package com.example.sudoku.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages SharedPreferences for simple app settings.
 */
public class PreferencesManager {

    private static final String PREF_NAME = "sudoku_prefs";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_FIRST_RUN = "is_first_run";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    public String getTheme() { // "LIGHT", "DARK", "AMOLED"
        return prefs.getString(KEY_THEME, "LIGHT");
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRun(boolean isFirstRun) {
        prefs.edit().putBoolean(KEY_FIRST_RUN, isFirstRun).apply();
    }
}
