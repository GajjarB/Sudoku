package com.example.sudoku.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Pre-generated puzzle waiting to be served to a player. Pool is refilled
 * by a WorkManager job so opening a new level is instant.
 */
@Entity(
        tableName = "puzzle_pool",
        indices = {
                @Index(value = {"difficulty", "consumed"}),
                @Index(value = {"canonicalHash"}, unique = true)
        }
)
public class PuzzlePoolEntry {

    @PrimaryKey
    @NonNull
    public String id;

    public int clueCount;

    @NonNull
    public String difficulty;

    @NonNull
    public String variant;

    @NonNull
    public String puzzle;

    @NonNull
    public String solution;

    public long canonicalHash;

    public long generatedAt;

    public boolean consumed;

    public PuzzlePoolEntry(@NonNull String id,
                           int clueCount,
                           @NonNull String difficulty,
                           @NonNull String variant,
                           @NonNull String puzzle,
                           @NonNull String solution,
                           long canonicalHash,
                           long generatedAt) {
        this.id = id;
        this.clueCount = clueCount;
        this.difficulty = difficulty;
        this.variant = variant;
        this.puzzle = puzzle;
        this.solution = solution;
        this.canonicalHash = canonicalHash;
        this.generatedAt = generatedAt;
        this.consumed = false;
    }
}
