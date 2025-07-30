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
            val puzzle = generator.createPuzzle(completeGrid, difficulty)
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
     * Create a puzzle by removing numbers from the complete grid
     */
    private fun createPuzzle(completeGrid: Array<IntArray>, difficulty: String): Array<Array<SudokuCell>> {
        val puzzle = Array(GRID_SIZE) { row ->
            Array(GRID_SIZE) { col ->
                SudokuCell(
                    value = completeGrid[row][col],
                    isOriginal = true
                )
            }
        }

        val cellsToRemove = when (difficulty.lowercase()) {
            "easy" -> EASY_CELLS_TO_REMOVE
            "medium" -> MEDIUM_CELLS_TO_REMOVE
            "hard" -> HARD_CELLS_TO_REMOVE
            else -> EASY_CELLS_TO_REMOVE
        }

        // Get all cell positions and shuffle them
        val allPositions = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                allPositions.add(Pair(row, col))
            }
        }
        allPositions.shuffle(Random.Default)

        // Remove numbers from random positions
        var removed = 0
        for (position in allPositions) {
            if (removed >= cellsToRemove) break

            val (row, col) = position
            puzzle[row][col] = SudokuCell(
                value = 0,
                isOriginal = false
            )
            removed++
        }

        return puzzle
    }
}