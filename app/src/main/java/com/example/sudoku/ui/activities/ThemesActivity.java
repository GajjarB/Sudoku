package com.example.sudoku.ui.activities;

import com.example.sudoku.ui.adapters.StaticListAdapter;
import java.util.Arrays;
import java.util.List;

public class ThemesActivity extends StaticScreenActivity {
    @Override protected String label() { return "Color playground"; }
    @Override protected String title() { return "Themes"; }
    @Override protected String subtitle() { return "Make your board feel playful, calm, or bold in one tap."; }
    @Override protected List<StaticListAdapter.Row> rows() {
        return Arrays.asList(
                new StaticListAdapter.Row("L", "Light", "Clean default theme.", "Selected"),
                new StaticListAdapter.Row("D", "Dark", "Low-light contrast.", ""),
                new StaticListAdapter.Row("N", "Notion", "Warm paper surfaces.", ""),
                new StaticListAdapter.Row("F", "Forest", "Muted greens.", ""),
                new StaticListAdapter.Row("O", "Ocean", "Soft blue palette.", ""),
                new StaticListAdapter.Row("M", "Midnight", "Quiet dark focus.", ""));
    }
}
