/**
 * Data class representing the serializable game state
 * Used for saving/loading games from SharedPreferences
 */

package com.kooshmeen.sudoku.data

data class SavedGameState(
    val grid: List<List<SavedCellState>>,
    val difficulty: String,
    val elapsedTimeSeconds: Int,
    val mistakesCount: Int,
    val solutionGrid: List<List<Int>>
)

data class SavedCellState(
    val value: Int,
    val notes: List<Int>,
    val isOriginal: Boolean,
    val hasError: Boolean
)
