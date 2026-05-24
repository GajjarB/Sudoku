package com.example.sudoku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sudoku.R;
import com.example.sudoku.ui.adapters.LevelGridAdapter;
import com.example.sudoku.ui.viewmodels.LevelSelectViewModel;
import com.example.sudoku.utils.Constants;

/**
 * Activity for selecting levels.
 * Uses ViewModel for proper MVVM architecture.
 */
public class LevelSelectActivity extends BaseActivity {

    private LevelSelectViewModel viewModel;
    private LevelGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        viewModel = new ViewModelProvider(this).get(LevelSelectViewModel.class);

        String difficulty = getIntent().getStringExtra(Constants.EXTRA_DIFFICULTY);
        TextView label = findViewById(R.id.tv_level_label);
        TextView title = findViewById(R.id.tv_level_title);
        TextView subtitle = findViewById(R.id.tv_level_subtitle);
        View difficultyOptions = findViewById(R.id.difficulty_options);
        View filters = findViewById(R.id.puzzle_filters);

        if (difficulty == null) {
            label.setText(R.string.levels_choose_label);
            title.setText(R.string.levels_choose_title);
            subtitle.setText(R.string.levels_choose_subtitle);
            bindDifficultyButtons();
            return;
        }

        label.setText(R.string.levels_list_label);
        title.setText(difficulty);
        subtitle.setText(R.string.levels_list_subtitle);
        difficultyOptions.setVisibility(View.GONE);
        filters.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = findViewById(R.id.recycler_levels);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LevelGridAdapter();
        adapter.setOnLevelClickListener(level -> openLevel(level));
        recyclerView.setAdapter(adapter);

        viewModel.getUnlockedLevels().observe(this, levels -> {
            if (levels != null) {
                adapter.setLevels(levels);
            }
        });
    }

    private void bindDifficultyButtons() {
        findViewById(R.id.btn_easy).setOnClickListener(v -> openDifficulty("Easy"));
        findViewById(R.id.btn_medium).setOnClickListener(v -> openDifficulty("Medium"));
        findViewById(R.id.btn_hard).setOnClickListener(v -> openDifficulty("Hard"));
        findViewById(R.id.btn_expert).setOnClickListener(v -> openDifficulty("Expert"));
    }

    private void openDifficulty(String difficulty) {
        Intent intent = new Intent(this, LevelSelectActivity.class);
        intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }

    private void openLevel(int level) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, level);
        startActivity(intent);
    }
}
