package com.example.sudoku.domain.engine

import com.example.sudoku.utils.Constants
import kotlin.math.max

object ScoreEngine {

    fun calculateScore(
        difficultyLevel: Int,
        timeMs: Long,
        mistakes: Int,
        hintsUsed: Int,
        advancedDeductions: Int = 0
    ): Int {
        val baseWeight = getDifficultyWeight(difficultyLevel)

        // Time decay: 100% at 0 time, drops to 30% at max time
        val targetTimeMs = getTargetTime(difficultyLevel)
        val timeRatio = timeMs.toDouble() / targetTimeMs
        val timeMultiplier = max(0.3, 1.0 - (timeRatio * 0.5)) // Non-linear decay could be used here

        val mistakePenalty = mistakes * Constants.MISTAKE_PENALTY
        val hintPenalty = hintsUsed * Constants.HINT_PENALTY
        val bonus = advancedDeductions * 50

        var score = ((Constants.BASE_SCORE * baseWeight * timeMultiplier).toInt()
                    - mistakePenalty - hintPenalty + bonus)

        return max(50, score)
    }

    private fun getDifficultyWeight(difficultyLevel: Int): Double {
        return when {
            difficultyLevel <= 5 -> 1.0
            difficultyLevel <= 20 -> 1.5
            difficultyLevel <= 50 -> 2.0
            difficultyLevel <= 200 -> 3.0
            else -> 4.0
        }
    }

    private fun getTargetTime(difficultyLevel: Int): Long {
        return when {
            difficultyLevel <= 5 -> 5 * 60 * 1000L
            difficultyLevel <= 20 -> 10 * 60 * 1000L
            difficultyLevel <= 50 -> 20 * 60 * 1000L
            difficultyLevel <= 200 -> 30 * 60 * 1000L
            else -> 45 * 60 * 1000L
        }
    }
}
