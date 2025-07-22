package com.kooshmeen.sudoku.utils

object SudokuValidator {

    /**
     * Checks if placing a number at the given position would create a conflict
     */
    fun isValidMove(grid: Array<IntArray>, row: Int, col: Int, number: Int): Boolean {
        return isValidInRow(grid, row, number) &&
                isValidInColumn(grid, col, number) &&
                isValidInBox(grid, row, col, number)
    }

    /**
     * Checks if number already exists in the row
     */
    private fun isValidInRow(grid: Array<IntArray>, row: Int, number: Int): Boolean {
        for (col in 0..8) {
            if (grid[row][col] == number) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if number already exists in the column
     */
    private fun isValidInColumn(grid: Array<IntArray>, col: Int, number: Int): Boolean {
        for (row in 0..8) {
            if (grid[row][col] == number) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if number already exists in the 3x3 box
     */
    private fun isValidInBox(grid: Array<IntArray>, row: Int, col: Int, number: Int): Boolean {
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3

        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (grid[r][c] == number) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Gets all conflicting cells for a number at a position
     */
    fun getConflictingCells(grid: Array<IntArray>, row: Int, col: Int, number: Int): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()

        // Check row conflicts
        for (c in 0..8) {
            if (c != col && grid[row][c] == number) {
                conflicts.add(Pair(row, c))
            }
        }

        // Check column conflicts
        for (r in 0..8) {
            if (r != row && grid[r][col] == number) {
                conflicts.add(Pair(r, col))
            }
        }

        // Check box conflicts
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3

        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && grid[r][c] == number) {
                    conflicts.add(Pair(r, c))
                }
            }
        }

        return conflicts
    }
}