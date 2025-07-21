/*
* Main Menu Screen for the Sudoku Game - entry point
* Settings button and dark/light mode toggle in upper right corner
* Play button in the center, with select difficulty dropdown below it
*
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
fun MainMenu(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToGame: () -> Unit = { /* Default no-op */ }
) {
    // Main menu content goes here
    // This is a placeholder for the actual implementation
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text("Main Menu Screen")

        // Temporary button to navigate to the game screen
        Button(
            onClick = onNavigateToGame,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Play Game")
        }
    }
}

@Preview
@Composable
fun MainMenuPreview() {
    MainMenu(
        modifier = Modifier,
        isDarkTheme = false, // Preview with light theme
        onThemeToggle = {}
    )
}