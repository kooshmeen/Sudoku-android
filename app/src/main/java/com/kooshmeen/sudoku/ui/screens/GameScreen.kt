/*
* Game Screen for the Sudoku Game
* This screen displays the Sudoku grid and allows users to play the game.
**/

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToMenu: () -> Unit = { /* Default no-op */ }
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background) // Use MaterialTheme for background color
    ) {
        Text("Game Screen")

        // Temporary button to navigate back to the main menu
        Button(
            onClick = onNavigateToMenu,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Back to Main Menu")
        }
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    GameScreen(
        modifier = Modifier.fillMaxSize(),
        isDarkTheme = false, // Preview with light theme
        onThemeToggle = {}
    )
}