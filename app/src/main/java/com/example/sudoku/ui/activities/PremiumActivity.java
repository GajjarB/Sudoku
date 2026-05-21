package com.example.sudoku.ui.activities;

import com.example.sudoku.ui.adapters.StaticListAdapter;
import java.util.Arrays;
import java.util.List;

public class PremiumActivity extends StaticScreenActivity {
    @Override protected String label() { return "Optional"; }
    @Override protected String title() { return "Premium"; }
    @Override protected String subtitle() { return "Core gameplay stays free. Premium adds comfort features."; }
    @Override protected List<StaticListAdapter.Row> rows() {
        return Arrays.asList(
                new StaticListAdapter.Row("+", "Unlimited hints", "More guidance for training sessions.", ""),
                new StaticListAdapter.Row("A", "Advanced stats", "Trends, accuracy, and difficulty analysis.", ""),
                new StaticListAdapter.Row("T", "Exclusive themes", "More calm color systems.", ""),
                new StaticListAdapter.Row("0", "No ads", "Distraction-free play.", ""),
                new StaticListAdapter.Row("P", "Puzzle packs", "Curated boards by skill.", ""),
                new StaticListAdapter.Row("S", "Streak protection", "Grace days for busy weeks.", ""));
    }
}
