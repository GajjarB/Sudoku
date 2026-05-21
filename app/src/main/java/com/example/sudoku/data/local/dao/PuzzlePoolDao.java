package com.example.sudoku.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import com.example.sudoku.data.local.entities.PuzzlePoolEntry;

import java.util.List;

@Dao
public interface PuzzlePoolDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(PuzzlePoolEntry entry);

    @Query("SELECT COUNT(*) FROM puzzle_pool WHERE difficulty = :difficulty AND variant = :variant AND consumed = 0")
    int countAvailable(String difficulty, String variant);

    @Query("SELECT * FROM puzzle_pool WHERE difficulty = :difficulty AND variant = :variant AND consumed = 0 ORDER BY generatedAt ASC LIMIT 1")
    PuzzlePoolEntry takeOne(String difficulty, String variant);

    @Query("UPDATE puzzle_pool SET consumed = 1 WHERE id = :id AND consumed = 0")
    int markConsumed(String id);

    @Transaction
    default PuzzlePoolEntry consumeOne(String difficulty, String variant) {
        PuzzlePoolEntry entry = takeOne(difficulty, variant);
        if (entry == null) {
            return null;
        }
        return markConsumed(entry.id) == 1 ? entry : null;
    }

    @Query("SELECT canonicalHash FROM puzzle_pool")
    List<Long> getAllCanonicalHashes();

    @Query("SELECT EXISTS(SELECT 1 FROM puzzle_pool WHERE canonicalHash = :hash)")
    boolean hashExists(long hash);

    @Query("DELETE FROM puzzle_pool WHERE consumed = 1 AND generatedAt < :olderThan")
    int deleteConsumedOlderThan(long olderThan);
}
