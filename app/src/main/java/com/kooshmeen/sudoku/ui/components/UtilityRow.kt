/**
 * UtilityRow.kt
 * This file defines a composable function for displaying a row of utility buttons in the Sudoku app.
 * These buttons are, from left to right:
 * Erase: selecting it will enable erasing mode, clearing non-starting cells when clicked.
 * Notes: selecting it will toggle notes mode, allowing users to add notes to cells.
 * Undo: selecting it will undo the last action.
 */

package com.kooshmeen.sudoku.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kooshmeen.sudoku.R

@Composable
fun UtilityRow(
    modifier: Modifier = Modifier,
    selectedButton: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Erase Button
        IconToggleButton(
            checked = selectedButton == "erase",
            onCheckedChange = { /* Handle erase mode */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon (
                painter = painterResource(id = R.drawable.ink_eraser_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                contentDescription = "Erase",
                tint = if (selectedButton == "erase") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
        // Notes Button
        IconToggleButton(
            checked = selectedButton == "notes",
            onCheckedChange = { /* Handle notes mode */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon (
                painter = painterResource(id = R.drawable.add_notes_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                contentDescription = "Notes",
                tint = if (selectedButton == "notes") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
        // Undo Button
        IconButton(
            onClick = { /* Handle undo action */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon (
                painter = painterResource(id = R.drawable.undo_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                contentDescription = "Undo",
                tint = if (selectedButton == "undo") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview (showBackground = true)
@Composable
fun UtilityRowPreview() {
    UtilityRow(
        selectedButton = null,
        modifier = Modifier.fillMaxSize()
    )
}