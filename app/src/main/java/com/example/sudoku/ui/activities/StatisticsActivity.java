package com.example.sudoku.ui.activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import com.example.sudoku.R;
import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.data.preferences.PreferencesManager;
import com.example.sudoku.ui.viewmodels.HomeViewModel;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends BaseActivity {

    private String profileSummary = "";
    private String historySummary = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStoredThemeMode();
        setContentView(R.layout.activity_statistics);

        TextView completed = findViewById(R.id.tv_stat_completed);
        TextView winRate = findViewById(R.id.tv_stat_win_rate);
        TextView currentStreak = findViewById(R.id.tv_stat_current_streak);
        TextView longestStreak = findViewById(R.id.tv_stat_longest_streak);
        TextView mistakes = findViewById(R.id.tv_stat_mistakes);
        TextView bestTimes = findViewById(R.id.tv_stat_best_times);
        ProgressBar mistakesProgress = findViewById(R.id.progress_mistakes);

        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.getUserProfile().observe(this, profile -> {
            if (profile == null) return;

            int played = profile.totalGamesPlayed;
            int wins = profile.totalGamesWon;
            int losses = profile.totalGamesLost;
            int rate = played == 0 ? 0 : Math.round((wins * 100f) / played);
            int mistakePercent = played == 0 ? 0 : Math.min(100, Math.round((losses * 100f) / played));

            completed.setText(wins + "\n" + getString(R.string.stats_solved_label));
            winRate.setText(rate + "%\n" + getString(R.string.stats_win_rate_label));
            currentStreak.setText(profile.currentStreak + "\n" + getString(R.string.stats_current_streak_label));
            longestStreak.setText(profile.longestStreak + "\n" + getString(R.string.stats_best_streak_label));
            mistakesProgress.setProgress(mistakePercent);
            mistakes.setText(played == 0
                    ? getString(R.string.stats_mistake_empty)
                    : getString(R.string.stats_mistake_summary, losses, played, mistakePercent));
            profileSummary = played == 0
                    ? getString(R.string.stats_no_play_time)
                    : getString(R.string.stats_profile_summary,
                    formatTime(profile.totalPlayTimeMs),
                    profile.totalStarsEarned,
                    profile.totalHintsUsed,
                    formatDecimal(played == 0 ? 0 : (double) profile.totalHintsUsed / played));
            updateSummary(bestTimes);
        });
        loadHistoryStats(bestTimes);
    }

    private void loadHistoryStats(TextView bestTimes) {
        AppExecutors.getInstance().io().execute(() -> {
            SudokuDatabase db = SudokuDatabase.getInstance(getApplicationContext());
            List<GameHistory> recentGames = db.gameDao().getRecentGames(25);
            int dailyChallenges = db.gameDao().getTotalDailyChallengesCompleted();
            long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L;
            double recentAverageMs = db.gameDao().getAverageCompletionTime(thirtyDaysAgo);

            int perfectGames = 0;
            int totalMistakes = 0;
            long bestTimeMs = Long.MAX_VALUE;
            long totalRecentTimeMs = 0;
            int timedGames = 0;
            for (GameHistory game : recentGames) {
                if (game.isPerfectGame) perfectGames++;
                totalMistakes += game.mistakesMade;
                if (game.starsEarned > 0 && game.totalTimeMs > 0) {
                    bestTimeMs = Math.min(bestTimeMs, game.totalTimeMs);
                    totalRecentTimeMs += game.totalTimeMs;
                    timedGames++;
                }
            }

            StringBuilder history = new StringBuilder();
            if (!recentGames.isEmpty()) {
                history.append("\n\nRecent games: ").append(recentGames.size());
                history.append("\nBest recent solve: ")
                        .append(bestTimeMs == Long.MAX_VALUE ? "--" : formatTime(bestTimeMs));
                history.append("\nAverage recent solve: ")
                        .append(timedGames == 0 ? "--" : formatTime(totalRecentTimeMs / timedGames));
                history.append("\n30-day average: ")
                        .append(recentAverageMs <= 0 ? "--" : formatTime((long) recentAverageMs));
                history.append("\nPerfect games: ").append(perfectGames);
                history.append("\nRecent mistakes: ").append(totalMistakes);
                history.append("\nDaily challenges: ").append(dailyChallenges);
            }

            runOnUiThread(() -> {
                historySummary = history.toString();
                updateSummary(bestTimes);
            });
        });
    }

    private void updateSummary(TextView bestTimes) {
        bestTimes.setText(profileSummary + historySummary);
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "--";
        long minutes = millis / 60000;
        long hours = minutes / 60;
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m";
    }

    private String formatDecimal(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private void applyStoredThemeMode() {
        String theme = new PreferencesManager(this).getTheme();
        if ("DARK".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("SYSTEM".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
