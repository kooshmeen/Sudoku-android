/**
 * SudokuGenerator.kt
 * Generates a Sudoku puzzle by randomly filling the 3x3 grids on the diagonal
 * then filling the rest of the grid using backtracking.
 * Then strategically removes numbers depending on selected difficulty.
 */

package com.kooshmeen.sudoku.data

import kotlin.random.Random

class SudokuGenerator {
    companion object {
        private const val GRID_SIZE = 9
        private const val BOX_SIZE = 3

        // Difficulty settings - number of cells to remove
        private const val EASY_CELLS_TO_REMOVE = 40
        private const val MEDIUM_CELLS_TO_REMOVE = 50
        private const val HARD_CELLS_TO_REMOVE = 60

        /**
         * Generate a complete Sudoku puzzle with given difficulty
         */
        fun generatePuzzle(difficulty: String): Array<Array<SudokuCell>> {
            val generator = SudokuGenerator()
            val completeGrid = generator.generateCompleteGrid()
            val puzzle = generator.createPuzzleWithUniquenessCheck(completeGrid, difficulty)
            return puzzle
        }
    }

    private val grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) }

    /**
     * Generate a complete valid Sudoku grid
     */
    private fun generateCompleteGrid(): Array<IntArray> {
        // Fill diagonal 3x3 boxes first (they don't depend on each other)
        fillDiagonalBoxes()

        // Fill remaining cells using backtracking
        fillRemaining(0, BOX_SIZE)

        return grid.map { it.clone() }.toTypedArray()
    }

    /**
     * Fill the three diagonal 3x3 boxes
     */
    private fun fillDiagonalBoxes() {
        for (i in 0 until GRID_SIZE step BOX_SIZE) {
            fillBox(i, i)
        }
    }

    /**
     * Fill a 3x3 box starting at (row, col)
     */
    private fun fillBox(row: Int, col: Int) {
        val numbers = (1..9).shuffled(Random.Default)
        var index = 0

        for (i in 0 until BOX_SIZE) {
            for (j in 0 until BOX_SIZE) {
                grid[row + i][col + j] = numbers[index++]
            }
        }
    }

    /**
     * Fill remaining cells using backtracking
     */
    private fun fillRemaining(row: Int, col: Int): Boolean {
        var currentRow = row
        var currentCol = col

        // Move to next cell if we've reached the end of current row
        if (currentCol >= GRID_SIZE) {
            currentRow += 1
            currentCol = 0
        }

        // If we've filled all rows, we're done
        if (currentRow >= GRID_SIZE) {
            return true
        }

        // Skip cells that are already filled (diagonal boxes)
        if (grid[currentRow][currentCol] != 0) {
            return fillRemaining(currentRow, currentCol + 1)
        }

        // Try numbers 1-9 in random order
        val numbers = (1..9).shuffled(Random.Default)

        for (num in numbers) {
            if (isSafe(currentRow, currentCol, num)) {
                grid[currentRow][currentCol] = num

                if (fillRemaining(currentRow, currentCol + 1)) {
                    return true
                }

                // Backtrack
                grid[currentRow][currentCol] = 0
            }
        }

        return false
    }

    /**
     * Check if it's safe to place a number at given position
     */
    private fun isSafe(row: Int, col: Int, num: Int): Boolean {
        return !usedInRow(row, num) &&
               !usedInCol(col, num) &&
               !usedInBox(row - row % BOX_SIZE, col - col % BOX_SIZE, num)
    }

    /**
     * Check if number is used in the row
     */
    private fun usedInRow(row: Int, num: Int): Boolean {
        for (col in 0 until GRID_SIZE) {
            if (grid[row][col] == num) {
                return true
            }
        }
        return false
    }

    /**
     * Check if number is used in the column
     */
    private fun usedInCol(col: Int, num: Int): Boolean {
        for (row in 0 until GRID_SIZE) {
            if (grid[row][col] == num) {
                return true
            }
        }
        return false
    }

    /**
     * Check if number is used in the 3x3 box
     */
    private fun usedInBox(boxStartRow: Int, boxStartCol: Int, num: Int): Boolean {
        for (row in 0 until BOX_SIZE) {
            for (col in 0 until BOX_SIZE) {
                if (grid[boxStartRow + row][boxStartCol + col] == num) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if the given puzzle has a unique solution
     */
    private fun hasUniqueSolution(puzzle: Array<Array<SudokuCell>>): Boolean {
        // Convert to IntArray for easier processing
        val grid = Array(GRID_SIZE) { row -> IntArray(GRID_SIZE) { col -> puzzle[row][col].value } }
        var solutionCount = 0
        fun solve(row: Int, col: Int): Boolean {
            if (row == GRID_SIZE) {
                solutionCount++
                return solutionCount > 1 // Stop if more than one solution found
            }
            val nextRow = if (col == GRID_SIZE - 1) row + 1 else row
            val nextCol = if (col == GRID_SIZE - 1) 0 else col + 1
            if (grid[row][col] != 0) {
                return solve(nextRow, nextCol)
            }
            for (num in 1..9) {
                if (isSafeForGrid(grid, row, col, num)) {
                    grid[row][col] = num
                    if (solve(nextRow, nextCol)) {
                        grid[row][col] = 0
                        return true // Early exit if more than one solution
                    }
                    grid[row][col] = 0
                }
            }
            return false
        }
        solve(0, 0)
        return solutionCount == 1
    }

    private fun isSafeForGrid(grid: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (c in 0 until GRID_SIZE) if (grid[row][c] == num) return false
        for (r in 0 until GRID_SIZE) if (grid[r][col] == num) return false
        val boxRow = (row / BOX_SIZE) * BOX_SIZE
        val boxCol = (col / BOX_SIZE) * BOX_SIZE
        for (r in boxRow until boxRow + BOX_SIZE) {
            for (c in boxCol until boxCol + BOX_SIZE) {
                if (grid[r][c] == num) return false
            }
        }
        return true
    }

    /**
     * Create a puzzle by removing cells, checking for uniqueness after each removal
     */
    private fun createPuzzleWithUniquenessCheck(completeGrid: Array<IntArray>, difficulty: String): Array<Array<SudokuCell>> {
        val puzzle = Array(GRID_SIZE) { row ->
            Array(GRID_SIZE) { col ->
                SudokuCell(
                    value = completeGrid[row][col],
                    isOriginal = true
                )
            }
        }
        val positions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                positions.add(Pair(row, col))
            }
        }
        positions.shuffle(Random.Default)
        var removed = 0
        val targetRemoved = when (difficulty.lowercase()) {
            "easy" -> EASY_CELLS_TO_REMOVE
            "medium" -> MEDIUM_CELLS_TO_REMOVE
            "hard" -> HARD_CELLS_TO_REMOVE
            else -> EASY_CELLS_TO_REMOVE
        }
        for (position in positions) {
            if (removed >= targetRemoved) break
            val (row, col) = position
            val originalValue = puzzle[row][col].value
            puzzle[row][col] = SudokuCell(value = 0, isOriginal = false)
            if (hasUniqueSolution(puzzle)) {
                removed++
            } else {
                puzzle[row][col] = SudokuCell(value = originalValue, isOriginal = true)
            }
        }

        return puzzle
    }
}