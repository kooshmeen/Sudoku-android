/**
 * Game state manager for Sudoku
 * Handles all game logic, state management, and action history for undo functionality
 */

package com.kooshmeen.sudoku.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kooshmeen.sudoku.utils.SudokuValidator
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

    // Current number of mistakes made by the player
    var mistakesCount by mutableIntStateOf(0)
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

    // Error cells
    var errorCells by mutableStateOf(emptySet<Pair<Int, Int>>())
        private set

    // Game active state
    var isGameActive by mutableStateOf(false)
        private set

    // Game completed state
    var isGameCompleted by mutableStateOf(false)
        private set

    // Store the original solution grid for error checking
    private var solutionGrid: Array<IntArray> = Array(9) { IntArray(9) }

    enum class GameMode {
        NORMAL,    // Place numbers
        NOTES,     // Add/remove notes
        ERASE      // Clear cells
    }

    /**
     * Select a number from the input row
     */
    fun selectNumber(number: Int) {
        selectedNumber = if (selectedNumber == number) null else number
    }

    /**
     * Toggle the game mode between NORMAL and NOTES
     */
    fun toggleNotesMode() {
        gameMode = if (gameMode == GameMode.NOTES) GameMode.NORMAL else GameMode.NOTES
    }

    /**
     * Toggle erase mode on/off
     */
    fun toggleEraseMode() {
        gameMode = if (gameMode == GameMode.ERASE) GameMode.NORMAL else GameMode.ERASE
    }

    /**
     * Set erase mode
     */
    fun setEraseMode() {
        gameMode = GameMode.ERASE
    }

    /**
     * Input a number directly to a cell (no selection needed)
     */
    fun inputToCell(row: Int, col: Int) {
        val number = selectedNumber ?: return

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
        if (currentCell.isOriginal) return
        val action = GameAction.SetValue(
            row = row,
            col = col,
            oldValue = currentCell.value,
            newValue = value,
            oldNotes = currentCell.notes
        )
        actionHistory.push(action)
        val newGrid = Array(9) { r -> Array(9) { c -> grid[r][c] } }
        // Error: compare to solutionGrid
        val hasError = solutionGrid[row][col] != value
        newGrid[row][col] = currentCell.copy(
            value = value,
            notes = emptySet(),
            hasError = hasError
        )
        // Track cells and old notes for undo
        val affectedCells = mutableListOf<Pair<Int, Int>>()
        val affectedOldNotes = mutableListOf<Set<Int>>()
        // Remove notes of this value in row
        for (c in 0..8) {
            if (c != col) {
                val cell = newGrid[row][c]
                if (cell.notes.contains(value)) {
                    affectedCells.add(Pair(row, c))
                    affectedOldNotes.add(cell.notes)
                    newGrid[row][c] = cell.copy(notes = cell.notes - value)
                }
            }
        }
        // Remove from column
        for (r in 0..8) {
            if (r != row) {
                val cell = newGrid[r][col]
                if (cell.notes.contains(value)) {
                    affectedCells.add(Pair(r, col))
                    affectedOldNotes.add(cell.notes)
                    newGrid[r][col] = cell.copy(notes = cell.notes - value)
                }
            }
        }
        // Remove from box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col)) {
                    val cell = newGrid[r][c]
                    if (cell.notes.contains(value)) {
                        affectedCells.add(Pair(r, c))
                        affectedOldNotes.add(cell.notes)
                        newGrid[r][c] = cell.copy(notes = cell.notes - value)
                    }
                }
            }
        }
        // Push batch note removal to action stack for undo
        if (affectedCells.isNotEmpty()) {
            actionHistory.push(GameAction.RemoveNotesBatch(affectedCells, value, affectedOldNotes))
        }
        if (hasError) {
            errorCells = errorCells + Pair(row, col)
            // Mark as mistake
            mistakesCount++
        } else {
            errorCells = errorCells - Pair(row, col)
        }
        grid = newGrid
        if (isGameComplete()) {
            isGameCompleted = true
        }
    }

    /**
     * Toggle a note in a cell
     */
    private fun toggleNote(row: Int, col: Int, note: Int) {
        val currentCell = grid[row][col]

        // Only allow notes in empty cells
        if (currentCell.isFilled || currentCell.isOriginal) return

        val newGrid = Array(9) { r -> Array(9) { c -> grid[r][c] } }

        if (currentCell.notes.contains(note)) {
            // Remove note
            val action = GameAction.RemoveNote(row, col, note)
            actionHistory.push(action)

            newGrid[row][col] = currentCell.copy(
                notes = currentCell.notes - note,
            )
        } else {
            // Add note
            val action = GameAction.AddNote(row, col, note)
            actionHistory.push(action)

            newGrid[row][col] = currentCell.copy(
                notes = currentCell.notes + note,
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
        val newGrid = Array(9) { r -> Array(9) { c -> grid[r][c] } }
        newGrid[row][col] = SudokuCell() // Create a new empty cell
        grid = newGrid
    }

    /**
     * Undo the last action
     */
    fun undo() {
        if (actionHistory.isEmpty()) return

        val action = actionHistory.pop()
        val newGrid = Array(9) { r -> Array(9) { c -> grid[r][c] } }

        when (action) {
            is GameAction.SetValue -> {
                newGrid[action.row][action.col] = grid[action.row][action.col].copy(
                    value = action.oldValue,
                    notes = action.oldNotes,
                    hasError = false
                )
            }
            is GameAction.AddNote -> {
                val currentCell = grid[action.row][action.col]
                newGrid[action.row][action.col] = currentCell.copy(
                    notes = currentCell.notes - action.note,
                )
            }
            is GameAction.RemoveNote -> {
                val currentCell = grid[action.row][action.col]
                newGrid[action.row][action.col] = currentCell.copy(
                    notes = currentCell.notes + action.note,
                )
            }
            is GameAction.ClearCell -> {
                newGrid[action.row][action.col] = SudokuCell(
                    value = action.oldValue,
                    notes = action.oldNotes,
                    isOriginal = grid[action.row][action.col].isOriginal
                )
            }
            is GameAction.RemoveNotesBatch -> {
                action.cells.forEachIndexed { idx, (row, col) ->
                    val cell = grid[row][col]
                    newGrid[row][col] = cell.copy(notes = action.oldNotes[idx])
                }
            }
        }

        grid = newGrid
    }

    /**
     * Check if there's an active game in progress
     */
    fun hasActiveGame(): Boolean {
        return isGameActive && !isGameCompleted
    }

    /**
     * Check if the game is completed
     */
    fun isGameComplete(): Boolean {
        // Check if all cells are filled and there are no errors
        for (row in 0..8) {
            for (col in 0..8) {
                val cell = grid[row][col]
                if (cell.isEmpty || cell.hasError) {
                    return false
                }
            }
        }
        return true
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
        this.isGameActive = true
        this.isGameCompleted = false
        this.errorCells = emptySet()

        // Generate a new puzzle and store the solution grid
        val generator = SudokuGenerator()
        val completeGrid = generator.generateCompleteGrid()
        solutionGrid = completeGrid.map { it.clone() }.toTypedArray()
        grid = generator.createPuzzleWithUniquenessCheck(completeGrid, difficulty)
    }

    /**
     * Continue an existing game
     */
    fun continueGame() {
        this.isPaused = false
        this.selectedCell = null
        this.selectedNumber = null
        this.gameMode = GameMode.NORMAL
    }

    /**
     * End the current game
     */
    fun endGame() {
        this.isGameActive = false
        this.isGameCompleted = isGameComplete()
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
        if (!isPaused && isGameActive) {
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
