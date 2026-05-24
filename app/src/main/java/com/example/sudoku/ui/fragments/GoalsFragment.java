package com.example.sudoku.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.R;
import com.example.sudoku.ui.activities.GameActivity;
import com.example.sudoku.ui.adapters.GoalCardAdapter;
import com.example.sudoku.ui.viewmodels.GoalsViewModel;
import com.example.sudoku.utils.Constants;

public class GoalsFragment extends Fragment {

    private GoalsViewModel viewModel;

    public GoalsFragment() {
        super(R.layout.fragment_goals);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GoalsViewModel.class);

        TextView streak = view.findViewById(R.id.tv_goals_streak);
        TextView streakMessage = view.findViewById(R.id.tv_goals_streak_message);
        TextView chestMessage = view.findViewById(R.id.tv_reward_chest);
        Button primaryAction = view.findViewById(R.id.btn_goal_primary_action);

        GoalCardAdapter dailyAdapter = new GoalCardAdapter(this::launchGoal);
        GoalCardAdapter weeklyAdapter = new GoalCardAdapter(this::launchGoal);
        ((RecyclerView) view.findViewById(R.id.recycler_daily_goals)).setAdapter(dailyAdapter);
        ((RecyclerView) view.findViewById(R.id.recycler_weekly_goals)).setAdapter(weeklyAdapter);

        primaryAction.setOnClickListener(v -> launchPuzzle(true, 1));

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                streak.setText("🔥 " + profile.currentStreak + " Day Streak");
            }
        });
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            streakMessage.setText(state.streakMessage);
            chestMessage.setText(state.chestMessage);
            primaryAction.setText(state.chestReady ? "Claim reward" : "Play a puzzle");
            dailyAdapter.submitList(state.dailyGoals);
            weeklyAdapter.submitList(state.weeklyGoals);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void launchGoal(GoalsViewModel.GoalCardItem item) {
        launchPuzzle(item.dailyChallenge, item.suggestedLevel);
    }

    private void launchPuzzle(boolean dailyChallenge, int level) {
        Intent intent = new Intent(requireContext(), GameActivity.class);
        if (dailyChallenge) {
            intent.putExtra(Constants.EXTRA_IS_DAILY_CHALLENGE, true);
        } else {
            intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, Math.max(1, level));
        }
        startActivity(intent);
    }
}
