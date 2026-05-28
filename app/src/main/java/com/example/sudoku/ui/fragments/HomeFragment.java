package com.example.sudoku.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.R;
import com.example.sudoku.data.local.entities.LevelProgress;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.ui.activities.GameActivity;
import com.example.sudoku.ui.activities.LevelSelectActivity;
import com.example.sudoku.ui.activities.SettingsActivity;
import com.example.sudoku.ui.adapters.LevelPathAdapter;
import com.example.sudoku.ui.utils.UiEffects;
import com.example.sudoku.ui.viewmodels.HomeViewModel;
import com.example.sudoku.utils.Constants;

import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private LevelPathAdapter adapter;
    private SavedGame activeGame;
    private UserProfile currentProfile;
    private TextView greeting;
    private TextView subtitle;
    private TextView streakChip;
    private TextView xpChip;
    private TextView dailyDifficulty;
    private TextView dailyStatus;
    private TextView continueTitle;
    private TextView continueMeta;
    private TextView unitTitle;
    private LinearLayout continueCard;
    private Button dailyAction;
    private Button continueAction;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        adapter = new LevelPathAdapter(this::openLevel);

        greeting = view.findViewById(R.id.tv_home_greeting);
        subtitle = view.findViewById(R.id.tv_home_subtitle);
        streakChip = view.findViewById(R.id.tv_home_streak_chip);
        xpChip = view.findViewById(R.id.tv_home_xp_chip);
        dailyDifficulty = view.findViewById(R.id.tv_daily_difficulty);
        dailyStatus = view.findViewById(R.id.tv_daily_status);
        continueTitle = view.findViewById(R.id.tv_continue_title);
        continueMeta = view.findViewById(R.id.tv_continue_meta);
        unitTitle = view.findViewById(R.id.tv_unit_title);
        continueCard = view.findViewById(R.id.card_continue_game);
        dailyAction = view.findViewById(R.id.btn_daily_action);
        continueAction = view.findViewById(R.id.btn_continue_action);

        RecyclerView path = view.findViewById(R.id.recycler_level_path);
        path.setAdapter(adapter);

        view.findViewById(R.id.btn_home_settings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));
        dailyAction.setOnClickListener(v -> openDailyChallenge());
        continueAction.setOnClickListener(v -> openContinueOrNext());
        UiEffects.applyDuolingoPressEffect(
                view.findViewById(R.id.btn_home_settings),
                dailyAction,
                continueAction
        );

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            currentProfile = profile;
            if (profile == null) return;
            greeting.setText(resolveGreeting());
            subtitle.setText(profile.currentStreak > 0
                    ? "Keep your " + profile.currentStreak + " day streak rolling with one smart puzzle."
                    : getString(R.string.home_greeting_subtitle));
            streakChip.setText("🔥 " + profile.currentStreak + " Day Streak");
            xpChip.setText("⭐ " + profile.totalXP + " XP");
            unitTitle.setText(resolveUnitTitle(profile.currentLevel));
            adapter.setActiveLevel(resolveActiveLevel());
            viewModel.refreshLevelPath();
        });

        viewModel.getIncompleteGame().observe(getViewLifecycleOwner(), game -> {
            activeGame = game;
            updateDailyCard();
            updateContinueCard();
            adapter.setActiveLevel(resolveActiveLevel());
        });

        viewModel.getLevelPath().observe(getViewLifecycleOwner(), adapter::submitList);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshLevelPath();
        }
    }

    private void updateDailyCard() {
        if (activeGame != null && activeGame.isDailyChallenge) {
            dailyDifficulty.setText(difficultyLabel(activeGame.levelNumber) + " • In progress");
            dailyStatus.setText("Resume today’s challenge and lock in your streak bonus.");
            dailyAction.setText("Continue");
        } else {
            dailyDifficulty.setText("Medium puzzle • +50 XP");
            dailyStatus.setText("Complete today’s challenge before midnight to earn bonus XP.");
            dailyAction.setText("Start");
        }
    }

    private void updateContinueCard() {
        boolean showContinue = activeGame != null && !activeGame.isDailyChallenge;
        continueCard.setVisibility(View.VISIBLE);
        if (!showContinue) {
            continueTitle.setText(R.string.home_continue_title_empty);
            continueMeta.setText(R.string.home_continue_meta_empty);
            continueAction.setText(R.string.home_continue_action_start);
            return;
        }
        continueTitle.setText(difficultyLabel(activeGame.levelNumber) + " Puzzle #" + activeGame.levelNumber);
        continueMeta.setText("Progress " + progressPercent(activeGame.currentGrid) + "% • Time " + formatElapsed(activeGame.elapsedTimeMs));
        continueAction.setText("Continue");
    }

    private void openDailyChallenge() {
        Intent intent = new Intent(requireContext(), GameActivity.class);
        if (activeGame != null && activeGame.isDailyChallenge) {
            intent.putExtra(Constants.EXTRA_GAME_ID, activeGame.gameId);
        } else {
            intent.putExtra(Constants.EXTRA_IS_DAILY_CHALLENGE, true);
        }
        startActivity(intent);
    }

    private void openContinueOrNext() {
        if (activeGame != null && !activeGame.isDailyChallenge) {
            Intent intent = new Intent(requireContext(), GameActivity.class);
            intent.putExtra(Constants.EXTRA_GAME_ID, activeGame.gameId);
            startActivity(intent);
            return;
        }
        startActivity(new Intent(requireContext(), LevelSelectActivity.class));
    }

    private void openLevel(LevelProgress level) {
        Intent intent = new Intent(requireContext(), GameActivity.class);
        intent.putExtra(Constants.EXTRA_LEVEL_NUMBER, level.levelNumber);
        startActivity(intent);
    }

    private int resolveActiveLevel() {
        if (activeGame != null) {
            return activeGame.levelNumber;
        }
        return currentProfile != null ? Math.max(1, currentProfile.currentLevel) : 1;
    }

    private String resolveGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good morning";
        if (hour < 18) return "Good afternoon";
        return "Good evening";
    }

    private String resolveUnitTitle(int level) {
        if (level <= 5) return getString(R.string.home_band_easy);
        if (level <= 20) return getString(R.string.home_band_medium);
        if (level <= 50) return getString(R.string.home_band_hard);
        return getString(R.string.home_band_expert);
    }

    private String difficultyLabel(int level) {
        if (level <= 5) return "Easy";
        if (level <= 20) return "Medium";
        if (level <= 50) return "Hard";
        return "Expert";
    }

    private int progressPercent(String grid) {
        if (grid == null || grid.isEmpty()) return 0;
        int filled = 0;
        for (int i = 0; i < grid.length(); i++) {
            char value = grid.charAt(i);
            if (value >= '1' && value <= '9') filled++;
        }
        return Math.round((filled * 100f) / 81f);
    }

    private String formatElapsed(long millis) {
        long totalSeconds = millis / 1000;
        return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
