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
import androidx.compose.foundation.layout.fillMaxSize
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val borderColor = MaterialTheme.colorScheme.outline
    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(0.5.dp, borderColor)
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable { onClick() }
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            // Display large number for filled cells
            Text(
                text = value.toString(),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else if (notes.isNotEmpty()) {
            // Display notes in 3x3 mini-grid
            NotesGrid(notes = notes)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { col ->
                    val number = row * 3 + col + 1
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notes.contains(number)) number.toString() else "",
                            fontSize = 7.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
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
        modifier = Modifier.padding(8.dp)
    )
}

// Preview for empty cell with notes (notes = {1, 2, 3, 5, 7})
@Preview(showBackground = true)
@Composable
fun SudokuCellPreviewEmptyWithNotes() {
    SudokuCell(
        value = 0,
        notes = setOf(1, 2, 3, 5, 7),
        isSelected = false,
        modifier = Modifier.padding(8.dp)
    )
}
