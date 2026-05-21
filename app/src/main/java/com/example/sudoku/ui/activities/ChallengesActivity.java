package com.example.sudoku.ui.activities;

import com.example.sudoku.ui.adapters.StaticListAdapter;
import java.util.Arrays;
import java.util.List;

public class ChallengesActivity extends StaticScreenActivity {
    @Override protected String label() { return "Daily, weekly, monthly"; }
    @Override protected String title() { return "Challenges"; }
    @Override protected String subtitle() { return "Small goals for steady practice."; }
    @Override protected List<StaticListAdapter.Row> rows() {
        return Arrays.asList(
                new StaticListAdapter.Row("D", "Daily Puzzle", "Solve today's board.", "Play"),
                new StaticListAdapter.Row("W", "Weekly Challenge", "Complete five puzzles this week.", "0/5"),
                new StaticListAdapter.Row("M", "Monthly Challenge", "Build a long-form streak.", "May"),
                new StaticListAdapter.Row("!", "No Mistake Challenge", "Win with perfect accuracy.", "New"),
                new StaticListAdapter.Row("T", "Speed Challenge", "Beat your average time.", "Ready"),
                new StaticListAdapter.Row("S", "Streak Challenge", "Return daily to keep momentum.", "0 days"));
    }
}
