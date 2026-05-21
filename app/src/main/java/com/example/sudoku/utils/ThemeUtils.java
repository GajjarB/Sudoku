package com.example.sudoku.utils;

import android.content.Context;
import android.graphics.Color;

public class ThemeUtils {

    public static Theme getDefaultTheme(Context context) {
        return Theme.LIGHT;
    }

    public enum Theme {
        LIGHT("Light", "#2F7BFF", "#F8F7F4", "#FFFFFF", "#111111", "#E7E5E0"),
        DARK("Dark", "#2F7BFF", "#111111", "#1B1B1B", "#FFFFFF", "#333333"),
        NOTION("Notion", "#2F7BFF", "#F8F7F4", "#FFFFFF", "#111111", "#E7E5E0"),
        FOREST("Forest", "#3D7C59", "#F5F8F4", "#FFFFFF", "#111111", "#DDE7DE"),
        OCEAN("Ocean", "#2F7BFF", "#F4F8FB", "#FFFFFF", "#111111", "#DCE8F2"),
        MIDNIGHT("Midnight", "#8AB4FF", "#101214", "#191C20", "#F7F7F7", "#30343A");

        public final String name;
        public final int accentColor;
        public final int backgroundColor;
        public final int secondaryColor;
        public final int primaryTextColor;
        public final int gridLineColor;

        Theme(String name, String accent, String background, String secondary, String text, String line) {
            this.name = name;
            this.accentColor = Color.parseColor(accent);
            this.backgroundColor = Color.parseColor(background);
            this.secondaryColor = Color.parseColor(secondary);
            this.primaryTextColor = Color.parseColor(text);
            this.gridLineColor = Color.parseColor(line);
        }
    }
}
