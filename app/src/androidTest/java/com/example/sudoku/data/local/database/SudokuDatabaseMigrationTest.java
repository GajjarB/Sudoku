package com.example.sudoku.data.local.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SudokuDatabaseMigrationTest {

    private static final String TEST_DB = "sudoku-migration-test";

    @Rule
    public MigrationTestHelper helper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            SudokuDatabase.class.getCanonicalName());

    @Test
    public void migrate1To3_preservesSavedGamesAndAddsVersion3Defaults() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        db.execSQL("INSERT INTO level_progress "
                + "(levelNumber, isUnlocked, isCompleted, starsEarned, bestScore, bestTimeMs, "
                + "timesPlayed, timesCompleted, firstCompletedAt, lastPlayedAt) "
                + "VALUES (1, 1, 0, 0, 0, 0, 0, 0, 0, 0)");
        db.execSQL("INSERT INTO saved_games "
                + "(gameId, levelNumber, puzzleGrid, solutionGrid, currentGrid, lockedCells, "
                + "elapsedTimeMs, hintsRemaining, mistakesMade, isCompleted, isDailyChallenge, "
                + "startedAt, lastPlayedAt, completedAt) "
                + "VALUES ('game-1', 1, '000', '123', '000', '111', 42, 3, 0, 0, 0, 10, 20, 0)");
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, SudokuDatabase.MIGRATIONS);

        Cursor cursor = db.query("SELECT notes, undoStack, redoStack, selectedRow, selectedCol, "
                + "notesMode, variant, difficulty, schemaVersion "
                + "FROM saved_games WHERE gameId = 'game-1'");
        assertTrue(cursor.moveToFirst());
        assertEquals("", cursor.getString(0));
        assertEquals("", cursor.getString(1));
        assertEquals("", cursor.getString(2));
        assertEquals(-1, cursor.getInt(3));
        assertEquals(-1, cursor.getInt(4));
        assertEquals(0, cursor.getInt(5));
        assertEquals("CLASSIC_9X9", cursor.getString(6));
        assertEquals("", cursor.getString(7));
        assertEquals(3, cursor.getInt(8));
        cursor.close();
        db.close();
    }
}
