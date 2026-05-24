package com.example.sudoku.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sudoku.core.AppExecutors;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.data.repository.GameRepository;
import com.example.sudoku.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GoalsViewModel extends AndroidViewModel {

    private final SudokuDatabase database;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<GoalsUiState> uiState = new MutableLiveData<>();
    private final LiveData<UserProfile> userProfile;

    public GoalsViewModel(@NonNull Application application) {
        super(application);
        database = SudokuDatabase.getInstance(application);
        gameRepository = new GameRepository(database.gameDao());
        userRepository = new UserRepository(database.userProfileDao());
        userProfile = userRepository.getUserProfile();
        userRepository.ensureProfileExists("default_user");
        refresh();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<GoalsUiState> getUiState() {
        return uiState;
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
            long startOfDay = startOfDay(now);
            long startOfWeek = startOfWeek(now);
            List<GameHistory> todayGames = database.gameDao().getGamesSince(startOfDay);
            List<GameHistory> weekGames = database.gameDao().getGamesSince(startOfWeek);
            SavedGame activeGame = gameRepository.getIncompleteGameSync();

            int solvedToday = todayGames.size();
            int lowMistakeToday = 0;
            for (GameHistory game : todayGames) {
                if (game.mistakesMade < 3) {
                    lowMistakeToday++;
                }
            }
            boolean notesUsed = activeGame != null && (activeGame.notesMode || (activeGame.notes != null && !activeGame.notes.isEmpty()));
            int weeklySolved = weekGames.size();
            int weeklyMedium = 0;
            for (GameHistory game : weekGames) {
                if (game.levelNumber > 5 && game.levelNumber <= 20) {
                    weeklyMedium++;
                }
            }

            List<GoalCardItem> dailyGoals = new ArrayList<>();
            dailyGoals.add(new GoalCardItem("🎯", "Solve 1 puzzle", solvedToday >= 1 ? "Completed" : solvedToday + "/1 completed", 20, Math.min(solvedToday, 1), 1, solvedToday >= 1 ? "Claim" : "Start", 1, false));
            dailyGoals.add(new GoalCardItem("🛡️", "Finish with fewer than 3 mistakes", lowMistakeToday >= 1 ? "Completed" : lowMistakeToday + "/1 completed", 30, Math.min(lowMistakeToday, 1), 1, lowMistakeToday >= 1 ? "Claim" : "Play", 1, false));
            dailyGoals.add(new GoalCardItem("✏️", "Use notes mode", notesUsed ? "Notes used in your active run" : "Turn notes on in your next puzzle", 10, notesUsed ? 1 : 0, 1, notesUsed ? "Claim" : "Use notes", 1, false));

            List<GoalCardItem> weeklyGoals = new ArrayList<>();
            weeklyGoals.add(new GoalCardItem("📚", "Complete 5 puzzles", weeklySolved + "/5 completed", 75, Math.min(weeklySolved, 5), 5, weeklySolved >= 5 ? "Complete" : "Play", 1, false));
            weeklyGoals.add(new GoalCardItem("🧠", "Finish 2 medium puzzles", weeklyMedium + "/2 completed", 60, Math.min(weeklyMedium, 2), 2, weeklyMedium >= 2 ? "Complete" : "Try medium", 8, false));
            weeklyGoals.add(new GoalCardItem("🔥", "Maintain a 5-day streak", Math.min(profile.currentStreak, 5) + "/5 days", 100, Math.min(profile.currentStreak, 5), 5, profile.currentStreak >= 5 ? "Complete" : "Keep going", 1, true));

            boolean chestReady = true;
            for (GoalCardItem item : dailyGoals) {
                chestReady &= item.progress >= item.target;
            }
            String chestMessage = chestReady
                    ? "All daily goals are complete — open the chest and collect your bonus reward."
                    : "Complete all daily goals to unlock today’s reward chest.";

            String streakMessage = profile.currentStreak == 0
                    ? "Solve one puzzle today to begin a fresh streak."
                    : "Play today to protect your " + profile.currentStreak + " day streak.";

            uiState.postValue(new GoalsUiState(streakMessage, chestMessage, chestReady, dailyGoals, weeklyGoals));
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

    private long startOfWeek(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static class GoalCardItem {
        public final String icon;
        public final String title;
        public final String subtitle;
        public final int rewardXp;
        public final int progress;
        public final int target;
        public final String actionLabel;
        public final int suggestedLevel;
        public final boolean dailyChallenge;

        public GoalCardItem(String icon, String title, String subtitle, int rewardXp, int progress, int target,
                            String actionLabel, int suggestedLevel, boolean dailyChallenge) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.rewardXp = rewardXp;
            this.progress = progress;
            this.target = target;
            this.actionLabel = actionLabel;
            this.suggestedLevel = suggestedLevel;
            this.dailyChallenge = dailyChallenge;
        }
    }

    public static class GoalsUiState {
        public final String streakMessage;
        public final String chestMessage;
        public final boolean chestReady;
        public final List<GoalCardItem> dailyGoals;
        public final List<GoalCardItem> weeklyGoals;

        public GoalsUiState(String streakMessage, String chestMessage, boolean chestReady,
                            List<GoalCardItem> dailyGoals, List<GoalCardItem> weeklyGoals) {
            this.streakMessage = streakMessage;
            this.chestMessage = chestMessage;
            this.chestReady = chestReady;
            this.dailyGoals = dailyGoals;
            this.weeklyGoals = weeklyGoals;
        }
    }
}
