package com.example.sudoku.data.levels

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import java.io.InputStream

@Serializable
data class LevelBlock(
    val id: String,
    val difficulty: String,
    val clues: Int,
    val puzzle: String,
    val solution: String
)

object LevelStreamer {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    fun streamLevels(inputStream: InputStream): Sequence<LevelBlock> {
        // decodeToSequence will reactively parse the JSON
        return json.decodeToSequence<LevelBlock>(inputStream)
    }
}
