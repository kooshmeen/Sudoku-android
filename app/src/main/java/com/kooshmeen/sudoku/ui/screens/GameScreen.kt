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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.data.GameState
import com.kooshmeen.sudoku.ui.components.InputRow
import com.kooshmeen.sudoku.ui.components.SudokuGrid
import com.kooshmeen.sudoku.ui.components.UtilityRow
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToMenu: () -> Unit = { /* Default no-op */ }
) {
    val gameState = remember { GameState() }

    // Timer effect
    LaunchedEffect(gameState.isPaused) {
        while (true) {
            delay(1000)
            gameState.updateTimer()
        }
    }

    // Initialize game on first load
    LaunchedEffect(Unit) {
        gameState.startNewGame("Easy")
    }

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
                text = "Elapsed: ${gameState.getFormattedTime()}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.weight(1f)) // Push the difficulty indicator to the end
            // Difficulty indicator
            Text(
                text = gameState.difficulty,
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
                onClick = { gameState.togglePause() },
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

        // Grid - show overlay when paused
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            SudokuGrid(
                grid = Array(9) { row ->
                    IntArray(9) { col -> gameState.grid[row][col].value }
                },
                notes = Array(9) { row ->
                    Array(9) { col -> gameState.grid[row][col].notes }
                },
                selectedCell = null, // No cell selection
                onCellClick = { row, col ->
                    gameState.inputToCell(row, col) // Direct input to cell
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Pause overlay
            if (gameState.isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Game Paused",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(96.dp))

        // Input numbers row
        InputRow(
            modifier = Modifier.fillMaxWidth(),
            input = List(9) { it + 1 },
            onInputChange = { index, value ->
                val number = value.toInt()
                gameState.selectNumber(number)
            },
            selectedNumber = gameState.selectedNumber
        )
        Spacer(Modifier.height(24.dp))
        // Utility Row
        UtilityRow(
            modifier = Modifier.fillMaxWidth(),
            selectedButton = when (gameState.gameMode) {
                GameState.GameMode.NOTES -> "notes"
                GameState.GameMode.ERASE -> "erase"
                else -> null
            },
            onEraseClick = { gameState.toggleEraseMode() },
            onNotesClick = { gameState.toggleNotesMode() },
            onUndoClick = { gameState.undo() }
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
