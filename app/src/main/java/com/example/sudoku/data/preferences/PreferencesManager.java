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
    private static final String KEY_MISTAKE_ALERTS_ENABLED = "mistake_alerts_enabled";
    private static final String KEY_DEFAULT_NOTES_ENABLED = "default_notes_enabled";
    private static final String KEY_TIMER_ENABLED = "timer_enabled";
    private static final String KEY_HIGHLIGHT_CONFLICTS_ENABLED = "highlight_conflicts_enabled";
    private static final String KEY_HIGHLIGHT_SAME_NUMBER_ENABLED = "highlight_same_number_enabled";
    private static final String KEY_AUTO_CHECK_ENABLED = "auto_check_enabled";
    private static final String KEY_AUTO_CANDIDATE_ENABLED = "auto_candidate_enabled";
    private static final String KEY_INPUT_MODE = "input_mode";
    private static final String KEY_HAPTIC_INTENSITY = "haptic_intensity";
    private static final String KEY_FONT_SCALE = "font_scale";
    private static final String KEY_COLOR_BLIND_PALETTE = "color_blind_palette";
    private static final String KEY_HIGH_CONTRAST_ENABLED = "high_contrast_enabled";
    private static final String KEY_LEFT_HANDED_MODE_ENABLED = "left_handed_mode_enabled";
    private static final String KEY_DAILY_REMINDERS_ENABLED = "daily_reminders_enabled";
    private static final String KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled";
    private static final String KEY_BOARD_VARIANT = "board_variant";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_FIRST_RUN = "is_first_run";
    private static final String KEY_USER_ID = "user_id";

    public static final String INPUT_MODE_CELL_FIRST = "CELL_FIRST";
    public static final String INPUT_MODE_DIGIT_FIRST = "DIGIT_FIRST";

    public static final String BOARD_VARIANT_4X4 = "CLASSIC_4X4";
    public static final String BOARD_VARIANT_6X6 = "CLASSIC_6X6";
    public static final String BOARD_VARIANT_9X9 = "CLASSIC_9X9";
    public static final String BOARD_VARIANT_12X12 = "CLASSIC_12X12";
    public static final String BOARD_VARIANT_16X16 = "CLASSIC_16X16";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get or generate a unique user ID.
     * Generated on first launch, persisted in SharedPreferences.
     */
    public String getUserId() {
        String userId = prefs.getString(KEY_USER_ID, null);
        if (userId == null) {
            userId = java.util.UUID.randomUUID().toString();
            prefs.edit().putString(KEY_USER_ID, userId).apply();
        }
        return userId;
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

    public boolean isMistakeAlertsEnabled() {
        return prefs.getBoolean(KEY_MISTAKE_ALERTS_ENABLED, true);
    }

    public void setMistakeAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MISTAKE_ALERTS_ENABLED, enabled).apply();
    }

    public boolean isDefaultNotesEnabled() {
        return prefs.getBoolean(KEY_DEFAULT_NOTES_ENABLED, false);
    }

    public void setDefaultNotesEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DEFAULT_NOTES_ENABLED, enabled).apply();
    }

    public boolean isTimerEnabled() {
        return prefs.getBoolean(KEY_TIMER_ENABLED, true);
    }

    public void setTimerEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TIMER_ENABLED, enabled).apply();
    }

    public boolean isHighlightConflictsEnabled() {
        return prefs.getBoolean(KEY_HIGHLIGHT_CONFLICTS_ENABLED, true);
    }

    public void setHighlightConflictsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIGHLIGHT_CONFLICTS_ENABLED, enabled).apply();
    }

    public boolean isHighlightSameNumberEnabled() {
        return prefs.getBoolean(KEY_HIGHLIGHT_SAME_NUMBER_ENABLED, true);
    }

    public void setHighlightSameNumberEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIGHLIGHT_SAME_NUMBER_ENABLED, enabled).apply();
    }

    public boolean isAutoCheckEnabled() {
        return prefs.getBoolean(KEY_AUTO_CHECK_ENABLED, true);
    }

    public void setAutoCheckEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CHECK_ENABLED, enabled).apply();
    }

    public boolean isAutoCandidateEnabled() {
        return prefs.getBoolean(KEY_AUTO_CANDIDATE_ENABLED, true);
    }

    public void setAutoCandidateEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CANDIDATE_ENABLED, enabled).apply();
    }

    public String getInputMode() {
        return prefs.getString(KEY_INPUT_MODE, INPUT_MODE_CELL_FIRST);
    }

    public void setInputMode(String inputMode) {
        prefs.edit().putString(KEY_INPUT_MODE, inputMode).apply();
    }

    public int getHapticIntensity() {
        return prefs.getInt(KEY_HAPTIC_INTENSITY, 2);
    }

    public void setHapticIntensity(int intensity) {
        int clamped = Math.max(0, Math.min(3, intensity));
        prefs.edit().putInt(KEY_HAPTIC_INTENSITY, clamped).apply();
    }

    public float getFontScale() {
        return prefs.getFloat(KEY_FONT_SCALE, 1.0f);
    }

    public void setFontScale(float scale) {
        float clamped = Math.max(0.9f, Math.min(1.2f, scale));
        prefs.edit().putFloat(KEY_FONT_SCALE, clamped).apply();
    }

    public boolean isColorBlindPaletteEnabled() {
        return prefs.getBoolean(KEY_COLOR_BLIND_PALETTE, false);
    }

    public void setColorBlindPaletteEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_COLOR_BLIND_PALETTE, enabled).apply();
    }

    public boolean isHighContrastEnabled() {
        return prefs.getBoolean(KEY_HIGH_CONTRAST_ENABLED, false);
    }

    public void setHighContrastEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST_ENABLED, enabled).apply();
    }

    public boolean isLeftHandedModeEnabled() {
        return prefs.getBoolean(KEY_LEFT_HANDED_MODE_ENABLED, false);
    }

    public void setLeftHandedModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LEFT_HANDED_MODE_ENABLED, enabled).apply();
    }

    public boolean isDailyRemindersEnabled() {
        return prefs.getBoolean(KEY_DAILY_REMINDERS_ENABLED, true);
    }

    public void setDailyRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDERS_ENABLED, enabled).apply();
    }

    public boolean isQuietHoursEnabled() {
        return prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false);
    }

    public void setQuietHoursEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply();
    }

    public String getBoardVariant() {
        return BOARD_VARIANT_9X9;
    }

    public void setBoardVariant(String variant) {
        prefs.edit().putString(KEY_BOARD_VARIANT, BOARD_VARIANT_9X9).apply();
    }

    public String getTheme() { // "LIGHT", "DARK", "SYSTEM"
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
