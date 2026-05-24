package com.example.sudoku.ui.activities;

import com.example.sudoku.ui.adapters.StaticListAdapter;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends StaticScreenActivity {
    @Override protected String label() { return "Hero profile"; }
    @Override protected String title() { return "Puzzle Champion"; }
    @Override protected String subtitle() { return "Level 1 and climbing. Keep solving to unlock shiny badges."; }
    @Override protected List<StaticListAdapter.Row> rows() {
        return Arrays.asList(
                new StaticListAdapter.Row("XP", "Level Progress", "Earn XP from solves, streaks, and challenges.", "0%"),
                new StaticListAdapter.Row("1", "First Win", "Complete your first puzzle.", "Locked"),
                new StaticListAdapter.Row("10", "10 Wins", "Solve ten boards.", "Locked"),
                new StaticListAdapter.Row("P", "Perfectionist", "Win without mistakes.", "Locked"),
                new StaticListAdapter.Row("N", "No Hint Hero", "Complete a board without hints.", "Locked"),
                new StaticListAdapter.Row("$", "Premium", "Themes, stats, puzzle packs, no ads.", "Optional"));
    }
}
