/**
 * Game Screen for the Sudoku Game
 * This screen displays the Sudoku grid and allows users to play the game.
 * Includes a timer from the start of the game, a difficulty indicator,
 * a back button to return to the main menu, and a pause button - which pauses the timer
 * and covers the grid.
 *
 * The grid is displayed using a SudokuGrid composable, and the input numbers are handled by an InputRow composable.
 * Below is a UtilityRow composable.
*/

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.ui.components.InputRow
import com.kooshmeen.sudoku.ui.components.SudokuGrid
import com.kooshmeen.sudoku.ui.components.UtilityRow
import com.kooshmeen.sudoku.ui.theme.SudokuTheme

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToMenu: () -> Unit = { /* Default no-op */ }
) {
    var selectedNumber by remember { mutableStateOf<Int?>(null) } // Track selected number
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .background(MaterialTheme.colorScheme.background) // Use MaterialTheme for background color
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Timer
            Text(
                text = "Elapsed: 00:00",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.weight(1f)) // Push the difficulty indicator to the end
            // Difficulty indicator
            Text(
                text = "Easy",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 32.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // back to menu button
            IconButton(
                onClick = onNavigateToMenu,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Main Menu",
                    tint = MaterialTheme.colorScheme.onBackground // Use MaterialTheme for icon color
                )
            }
            Spacer(Modifier.weight(1f)) // Push the pause button to the end
            // Pause button - will pause timer, but cover the grid
            IconButton(
                onClick = { /* TODO: Implement pause functionality */ },
                modifier = Modifier.padding(8.dp),
                // align the icon to the end of the row
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Pause Game",
                )
            }
        }
        Spacer(Modifier.height(96.dp))
        // Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SudokuGrid(
                grid = Array(9) { IntArray(9) { 0 } }, // Placeholder empty grid
                notes = Array(9) { Array(9) { emptySet<Int>() } }, // Placeholder empty notes
                selectedCell = null, // No cell selected initially
                onCellClick = { row, col -> /* Handle cell click */ },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(96.dp))

        // Input numbers row
        InputRow(
            modifier = Modifier.fillMaxWidth(),
            input = List(9) { it + 1 },
            onInputChange = { index, value ->
                val number = value.toInt()
                selectedNumber = number
            },
            selectedNumber = selectedNumber
        )
        Spacer(Modifier.height(24.dp))
        // Utility Row
        UtilityRow(
            modifier = Modifier.fillMaxWidth(),
            selectedButton = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    var isDarkTheme by remember { mutableStateOf(false) }

    SudokuTheme(darkTheme = isDarkTheme) {
        GameScreen(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = it }
        )
    }
}
