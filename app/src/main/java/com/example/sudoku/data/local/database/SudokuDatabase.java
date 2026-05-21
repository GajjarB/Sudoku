package com.example.sudoku.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.sudoku.data.local.dao.DailyChallengeDao;
import com.example.sudoku.data.local.dao.GameDao;
import com.example.sudoku.data.local.dao.LevelProgressDao;
import com.example.sudoku.data.local.dao.PuzzlePoolDao;
import com.example.sudoku.data.local.dao.UserProfileDao;
import com.example.sudoku.data.local.entities.CognitiveMetrics;
import com.example.sudoku.data.local.entities.DailyChallenge;
import com.example.sudoku.data.local.entities.GameHistory;
import com.example.sudoku.data.local.entities.LevelProgress;
import com.example.sudoku.data.local.entities.PlayerBehavior;
import com.example.sudoku.data.local.entities.PuzzlePoolEntry;
import com.example.sudoku.data.local.entities.SavedGame;
import com.example.sudoku.data.local.entities.SolvingPattern;
import com.example.sudoku.data.local.entities.UserProfile;
import com.example.sudoku.utils.Constants;

/**
 * Room database for Sudoku Nexus.
 */
@Database(
    entities = {
        UserProfile.class,
        LevelProgress.class,
        SavedGame.class,
        GameHistory.class,
        DailyChallenge.class,
        PlayerBehavior.class,
        SolvingPattern.class,
        CognitiveMetrics.class,
        PuzzlePoolEntry.class
    },
    version = Constants.DATABASE_VERSION,
    exportSchema = true
)
public abstract class SudokuDatabase extends RoomDatabase {

    private static volatile SudokuDatabase INSTANCE;

    public abstract GameDao gameDao();
    public abstract UserProfileDao userProfileDao();
    public abstract LevelProgressDao levelProgressDao();
    public abstract DailyChallengeDao dailyChallengeDao();
    public abstract PuzzlePoolDao puzzlePoolDao();

    public static SudokuDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SudokuDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SudokuDatabase.class,
                            Constants.DATABASE_NAME)
                            .addMigrations(MIGRATIONS)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final androidx.room.migration.Migration MIGRATION_1_2 =
            new androidx.room.migration.Migration(1, 2) {
                @Override
                public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `puzzle_pool` ("
                            + "`id` TEXT NOT NULL, "
                            + "`clueCount` INTEGER NOT NULL, "
                            + "`difficulty` TEXT NOT NULL, "
                            + "`variant` TEXT NOT NULL, "
                            + "`puzzle` TEXT NOT NULL, "
                            + "`solution` TEXT NOT NULL, "
                            + "`canonicalHash` INTEGER NOT NULL, "
                            + "`generatedAt` INTEGER NOT NULL, "
                            + "`consumed` INTEGER NOT NULL, "
                            + "PRIMARY KEY(`id`))");
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_puzzle_pool_difficulty_consumed` "
                            + "ON `puzzle_pool` (`difficulty`, `consumed`)");
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_puzzle_pool_canonicalHash` "
                            + "ON `puzzle_pool` (`canonicalHash`)");
                }
            };

    static final androidx.room.migration.Migration MIGRATION_2_3 =
            new androidx.room.migration.Migration(2, 3) {
                @Override
                public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `undoStack` TEXT NOT NULL DEFAULT ''");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `redoStack` TEXT NOT NULL DEFAULT ''");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `selectedRow` INTEGER NOT NULL DEFAULT -1");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `selectedCol` INTEGER NOT NULL DEFAULT -1");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `notesMode` INTEGER NOT NULL DEFAULT 0");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `variant` TEXT NOT NULL DEFAULT 'CLASSIC_9X9'");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `difficulty` TEXT NOT NULL DEFAULT ''");
                    db.execSQL("ALTER TABLE `saved_games` ADD COLUMN `schemaVersion` INTEGER NOT NULL DEFAULT 3");
                }
            };

    static final androidx.room.migration.Migration[] MIGRATIONS = { MIGRATION_1_2, MIGRATION_2_3 };

    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}
