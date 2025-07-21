/*
* Game Screen for the Sudoku Game
* This screen displays the Sudoku grid and allows users to play the game.
**/

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kooshmeen.sudoku.ui.theme.SudokuTheme

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToMenu: () -> Unit = { /* Default no-op */ }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background) // Use MaterialTheme for background color
    ) {
        // Temporary button to navigate back to the main menu
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
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
        }
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
