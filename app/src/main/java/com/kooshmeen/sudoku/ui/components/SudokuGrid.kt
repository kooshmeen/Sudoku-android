/**
 * Sudoku Grid Component
 * Displays a 9x9 grid with bold borders every 3 cells to separate 3x3 boxes.
 * Manages cell interactions and visual feedback.
 */

package com.kooshmeen.sudoku.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.data.SudokuCell

@Composable
fun SudokuGrid(
    grid: Array<Array<SudokuCell>>, // Change from Array<IntArray> to Array<Array<SudokuCell>>
    selectedCell: Pair<Int, Int>? = null,
    onCellClick: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.outline)
    ) {
        repeat(9) { row ->
            Row {
                repeat(9) { col ->
                    val cell = grid[row][col]
                    SudokuCell(
                        value = cell.value,
                        notes = cell.notes,
                        isSelected = selectedCell == Pair(row, col),
                        hasError = cell.hasError,
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
    sampleGrid[0][1] = SudokuCell(notes = setOf(1, 2, 3))

    SudokuGrid(
        grid = sampleGrid,
        selectedCell = Pair(0, 0),
        onCellClick = { row, col -> println("Cell clicked: ($row, $col)") },
        modifier = Modifier.fillMaxSize()
    )
}