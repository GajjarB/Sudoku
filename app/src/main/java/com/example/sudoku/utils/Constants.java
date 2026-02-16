package com.example.sudoku.utils;

/**
 * ═══════════════════════════════════════════════════════════════════
 * CONSTANTS — All Application Constants
 * ═══════════════════════════════════════════════════════════════════
 */
public class Constants {
    
    // ═══════════════════════════════════════════════════════════════
    // DATABASE
    // ═══════════════════════════════════════════════════════════════
    public static final String DATABASE_NAME = "sudoku_nexus.db";
    public static final int DATABASE_VERSION = 1;
    
    // ═══════════════════════════════════════════════════════════════
    // GAME CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    public static final int GRID_SIZE = 9;
    public static final int BOX_SIZE = 3;
    public static final int TOTAL_CELLS = 81;
    public static final int MAX_LEVEL = 999;
    
    // Difficulty Ranges (clue counts)
    public static final int CLUES_EASY_MIN = 38;
    public static final int CLUES_EASY_MAX = 40;
    public static final int CLUES_MEDIUM_MIN = 32;
    public static final int CLUES_MEDIUM_MAX = 37;
    public static final int CLUES_HARD_MIN = 26;
    public static final int CLUES_HARD_MAX = 31;
    public static final int CLUES_EXPERT_MIN = 22;
    public static final int CLUES_EXPERT_MAX = 25;
    
    // ═══════════════════════════════════════════════════════════════
    // GAMEPLAY
    // ═══════════════════════════════════════════════════════════════
    public static final int MAX_HINTS_PER_GAME = 3;
    public static final int MAX_MISTAKES_ALLOWED = 3;
    public static final int INITIAL_HINTS = 3;
    
    // ═══════════════════════════════════════════════════════════════
    // SCORING & XP
    // ═══════════════════════════════════════════════════════════════
    public static final int BASE_SCORE = 1000;
    public static final int TIME_BONUS_MAX = 500;
    public static final int ACCURACY_BONUS_MAX = 300;
    public static final int MISTAKE_PENALTY = 100;
    public static final int HINT_PENALTY = 50;
    
    // XP System
    public static final int BASE_XP = 100;
    public static final int XP_PER_STAR = 50;
    public static final int DAILY_CHALLENGE_XP_MULTIPLIER = 2;
    public static final double XP_CURVE_EXPONENT = 1.5; // For level-up formula
    
    // ═══════════════════════════════════════════════════════════════
    // STREAK SYSTEM
    // ═══════════════════════════════════════════════════════════════
    public static final int GRACE_PERIODS_PER_MONTH = 1;
    public static final long STREAK_GRACE_HOURS = 24;
    
    // ═══════════════════════════════════════════════════════════════
    // TIME LIMITS
    // ═══════════════════════════════════════════════════════════════
    public static final long TIME_BONUS_THRESHOLD_MS = 5 * 60 * 1000; // 5 minutes
    public static final long PERFECT_TIME_MS = 3 * 60 * 1000; // 3 minutes
    
    // ═══════════════════════════════════════════════════════════════
    // STARS CALCULATION
    // ═══════════════════════════════════════════════════════════════
    public static final int STARS_3_MIN_SCORE = 1500;
    public static final int STARS_2_MIN_SCORE = 1000;
    public static final int STARS_1_MIN_SCORE = 500;
    
    // ═══════════════════════════════════════════════════════════════
    // PREFERENCES KEYS
    // ═══════════════════════════════════════════════════════════════
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_LAST_PLAYED_DATE = "last_played_date";
    public static final String PREF_GRACE_USES_THIS_MONTH = "grace_uses_this_month";
    public static final String PREF_GRACE_RESET_MONTH = "grace_reset_month";
    public static final String PREF_SOUND_ENABLED = "sound_enabled";
    public static final String PREF_VIBRATION_ENABLED = "vibration_enabled";
    public static final String PREF_THEME = "theme";
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_INCOMPLETE_GAME_ID = "incomplete_game_id";
    
    // ═══════════════════════════════════════════════════════════════
    // INTENT EXTRAS
    // ═══════════════════════════════════════════════════════════════
    public static final String EXTRA_GAME_ID = "game_id";
    public static final String EXTRA_LEVEL_NUMBER = "level_number";
    public static final String EXTRA_IS_DAILY_CHALLENGE = "is_daily_challenge";
    public static final String EXTRA_RESUME_GAME = "resume_game";
    
    // ═══════════════════════════════════════════════════════════════
    // WORKER TAGS
    // ═══════════════════════════════════════════════════════════════
    public static final String WORKER_DAILY_ANALYSIS = "daily_analysis";
    public static final String WORKER_STREAK_CHECK = "streak_check";
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMATION DURATIONS
    // ═══════════════════════════════════════════════════════════════
    public static final int ANIM_CELL_FLASH_DURATION = 300;
    public static final int ANIM_VICTORY_DURATION = 1000;
    public static final int ANIM_LEVEL_UP_DURATION = 1500;
    public static final int ANIM_FADE_DURATION = 200;
    
    // ═══════════════════════════════════════════════════════════════
    // SUDOKU STRATEGIES (for AI learning)
    // ═══════════════════════════════════════════════════════════════
    public static final String STRATEGY_NAKED_SINGLE = "NAKED_SINGLE";
    public static final String STRATEGY_HIDDEN_SINGLE = "HIDDEN_SINGLE";
    public static final String STRATEGY_NAKED_PAIR = "NAKED_PAIR";
    public static final String STRATEGY_NAKED_TRIPLE = "NAKED_TRIPLE";
    public static final String STRATEGY_HIDDEN_PAIR = "HIDDEN_PAIR";
    public static final String STRATEGY_POINTING_PAIR = "POINTING_PAIR";
    public static final String STRATEGY_BOX_LINE_REDUCTION = "BOX_LINE_REDUCTION";
    public static final String STRATEGY_LOGICAL_DEDUCTION = "LOGICAL_DEDUCTION";
    public static final String STRATEGY_GUESSING = "GUESSING";
    
    // ═══════════════════════════════════════════════════════════════
    // AI THRESHOLDS
    // ═══════════════════════════════════════════════════════════════
    public static final double AI_PLATEAU_THRESHOLD = 0.7;
    public static final double AI_STRUGGLING_ERROR_RATE = 0.4;
    public static final int AI_STRUGGLING_READINESS = 20;
    public static final double AI_MASTERING_ERROR_RATE = 0.1;
    public static final int AI_MASTERING_READINESS = 80;
    public static final long AI_MASTERING_DECISION_TIME = 3000;
    
    // ═══════════════════════════════════════════════════════════════
    // VIEW DIMENSIONS (in dp)
    // ═══════════════════════════════════════════════════════════════
    public static final int GRID_PADDING = 16;
    public static final int CELL_SIZE = 40;
    public static final int GRID_LINE_THIN = 1;
    public static final int GRID_LINE_THICK = 3;
    
    // ═══════════════════════════════════════════════════════════════
    // NOTIFICATION
    // ═══════════════════════════════════════════════════════════════
    public static final String NOTIFICATION_CHANNEL_STREAK = "streak_channel";
    public static final String NOTIFICATION_CHANNEL_DAILY = "daily_challenge_channel";
    public static final int NOTIFICATION_ID_STREAK = 1001;
    public static final int NOTIFICATION_ID_DAILY = 1002;
}
