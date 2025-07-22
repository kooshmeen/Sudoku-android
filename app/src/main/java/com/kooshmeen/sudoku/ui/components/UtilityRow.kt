/**
 * UtilityRow.kt
 * This file defines a composable function for displaying a row of utility buttons in the Sudoku app.
 * These buttons are, from left to right:
 * Erase: selecting it will enable erasing mode, clearing non-starting cells when clicked.
 * Notes: selecting it will toggle notes mode, allowing users to add notes to cells.
 * Undo: selecting it will undo the last action.
 */

package com.kooshmeen.sudoku.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UtilityRow(
    modifier: Modifier = Modifier,

) {

}

@Preview
@Composable
fun UtilityRowPreview() {
    UtilityRow()
}