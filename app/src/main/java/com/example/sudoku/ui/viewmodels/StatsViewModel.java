package com.example.sudoku.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.data.repository.UserRepository;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsViewModel extends AndroidViewModel {

    public enum Filter { WEEK, MONTH, ALL }

    private final SudokuDatabase database;
    private final UserRepository userRepository;
    private final MutableLiveData<StatsUiState> uiState = new MutableLiveData<>();
    private Filter currentFilter = Filter.WEEK;

    public StatsViewModel(@NonNull Application application) {
        super(application);
        database = SudokuDatabase.getInstance(application);
        userRepository = new UserRepository(database.userProfileDao());
        userRepository.ensureProfileExists("default_user");
        refresh();
    }

    public LiveData<StatsUiState> getUiState() {
        return uiState;
    }

    public void setFilter(Filter filter) {
        currentFilter = filter;
        refresh();
    }

    public void refresh() {
        AppExecutors.getInstance().io().execute(() -> {
            UserProfile profile = database.userProfileDao().getUserProfile();
            if (profile == null) {
                userRepository.ensureProfileExistsSync("default_user");
                profile = database.userProfileDao().getUserProfile();
                if (profile == null) {
                    profile = new UserProfile("default_user");
                }
            }

            long now = System.currentTimeMillis();
            long since = currentFilter == Filter.WEEK ? now - 7L * 24 * 60 * 60 * 1000
                    : currentFilter == Filter.MONTH ? now - 30L * 24 * 60 * 60 * 1000
                    : 0L;

            // The chart always only shows the last 7 days regardless of the selected filter.
            // Avoid loading the entire history into memory for MONTH or ALL filters.

            com.example.sudoku.data.local.dao.GameStats stats = database.gameDao().getAggregatedStatsSince(since);

            int solved = stats != null ? stats.gameCount : 0;
            int accuracy = profile.totalGamesPlayed == 0 ? 0 : Math.round((profile.totalGamesWon * 100f) / profile.totalGamesPlayed);
            long bestTime = stats != null && stats.bestTime > 0 ? stats.bestTime : Long.MAX_VALUE;
            long totalTime = stats != null ? stats.totalTime : 0L;
            int xpEarned = stats != null ? stats.xpEarned : 0;
            int easy = stats != null ? stats.easyCount : 0;
            int medium = stats != null ? stats.mediumCount : 0;
            int hard = stats != null ? stats.hardCount : 0;
            int expert = stats != null ? stats.expertCount : 0;
            long averageTime = solved == 0 ? 0 : totalTime / solved;

            String[] labels = new String[7];
            int[] values = new int[7];
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now);
            calendar.add(Calendar.DAY_OF_YEAR, -6);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE", Locale.US);
            for (int i = 0; i < 7; i++) {
                long dayStart = startOfDay(calendar.getTimeInMillis());
                long dayEnd = dayStart + 24L * 60 * 60 * 1000;
                labels[i] = sdf.format(calendar.getTime());
                values[i] = database.gameDao().getGameCountInRange(dayStart, dayEnd);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            String insightTitle = "You are building steady momentum.";
            String insightBody = "Play a couple more medium puzzles this week to keep stretching your logic speed.";
            double currentAvg = database.gameDao().getAverageCompletionTimeForWindow(now - 7L * 24 * 60 * 60 * 1000, now);
            double previousAvg = database.gameDao().getAverageCompletionTimeForWindow(now - 14L * 24 * 60 * 60 * 1000, now - 7L * 24 * 60 * 60 * 1000);
            if (currentAvg > 0 && previousAvg > 0 && currentAvg < previousAvg) {
                insightTitle = "You improved your average solve time.";
                insightBody = "You are solving about " + Math.round(((previousAvg - currentAvg) / previousAvg) * 100) + "% faster than the previous week.";
            } else if (medium < Math.max(1, easy / 2)) {
                insightTitle = "Ready for a harder step?";
                insightBody = "Try two medium boards to balance your easy-puzzle speed with stronger logic practice.";
            }

            int totalDifficulty = Math.max(1, easy + medium + hard + expert);
            uiState.postValue(new StatsUiState(
                    currentFilter,
                    solved,
                    profile.currentStreak,
                    accuracy,
                    bestTime == Long.MAX_VALUE ? 0 : bestTime,
                    averageTime,
                    xpEarned,
                    labels,
                    values,
                    easy,
                    medium,
                    hard,
                    expert,
                    Math.round((easy * 100f) / totalDifficulty),
                    Math.round((medium * 100f) / totalDifficulty),
                    Math.round((hard * 100f) / totalDifficulty),
                    Math.round((expert * 100f) / totalDifficulty),
                    insightTitle,
                    insightBody));
        });
    }

    private long startOfDay(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static class StatsUiState {
        public final Filter filter;
        public final int solved;
        public final int streak;
        public final int accuracy;
        public final long bestTimeMs;
        public final long averageTimeMs;
        public final int xpEarned;
        public final String[] chartLabels;
        public final int[] chartValues;
        public final int easyCount;
        public final int mediumCount;
        public final int hardCount;
        public final int expertCount;
        public final int easyProgress;
        public final int mediumProgress;
        public final int hardProgress;
        public final int expertProgress;
        public final String insightTitle;
        public final String insightBody;

        public StatsUiState(Filter filter, int solved, int streak, int accuracy, long bestTimeMs,
                            long averageTimeMs, int xpEarned, String[] chartLabels, int[] chartValues,
                            int easyCount, int mediumCount, int hardCount, int expertCount,
                            int easyProgress, int mediumProgress, int hardProgress, int expertProgress,
                            String insightTitle, String insightBody) {
            this.filter = filter;
            this.solved = solved;
            this.streak = streak;
            this.accuracy = accuracy;
            this.bestTimeMs = bestTimeMs;
            this.averageTimeMs = averageTimeMs;
            this.xpEarned = xpEarned;
            this.chartLabels = chartLabels;
            this.chartValues = chartValues;
            this.easyCount = easyCount;
            this.mediumCount = mediumCount;
            this.hardCount = hardCount;
            this.expertCount = expertCount;
            this.easyProgress = easyProgress;
            this.mediumProgress = mediumProgress;
            this.hardProgress = hardProgress;
            this.expertProgress = expertProgress;
            this.insightTitle = insightTitle;
            this.insightBody = insightBody;
        }
    }
}
