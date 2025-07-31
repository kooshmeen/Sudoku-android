/**
 * MainMenu.kt
 * This file defines the main menu screen for the Sudoku app.
 * It includes options to start a game, select difficulty, and toggle themes.
 * It also includes a settings button.
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.R
import com.kooshmeen.sudoku.data.GameStateManager
import com.kooshmeen.sudoku.ui.theme.SudokuTheme

@Composable
fun MainMenu (
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToGame: () -> Unit = { /* Default no-op */ },
    onContinueGame: () -> Unit = { /* Default no-op */ },
    onStartNewGame: (String) -> Unit = { /* Default no-op */ },
    onNavigateToRecords: () -> Unit = { /* Default no-op */ },
    onNavigateToLeaderboard: () -> Unit = { /* Default no-op */ }
) {
    var isDifficultyDropdownOpen by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf("Easy") }
    val hasActiveGame = GameStateManager.hasActiveGame()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconToggleButton(
                checked = isDarkTheme,
                onCheckedChange = onThemeToggle,  // Use the callback
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (!isDarkTheme) R.drawable.light_mode_24dp_e3e3e3_fill0_wght400_grad0_opsz24
                        else R.drawable.dark_mode_24dp_e3e3e3_fill0_wght400_grad0_opsz24
                    ),
                    contentDescription = "Toggle Dark/Light Mode",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* TODO: Open Settings */ }) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                        .fillMaxSize()
                )
            }
        }

        // Title
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Welcome to Sudoku!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(256.dp))

        // Continue game button - only show if there's an active game
        if (hasActiveGame) {
            Button(
                onClick = {
                    GameStateManager.continueGame()
                    onContinueGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Continue Game",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Start new game button
        Button(
            onClick = {
                GameStateManager.startNewGame(selectedDifficulty)
                onStartNewGame(selectedDifficulty)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (hasActiveGame) "New Game" else "Start Game",
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Select difficulty
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedButton(
                onClick = { isDifficultyDropdownOpen = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Difficulty: $selectedDifficulty",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_drop_down),
                        contentDescription = "Dropdown arrow",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            DropdownMenu(
                expanded = isDifficultyDropdownOpen,
                onDismissRequest = { isDifficultyDropdownOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Easy") },
                    onClick = {
                        selectedDifficulty = "Easy"
                        isDifficultyDropdownOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Medium") },
                    onClick = {
                        selectedDifficulty = "Medium"
                        isDifficultyDropdownOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hard") },
                    onClick = {
                        selectedDifficulty = "Hard"
                        isDifficultyDropdownOpen = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(128.dp))

        // Row for Leaderboard button (left) and Records button (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                onClick = { /* TODO: Navigate to Leaderboard */ },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Leaderboard",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp)) // Space between buttons
            Button(
                onClick = { onNavigateToRecords() },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Records",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    var isDarkTheme by remember { mutableStateOf(false) }

    SudokuTheme(darkTheme = isDarkTheme) {
        MainMenu(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = it }
        )
    }
}
