package com.example.sudoku.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sudoku.R;
import com.example.sudoku.ui.viewmodels.StatsViewModel;
import com.example.sudoku.ui.views.ProgressChartView;

import java.util.Locale;

public class StatsFragment extends Fragment {

    private StatsViewModel viewModel;
    private Button weekButton;
    private Button monthButton;
    private Button allButton;

    public StatsFragment() {
        super(R.layout.fragment_stats);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        weekButton = view.findViewById(R.id.btn_filter_week);
        monthButton = view.findViewById(R.id.btn_filter_month);
        allButton = view.findViewById(R.id.btn_filter_all);
        weekButton.setOnClickListener(v -> viewModel.setFilter(StatsViewModel.Filter.WEEK));
        monthButton.setOnClickListener(v -> viewModel.setFilter(StatsViewModel.Filter.MONTH));
        allButton.setOnClickListener(v -> viewModel.setFilter(StatsViewModel.Filter.ALL));

        TextView solved = view.findViewById(R.id.tv_stats_solved);
        TextView streak = view.findViewById(R.id.tv_stats_streak);
        TextView accuracy = view.findViewById(R.id.tv_stats_accuracy);
        TextView bestTime = view.findViewById(R.id.tv_stats_best_time);
        TextView secondary = view.findViewById(R.id.tv_stats_secondary);
        ProgressChartView chartView = view.findViewById(R.id.progress_chart);
        TextView easyCount = view.findViewById(R.id.tv_easy_count);
        TextView mediumCount = view.findViewById(R.id.tv_medium_count);
        TextView hardCount = view.findViewById(R.id.tv_hard_count);
        TextView expertCount = view.findViewById(R.id.tv_expert_count);
        ProgressBar easyProgress = view.findViewById(R.id.progress_easy);
        ProgressBar mediumProgress = view.findViewById(R.id.progress_medium);
        ProgressBar hardProgress = view.findViewById(R.id.progress_hard);
        ProgressBar expertProgress = view.findViewById(R.id.progress_expert);
        TextView insightTitle = view.findViewById(R.id.tv_insight_title);
        TextView insightBody = view.findViewById(R.id.tv_insight_body);

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            setSelectedFilter(state.filter);
            solved.setText(String.valueOf(state.solved));
            streak.setText(state.streak + " days");
            accuracy.setText(state.accuracy + "%");
            bestTime.setText(formatTime(state.bestTimeMs));
            secondary.setText("Average time " + formatTime(state.averageTimeMs) + " • XP " + state.xpEarned);
            chartView.setData(state.chartLabels, state.chartValues);
            easyCount.setText(state.easyCount + " solved");
            mediumCount.setText(state.mediumCount + " solved");
            hardCount.setText(state.hardCount + " solved");
            expertCount.setText(state.expertCount + " solved");
            easyProgress.setProgress(state.easyProgress);
            mediumProgress.setProgress(state.mediumProgress);
            hardProgress.setProgress(state.hardProgress);
            expertProgress.setProgress(state.expertProgress);
            insightTitle.setText(state.insightTitle);
            insightBody.setText(state.insightBody);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void setSelectedFilter(StatsViewModel.Filter filter) {
        styleButton(weekButton, filter == StatsViewModel.Filter.WEEK);
        styleButton(monthButton, filter == StatsViewModel.Filter.MONTH);
        styleButton(allButton, filter == StatsViewModel.Filter.ALL);
    }

    private void styleButton(Button button, boolean selected) {
        button.setBackgroundResource(selected ? R.drawable.bg_bottom_nav_active : R.drawable.bg_surface_card);
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "--";
        long seconds = millis / 1000;
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }
}
