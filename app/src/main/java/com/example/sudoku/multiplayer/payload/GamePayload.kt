package com.example.sudoku.multiplayer.payload

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GamePayload(
    val type: PayloadType,
    val timestamp: Long = System.currentTimeMillis(),
    val progress: Int = 0, // Number of correctly placed cells
    val errors: Int = 0, // Number of incorrectly placed cells
    val isComplete: Boolean = false,
    val seed: Long? = null // Optional seed for game sync
) {
    companion object {
        fun toByteArray(payload: GamePayload): ByteArray {
            return Json.encodeToString(payload).toByteArray(Charsets.UTF_8)
        }

        fun fromByteArray(bytes: ByteArray): GamePayload? {
            return try {
                val jsonString = String(bytes, Charsets.UTF_8)
                Json.decodeFromString<GamePayload>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }
}

@Serializable
enum class PayloadType {
    SYNC_STATE,
    GAME_START,
    GAME_END,
    MOVE_MADE
}
