/**
 * Game state manager for Sudoku
 * Handles all game logic, state management, and action history for undo functionality
 */

package com.kooshmeen.sudoku.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kooshmeen.sudoku.repository.SudokuRepository
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
    internal var solutionGrid: Array<IntArray> = Array(9) { IntArray(9) }
        private set

    enum class GameMode {
        NORMAL,    // Place numbers
        NOTES,     // Add/remove notes
        ERASE      // Clear cells
    }

    private lateinit var repository: SudokuRepository

    fun initializeRepository(context: Context) {
        repository = SudokuRepository(context)
    }

    suspend fun submitScoreToServer(): Boolean {
        return if (isGameCompleted && ::repository.isInitialized && repository.isLoggedIn()) {
            val result = repository.submitGame(
                difficulty = difficulty,
                timeSeconds = elapsedTimeSeconds,
                mistakes = mistakesCount
            )
            result.isSuccess
        } else {
            false
        }
    }

    /**
     * Record game completion in local statistics
     */
    fun recordGameCompletion(context: Context) {
        if (isGameCompleted) {
            com.kooshmeen.sudoku.utils.StatisticsManager.recordCompletedGame(
                context = context,
                difficulty = difficulty,
                score = highestPossibleScore(),
                timeSeconds = elapsedTimeSeconds,
                mistakes = mistakesCount
            )
        }
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
     * Returns highest possible score from current game state
     * Calculated as:
     * const score = Math.round((difficultyMultiplier * mistakePenalty) * (Math.max(0, (timeScore - timeSeconds) * 2) + basePoints));
     * With: difficultyMultiplier = 0.33 for Easy, 0.7 for Medium, 1.5 for Hard
     * timeScore = 600 for Easy, 1200 for Medium, 1800 for Hard
     * mistakePenalty = max(0.4, 1 - (numberOfMistakes * 0.1)
     * basePoints = 1000
     */
    fun highestPossibleScore(): Int {
        val difficultyMultiplier = when (difficulty.lowercase(Locale.ROOT)) {
            "easy" -> 0.33
            "medium" -> 0.7
            "hard" -> 1.5
            else -> 0.33 // Default to Easy if unknown
        }

        val timeScore = when (difficulty.lowercase(Locale.ROOT)) {
            "easy" -> 600
            "medium" -> 1200
            "hard" -> 1800
            else -> 600 // Default to Easy if unknown
        }

        val mistakePenalty = maxOf(0.4, 1 - (mistakesCount * 0.1))
        val basePoints = 1000

        return ((difficultyMultiplier * mistakePenalty) * (maxOf(0, (timeScore - elapsedTimeSeconds) * 2) + basePoints)).toInt()
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
            is GameAction.AutofillNotes -> {
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
        this.mistakesCount = 0

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

    /**
     * Load game state from saved data
     */
    fun loadFromSavedState(savedState: SavedGameState) {
        this.difficulty = savedState.difficulty
        this.elapsedTimeSeconds = savedState.elapsedTimeSeconds
        this.mistakesCount = savedState.mistakesCount
        this.isGameActive = true
        this.isGameCompleted = false
        this.isPaused = false
        this.selectedCell = null
        this.selectedNumber = null
        this.gameMode = GameMode.NORMAL
        this.actionHistory.clear()
        this.errorCells = emptySet()

        // Convert saved grid to SudokuCell array
        this.grid = Array(9) { row ->
            Array(9) { col ->
                val saved = savedState.grid[row][col]
                SudokuCell(
                    value = saved.value,
                    notes = saved.notes.toSet(),
                    isOriginal = saved.isOriginal,
                    hasError = saved.hasError
                )
            }
        }

        // Convert solution grid
        this.solutionGrid = Array(9) { row ->
            IntArray(9) { col ->
                savedState.solutionGrid[row][col]
            }
        }
    }

    /**
     * Autofill all valid notes for empty cells
     */
    fun autofillNotes() {
        val newGrid = Array(9) { r -> Array(9) { c -> grid[r][c] } }
        val affectedCells = mutableListOf<Pair<Int, Int>>()
        val oldNotes = mutableListOf<Set<Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                val cell = grid[row][col]
                if (cell.isEmpty && !cell.isOriginal) {
                    val validNotes = getValidNotesForCell(row, col)
                    if (validNotes != cell.notes) {
                        affectedCells.add(Pair(row, col))
                        oldNotes.add(cell.notes)
                        newGrid[row][col] = cell.copy(notes = validNotes)
                    }
                }
            }
        }

        // Only update grid and add to history if there were changes
        if (affectedCells.isNotEmpty()) {
            // Create action for undo
            val action = GameAction.AutofillNotes(affectedCells, oldNotes)
            actionHistory.push(action)
            grid = newGrid
        }
    }

    /**
     * Get all valid notes for a cell based on current grid state
     */
    private fun getValidNotesForCell(row: Int, col: Int): Set<Int> {
        val validNotes = mutableSetOf<Int>()

        for (number in 1..9) {
            if (isValidPlacement(row, col, number)) {
                validNotes.add(number)
            }
        }

        return validNotes
    }

    /**
     * Check if placing a number at the given position would be valid
     */
    private fun isValidPlacement(row: Int, col: Int, number: Int): Boolean {
        // Check row
        for (c in 0..8) {
            if (grid[row][c].value == number) {
                return false
            }
        }

        // Check column
        for (r in 0..8) {
            if (grid[r][col].value == number) {
                return false
            }
        }

        // Check 3x3 box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (grid[r][c].value == number) {
                    return false
                }
            }
        }

        return true
    }
}
