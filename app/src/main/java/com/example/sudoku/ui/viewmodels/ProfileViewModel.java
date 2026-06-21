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

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final SudokuDatabase database;
    private final UserRepository userRepository;
    private final MutableLiveData<ProfileUiState> uiState = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        database = SudokuDatabase.getInstance(application);
        userRepository = new UserRepository(database.userProfileDao());
        userRepository.ensureProfileExists("default_user");
        refresh();
    }

    public LiveData<ProfileUiState> getUiState() {
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

            boolean hasPerfectGame = database.gameDao().hasPerfectGame();
            boolean hasFastGame = database.gameDao().hasFastGame(5 * 60 * 1000L);

            List<AchievementItem> achievements = new ArrayList<>();
            achievements.add(new AchievementItem(profile.totalGamesWon >= 1 ? "✅" : "🏁", "First Win", "Complete your first puzzle", profile.totalGamesWon >= 1));
            achievements.add(new AchievementItem(hasPerfectGame ? "✨" : "🎯", "Perfect Game", "Finish with no mistakes and no hints", hasPerfectGame));
            achievements.add(new AchievementItem(hasFastGame ? "⚡" : "⏱️", "Speed Solver", "Finish under 5 minutes", hasFastGame));
            achievements.add(new AchievementItem(profile.currentStreak >= 7 ? "🔥" : "📅", "Streak Master", "Keep a 7-day streak", profile.currentStreak >= 7));

            List<RowItem> customization = new ArrayList<>();
            customization.add(new RowItem("Themes", "Current app theme", "settings"));
            customization.add(new RowItem("Board Style", "Classic rounded grid", null));
            customization.add(new RowItem("App Icon", "Default", null));
            customization.add(new RowItem("Number Style", "Bold digits", null));

            List<RowItem> settings = new ArrayList<>();
            settings.add(new RowItem("Sound", profile.soundEnabled ? "On" : "Off", "settings"));
            settings.add(new RowItem("Vibration", profile.vibrationEnabled ? "On" : "Off", "settings"));
            settings.add(new RowItem("Auto Check", "Managed from Settings", "settings"));
            settings.add(new RowItem("Notes by Default", "Managed from Settings", "settings"));

            int currentLevel = Math.max(1, profile.currentLevel);
            int levelFloorXp = xpForLevel(currentLevel);
            int nextLevelXp = xpForLevel(currentLevel + 1);
            int progress = Math.max(0, profile.totalXP - levelFloorXp);
            int maxProgress = Math.max(1, nextLevelXp - levelFloorXp);
            int progressPercent = Math.min(100, Math.round((progress * 100f) / maxProgress));

            String rank = "Level " + currentLevel + " • Sudoku Player";
            String xpText = profile.totalXP + " / " + nextLevelXp + " XP";
            uiState.postValue(new ProfileUiState(
                    "S",
                    "Sudoku Player",
                    rank,
                    xpText,
                    progressPercent,
                    achievements,
                    customization,
                    settings));
        });
    }

    private int xpForLevel(int level) {
        return (int) (Math.pow(level, 1.5) * 100);
    }

    public static class AchievementItem {
        public final String icon;
        public final String title;
        public final String subtitle;
        public final boolean unlocked;

        public AchievementItem(String icon, String title, String subtitle, boolean unlocked) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.unlocked = unlocked;
        }
    }

    public static class RowItem {
        public final String title;
        public final String subtitle;
        public final String action;

        public RowItem(String title, String subtitle, String action) {
            this.title = title;
            this.subtitle = subtitle;
            this.action = action;
        }
    }

    public static class ProfileUiState {
        public final String avatarLetter;
        public final String name;
        public final String rank;
        public final String xpText;
        public final int xpProgress;
        public final List<AchievementItem> achievements;
        public final List<RowItem> customization;
        public final List<RowItem> settings;

        public ProfileUiState(String avatarLetter, String name, String rank, String xpText, int xpProgress,
                              List<AchievementItem> achievements, List<RowItem> customization,
                              List<RowItem> settings) {
            this.avatarLetter = avatarLetter;
            this.name = name;
            this.rank = rank;
            this.xpText = xpText;
            this.xpProgress = xpProgress;
            this.achievements = achievements;
            this.customization = customization;
            this.settings = settings;
        }
    }
}
