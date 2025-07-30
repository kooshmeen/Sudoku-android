/**
 * Individual Sudoku Cell Component
 * Displays either a large number (if filled) or small note numbers (if empty with notes).
 * Handles visual states like selection and borders for 3x3 box separation.
 */

package com.kooshmeen.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SudokuCell(
    value: Int, // 0 if empty
    notes: Set<Int> = emptySet(), // Notes 1-9 for empty cells
    isSelected: Boolean = false,
    isOriginal: Boolean = false, // New parameter to identify original puzzle cells
    hasError: Boolean = false, // Not used here, but can be added for error state
    isHighlighted: Boolean = false, // New parameter for highlight
    selectedNumber: Int? = null, // Pass selectedNumber
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val borderColor = MaterialTheme.colorScheme.outline
    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val highlightColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(0.5.dp, borderColor)
            .background(
                when {
                    isSelected -> selectedColor
                    isHighlighted -> highlightColor
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            // Display large number for filled cells
            Text(
                text = value.toString(),
                fontSize = 30.sp,
                fontWeight = if (isOriginal) FontWeight.ExtraBold else FontWeight.Bold,
                color = when {
                    hasError -> MaterialTheme.colorScheme.error
                    isOriginal -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        } else if (notes.isNotEmpty()) {
            NotesGrid(notes = notes, selectedNumber = selectedNumber)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>, selectedNumber: Int?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp), // Explicit zero padding
        verticalArrangement = Arrangement.SpaceAround
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier
                    .fillMaxHeight(
                        when (row) {
                            0 -> 0.8f/3f
                            1 -> 0.8f/2f
                            else -> 1f
                        }
                    )
                    .padding(0.dp), // Explicit zero padding
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                repeat(3) { col ->
                    val number = row * 3 + col + 1
                    val isBold = selectedNumber == number
                    Box(
                        modifier = Modifier

                            .fillMaxWidth(
                                when (col) {
                                    0 -> 1f/3f
                                    1 -> 1f/2f
                                    else -> 1f
                                }
                            )
                            .padding(0.dp), // Explicit zero padding
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            text = if (notes.contains(number)) number.toString() else "",
                            fontSize = 11.sp,
                            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isBold) MaterialTheme.colorScheme.onSurface.copy(alpha = 1f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(0.dp),
                            // make text persistent, i.e., don't truncate or wrap
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                        )
                    }
                }
            }
        }
    }
}

// Preview for filled cell (with value = 5)
@Preview(showBackground = true)
@Composable
fun SudokuCellPreviewFilled() {
    SudokuCell(
        value = 5,
        isSelected = true,
        modifier = Modifier.padding(0.dp)
    )
}

// Preview for empty cell with notes (notes = {1, 2, 3, 5, 7})
@Preview(showBackground = true)
@Composable
fun SudokuCellPreviewEmptyWithNotes() {
    SudokuCell(
        value = 0,
        notes = setOf(1, 2, 3, 5, 7),
        selectedNumber = 5,
        isSelected = false,
        modifier = Modifier.padding(0.dp)
    )
}
