/**
 * Sealed class representing different actions that can be performed on the Sudoku grid
 * Used for implementing undo functionality
 */

package com.kooshmeen.sudoku.data

sealed class GameAction {
    data class SetValue(
        val row: Int,
        val col: Int,
        val oldValue: Int,
        val newValue: Int,
        val oldNotes: Set<Int>
    ) : GameAction()

    data class AddNote(
        val row: Int,
        val col: Int,
        val note: Int
    ) : GameAction()

    data class RemoveNote(
        val row: Int,
        val col: Int,
        val note: Int
    ) : GameAction()

    data class ClearCell(
        val row: Int,
        val col: Int,
        val oldValue: Int,
        val oldNotes: Set<Int>
    ) : GameAction()

    data class RemoveNotesBatch(
        val cells: List<Pair<Int, Int>>,
        val note: Int,
        val oldNotes: List<Set<Int>>
    ) : GameAction()
}
