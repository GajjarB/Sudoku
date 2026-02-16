package com.example.sudoku.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeStorage {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "key_theme";

    public static void saveTheme(Context context, ThemeUtils.Theme theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, theme.name()).apply();
    }

    public static ThemeUtils.Theme loadTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String themeName = prefs.getString(KEY_THEME, ThemeUtils.Theme.BLUE.name());
        try {
            return ThemeUtils.Theme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return ThemeUtils.Theme.BLUE;
        }
    }
}
