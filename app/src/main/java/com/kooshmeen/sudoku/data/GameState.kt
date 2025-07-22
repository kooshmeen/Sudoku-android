/**
 * Game state manager for Sudoku
 * Handles all game logic, state management, and action history for undo functionality
 */

package com.kooshmeen.sudoku.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import java.util.Stack

class GameState {
    // Game grid - 9x9 array of SudokuCell objects
    var grid by mutableStateOf(Array(9) { Array(9) { SudokuCell() } })
        private set

    // Currently selected cell
    var selectedCell by mutableStateOf<Pair<Int, Int>?>(null)
        private set

    // Current game mode
    var gameMode by mutableStateOf(GameMode.NORMAL)
        private set

    // Selected number from input row (1-9)
    var selectedNumber by mutableStateOf<Int?>(null)
        private set

    // Action history for undo functionality
    private val actionHistory = Stack<GameAction>()

    // Game timer
    var elapsedTimeSeconds by mutableStateOf(0)
        private set

    // Game difficulty
    var difficulty by mutableStateOf("Easy")
        private set

    // Game paused state
    var isPaused by mutableStateOf(false)
        private set

    enum class GameMode {
        NORMAL,    // Place numbers
        NOTES,     // Add/remove notes
        ERASE      // Clear cells
    }

    /**
     * Select a cell on the grid
     */
    fun selectCell(row: Int, col: Int) {
        selectedCell = Pair(row, col)
    }

    /**
     * Select a number from the input row
     */
    fun selectNumber(number: Int) {
        selectedNumber = if (selectedNumber == number) null else number
    }

    /**
     * Set the game mode (normal, notes, erase)
     */
    fun switchGameMode(mode: GameMode) {
        gameMode = mode
    }

    /**
     * Perform action on the currently selected cell based on current mode
     */
    fun performAction() {
        val cell = selectedCell ?: return
        val number = selectedNumber ?: return
        val (row, col) = cell

        when (gameMode) {
            GameMode.NORMAL -> setValue(row, col, number)
            GameMode.NOTES -> toggleNote(row, col, number)
            GameMode.ERASE -> clearCell(row, col)
        }
    }

    /**
     * Set a value in a cell
     */
    private fun setValue(row: Int, col: Int, value: Int) {
        val currentCell = grid[row][col]

        // Don't allow modifying original puzzle cells
        if (currentCell.isOriginal) return

        // Create action for undo
        val action = GameAction.SetValue(
            row = row,
            col = col,
            oldValue = currentCell.value,
            newValue = value,
            oldNotes = currentCell.notes
        )
        actionHistory.push(action)

        // Update the cell
        val newGrid = grid.map { it.clone() }.toTypedArray()
        newGrid[row][col] = currentCell.copy(
            value = value,
            notes = emptySet() // Clear notes when setting a value
        )
        grid = newGrid
    }

    /**
     * Toggle a note in a cell
     */
    private fun toggleNote(row: Int, col: Int, note: Int) {
        val currentCell = grid[row][col]

        // Only allow notes in empty cells
        if (currentCell.isFilled || currentCell.isOriginal) return

        val newGrid = grid.map { it.clone() }.toTypedArray()

        if (currentCell.notes.contains(note)) {
            // Remove note
            val action = GameAction.RemoveNote(row, col, note)
            actionHistory.push(action)

            newGrid[row][col] = currentCell.copy(
                notes = currentCell.notes - note
            )
        } else {
            // Add note
            val action = GameAction.AddNote(row, col, note)
            actionHistory.push(action)

            newGrid[row][col] = currentCell.copy(
                notes = currentCell.notes + note
            )
        }

        grid = newGrid
    }

    /**
     * Clear a cell (erase mode)
     */
    private fun clearCell(row: Int, col: Int) {
        val currentCell = grid[row][col]

        // Don't allow clearing original puzzle cells
        if (currentCell.isOriginal) return

        // Create action for undo
        val action = GameAction.ClearCell(
            row = row,
            col = col,
            oldValue = currentCell.value,
            oldNotes = currentCell.notes
        )
        actionHistory.push(action)

        // Clear the cell
        val newGrid = grid.map { it.clone() }.toTypedArray()
        newGrid[row][col] = currentCell.copy(
            value = 0,
            notes = emptySet()
        )
        grid = newGrid
    }

    /**
     * Undo the last action
     */
    fun undo() {
        if (actionHistory.isEmpty()) return

        val action = actionHistory.pop()
        val newGrid = grid.map { it.clone() }.toTypedArray()

        when (action) {
            is GameAction.SetValue -> {
                newGrid[action.row][action.col] = grid[action.row][action.col].copy(
                    value = action.oldValue,
                    notes = action.oldNotes
                )
            }
            is GameAction.AddNote -> {
                val currentCell = grid[action.row][action.col]
                newGrid[action.row][action.col] = currentCell.copy(
                    notes = currentCell.notes - action.note
                )
            }
            is GameAction.RemoveNote -> {
                val currentCell = grid[action.row][action.col]
                newGrid[action.row][action.col] = currentCell.copy(
                    notes = currentCell.notes + action.note
                )
            }
            is GameAction.ClearCell -> {
                newGrid[action.row][action.col] = grid[action.row][action.col].copy(
                    value = action.oldValue,
                    notes = action.oldNotes
                )
            }
        }

        grid = newGrid
    }

    /**
     * Initialize a new game with the given difficulty
     */
    fun startNewGame(difficulty: String) {
        this.difficulty = difficulty
        this.elapsedTimeSeconds = 0
        this.isPaused = false
        this.selectedCell = null
        this.selectedNumber = null
        this.gameMode = GameMode.NORMAL
        this.actionHistory.clear()

        // Initialize with empty grid for now
        // TODO: Generate puzzle based on difficulty
        grid = Array(9) { Array(9) { SudokuCell() } }
    }

    /**
     * Toggle pause state
     */
    fun togglePause() {
        isPaused = !isPaused
    }

    /**
     * Update timer (call this every second when game is not paused)
     */
    fun updateTimer() {
        if (!isPaused) {
            elapsedTimeSeconds++
        }
    }

    /**
     * Format elapsed time as MM:SS
     */
    fun getFormattedTime(): String {
        val minutes = elapsedTimeSeconds / 60
        val seconds = elapsedTimeSeconds % 60
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
