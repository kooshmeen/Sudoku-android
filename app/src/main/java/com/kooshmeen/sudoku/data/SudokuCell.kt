/**
 * Data class representing a single Sudoku cell
 */

package com.kooshmeen.sudoku.data

data class SudokuCell(
    val value: Int = 0, // 0 means empty, 1-9 for filled cells
    val notes: Set<Int> = emptySet(), // Notes for empty cells
    val isOriginal: Boolean = false, // True if this was part of the initial puzzle
    val isHighlighted: Boolean = false, // For visual feedback
    val hasError: Boolean = false
) {
    val isEmpty: Boolean get() = value == 0
    val isFilled: Boolean get() = value != 0
}
