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
        String name = prefs.getString(KEY_THEME, ThemeUtils.Theme.LIGHT.name());
        try {
            return ThemeUtils.Theme.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return ThemeUtils.Theme.LIGHT;
        }
    }
}
