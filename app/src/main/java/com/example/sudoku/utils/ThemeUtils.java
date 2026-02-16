package com.example.sudoku.utils;

import android.graphics.Color;
import com.example.sudoku.R;

public class ThemeUtils {
    
    public enum Theme {
        BLUE("Blue", "#4A90E2", "#FFFFFF", "#F5F7FA", "#1A1A1A", "#E8ECF0"),
        DARK("Dark", "#64B5F6", "#000000", "#1A1A1A", "#FFFFFF", "#333333"),
        PINK("Pink", "#EC407A", "#FFFFFF", "#FCE4EC", "#1A1A1A", "#E8ECF0"),
        GREEN("Green", "#66BB6A", "#FFFFFF", "#E8F5E9", "#1A1A1A", "#E8ECF0"),
        PURPLE("Purple", "#AB47BC", "#FFFFFF", "#F3E5F5", "#1A1A1A", "#E8ECF0"),
        ORANGE("Orange", "#FF7043", "#FFFFFF", "#FBE9E7", "#1A1A1A", "#E8ECF0");

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
