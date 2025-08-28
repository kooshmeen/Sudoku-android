/**
 * Sudoku Grid Component
 * Displays a 9x9 grid with bold borders every 3 cells to separate 3x3 boxes.
 * Manages cell interactions and visual feedback.
 */

package com.kooshmeen.sudoku.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.kooshmeen.sudoku.data.SudokuCell
import com.kooshmeen.sudoku.data.GameState

data class CellPosition(val row: Int, val col: Int)

enum class DragMode {
    ADD, REMOVE
}

@Composable
fun SudokuGrid(
    grid: Array<Array<SudokuCell>>, // Change from Array<IntArray> to Array<Array<SudokuCell>>
    selectedCell: Pair<Int, Int>? = null,
    selectedNumber: Int? = null, // Add selectedNumber
    onCellClick: (Int, Int) -> Unit = { _, _ -> },
    onNoteToggle: ((Int, Int, Int) -> Unit)? = null, // New parameter for note toggling
    gameMode: GameState.GameMode = GameState.GameMode.NORMAL, // New parameter for game mode
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val gridSize = remember { mutableStateOf(Size.Zero) }
    val visitedCells = remember { mutableStateOf(mutableSetOf<CellPosition>()) }
    val dragMode = remember { mutableStateOf<DragMode?>(null) }

    fun coordinateToCell(offset: Offset): CellPosition? {
        if (gridSize.value == Size.Zero) return null

        val cellWidth = gridSize.value.width / 9
        val cellHeight = gridSize.value.height / 9

        val col = (offset.x / cellWidth).toInt().coerceIn(0, 8)
        val row = (offset.y / cellHeight).toInt().coerceIn(0, 8)

        return CellPosition(row, col)
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .onSizeChanged { size ->
                gridSize.value = size.toSize()
            }
            .pointerInput(selectedNumber, gameMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (gameMode == GameState.GameMode.NOTES && selectedNumber != null && onNoteToggle != null) {
                            val startCell = coordinateToCell(offset)
                            if (startCell != null) {
                                visitedCells.value.clear()

                                val currentCell = grid[startCell.row][startCell.col]
                                // Only allow drag on empty cells
                                if (!currentCell.isFilled && !currentCell.isOriginal) {
                                    // Determine if we're adding or removing notes
                                    val startCellHasNote = currentCell.notes.contains(selectedNumber)
                                    dragMode.value = if (startCellHasNote) DragMode.REMOVE else DragMode.ADD

                                    // Apply to starting cell immediately
                                    onNoteToggle(startCell.row, startCell.col, selectedNumber)
                                    visitedCells.value.add(startCell)
                                }
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        if (gameMode == GameState.GameMode.NOTES && selectedNumber != null && onNoteToggle != null && dragMode.value != null) {
                            val currentCell = coordinateToCell(change.position)

                            // Only toggle if we haven't visited this cell yet
                            if (currentCell != null && !visitedCells.value.contains(currentCell)) {
                                val cell = grid[currentCell.row][currentCell.col]
                                // Only allow drag on empty cells
                                if (!cell.isFilled && !cell.isOriginal) {
                                    // Apply the same action (add/remove) determined at start
                                    val cellHasNote = cell.notes.contains(selectedNumber)
                                    when (dragMode.value) {
                                        DragMode.ADD -> {
                                            if (!cellHasNote) {
                                                onNoteToggle(currentCell.row, currentCell.col, selectedNumber)
                                            }
                                        }
                                        DragMode.REMOVE -> {
                                            if (cellHasNote) {
                                                onNoteToggle(currentCell.row, currentCell.col, selectedNumber)
                                            }
                                        }
                                        null -> { /* Do nothing */ }
                                    }
                                    visitedCells.value.add(currentCell)
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        visitedCells.value.clear()
                        dragMode.value = null
                    }
                )
            }
    ) {
        repeat(9) { row ->
            Row {
                repeat(9) { col ->
                    val cell = grid[row][col]
                    val isHighlighted = selectedNumber != null && cell.value == selectedNumber
                    SudokuCell(
                        value = cell.value,
                        notes = cell.notes,
                        isSelected = selectedCell == Pair(row, col),
                        isOriginal = cell.isOriginal,
                        selectedNumber = selectedNumber, // Pass selectedNumber here
                        hasError = cell.hasError,
                        isHighlighted = isHighlighted, // Pass highlight flag
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier
                            .weight(1f)
                            .then(getBoxBorderModifier(row, col))
                    )
                }
            }
        }
    }
}

@Composable
private fun getBoxBorderModifier(row: Int, col: Int): Modifier {
    val borderColor = MaterialTheme.colorScheme.outline // Extract color here

    return Modifier.drawBehind {
        val thinStroke = 1.dp.toPx()
        val thickStroke = 4.dp.toPx()

        // Draw thick top border for rows 3 and 6
        if (row == 3 || row == 6) {
            drawLine(
                color = borderColor, // Use the extracted color
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = thickStroke
            )
        }

        // Draw thick left border for columns 3 and 6
        if (col == 3 || col == 6) {
            drawLine(
                color = borderColor, // Use the extracted color
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = thickStroke
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SudokuGridPreview() {
    val sampleGrid = Array(9) { Array(9) { SudokuCell() } }
    sampleGrid[0][0] = SudokuCell(value = 5)
    sampleGrid[1][1] = SudokuCell(value = 3)
    sampleGrid[4][4] = SudokuCell(value = 7)
    sampleGrid[3][0] = SudokuCell(value = 5, hasError = true)
    sampleGrid[0][1] = SudokuCell(notes = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9))

    SudokuGrid(
        grid = sampleGrid,
        selectedCell = Pair(0, 0),
        onCellClick = { row, col -> println("Cell clicked: ($row, $col)") },
        selectedNumber = 4,
        modifier = Modifier.fillMaxSize()
    )
}
