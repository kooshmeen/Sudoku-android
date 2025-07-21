package com.kooshmeen.sudoku.ui.screens


import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import com.kooshmeen.sudoku.R

@Composable
fun MainMenu (
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToGame: () -> Unit = { /* Default no-op */ }
) {
    var isDifficultyDropdownOpen by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf("Easy") }

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
        // Play button - start game if no game started
        Button(
            onClick = { onNavigateToGame() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Start Game",
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
