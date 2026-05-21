package com.example.sudoku.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.sudoku.data.local.dao.PuzzlePoolDao;
import com.example.sudoku.data.local.database.SudokuDatabase;
import com.example.sudoku.data.local.entities.PuzzlePoolEntry;
import com.example.sudoku.domain.engine.CancellationToken;
import com.example.sudoku.domain.engine.Difficulty;
import com.example.sudoku.domain.engine.GenerationResult;
import com.example.sudoku.domain.engine.SudokuGenerator;
import com.example.sudoku.domain.engine.SymmetryStrategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Refill the puzzle pool so opening a new level never blocks on generation.
 *
 * Target: TARGET_PER_DIFFICULTY puzzles per (variant, difficulty). Only
 * classic 9x9 is generated until other variants land.
 */
public class PuzzlePoolWorker extends Worker {

    private static final String TAG = "PuzzlePoolWorker";
    private static final String VARIANT = "CLASSIC_9X9";
    private static final int TARGET_PER_DIFFICULTY = 20;
    private static final long PER_PUZZLE_TIMEOUT_MS = 8_000L;

    public PuzzlePoolWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        PuzzlePoolDao dao = SudokuDatabase.getInstance(getApplicationContext()).puzzlePoolDao();
        Set<Long> knownHashes = new HashSet<>();
        List<Long> existing = dao.getAllCanonicalHashes();
        if (existing != null) knownHashes.addAll(existing);

        SudokuGenerator generator = new SudokuGenerator();

        for (Difficulty difficulty : Difficulty.values()) {
            if (isStopped()) return Result.success();

            int available = dao.countAvailable(difficulty.name(), VARIANT);
            int needed = Math.max(0, TARGET_PER_DIFFICULTY - available);
            for (int i = 0; i < needed; i++) {
                if (isStopped()) return Result.success();

                CancellationToken token = new CancellationToken();
                GenerationResult result = generator.generate(
                        difficulty.targetClues(),
                        SymmetryStrategy.ROTATIONAL_180,
                        PER_PUZZLE_TIMEOUT_MS,
                        token,
                        knownHashes::contains);

                if (!result.isSuccess()) {
                    Log.w(TAG, "Generation skipped: " + result.status() + " " + result.message());
                    continue;
                }
                SudokuGenerator.Puzzle puzzle = result.puzzle();
                if (!knownHashes.add(puzzle.canonicalHash)) continue;

                PuzzlePoolEntry entry = new PuzzlePoolEntry(
                        UUID.randomUUID().toString(),
                        puzzle.clueCount,
                        difficulty.name(),
                        VARIANT,
                        puzzle.puzzle,
                        puzzle.solution,
                        puzzle.canonicalHash,
                        System.currentTimeMillis());

                try {
                    dao.insert(entry);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to persist puzzle pool entry", e);
                }
            }
        }

        long sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        try {
            int purged = dao.deleteConsumedOlderThan(sevenDaysAgo);
            if (purged > 0) Log.d(TAG, "Purged consumed pool entries: " + purged);
        } catch (Exception e) {
            Log.w(TAG, "Pool purge failed", e);
        }
        return Result.success();
    }
}
