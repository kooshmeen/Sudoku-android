/**
 * MainActivity.kt
 * This file defines the main entry point for the Sudoku app.
 */

package com.kooshmeen.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kooshmeen.sudoku.data.GameStateManager
import com.kooshmeen.sudoku.data.api.GroupData
import com.kooshmeen.sudoku.ui.screens.AuthScreen
import com.kooshmeen.sudoku.ui.screens.GameScreen
import com.kooshmeen.sudoku.ui.screens.GroupMembersScreen
import com.kooshmeen.sudoku.ui.screens.GroupsScreen
import com.kooshmeen.sudoku.ui.screens.LeaderboardScreen
import com.kooshmeen.sudoku.ui.screens.MainMenu
import com.kooshmeen.sudoku.ui.screens.RecordScreen
import com.kooshmeen.sudoku.ui.screens.SettingsScreen
import com.kooshmeen.sudoku.ui.theme.SudokuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val NavController = rememberNavController()
            var isDarkTheme by remember { mutableStateOf(false) }

            NavHost(
                navController = NavController,
                startDestination = "main_menu",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("main_menu") {
                    SudokuTheme (darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            MainMenu(
                                onNavigateToGame = {
                                    NavController.navigate("game_screen")
                                },
                                onContinueGame = {
                                    NavController.navigate("game_screen")
                                },
                                onStartNewGame = { difficulty ->
                                    NavController.navigate("game_screen")
                                },
                                onNavigateToRecords = {
                                    NavController.navigate("record_screen")
                                },
                                onNavigateToAuth = {
                                    NavController.navigate("auth_screen")
                                },
                                onNavigateToLeaderboard = {
                                    NavController.navigate("leaderboard_screen")
                                },
                                onNavigateToSettings = {
                                    NavController.navigate("settings_screen")
                                },
                                onNavigateToGroups = {
                                    NavController.navigate("groups_screen")
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = it }
                            )
                        }
                    }
                }
                composable("auth_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            AuthScreen(
                                onNavigateBack = {
                                    NavController.navigateUp()
                                },
                                onLoginSuccess = {
                                    NavController.navigate("main_menu") {
                                        popUpTo("main_menu") { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
                composable("game_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            GameScreen(
                                onNavigateToMenu = {
                                    NavController.navigateUp()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                            )
                        }
                    }
                }
                composable("record_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            RecordScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                onNavigateBack = {
                                    NavController.navigateUp()
                                }
                            )
                        }
                    }
                }
                composable("leaderboard_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            LeaderboardScreen(
                                onNavigateBack = {
                                    NavController.navigateUp()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
                composable("settings_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            SettingsScreen(
                                onNavigateBack = {
                                    NavController.navigateUp()
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = it }
                            )
                        }
                    }
                }
                composable("groups_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            GroupsScreen(
                                onNavigateBack = {
                                    NavController.navigateUp()
                                },
                                onNavigateToGroupLeaderboard = { groupId ->
                                    // Navigate to group leaderboard screen when implemented
                                    // NavController.navigate("group_leaderboard/$groupId")
                                },
                                onNavigateToGroupMembers = { group ->
                                    // Pass only the group ID as a navigation argument
                                    group.id?.let { groupId ->
                                        NavController.navigate("group_members_screen/$groupId")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
                composable("group_members_screen/{groupId}") { backStackEntry ->
                    SudokuTheme(darkTheme = isDarkTheme) {
                        val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull()

                        if (groupId != null) {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                GroupMembersScreen(
                                    groupId = groupId,
                                    onNavigateBack = {
                                        NavController.navigateUp()
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Save game state when app goes to background
        GameStateManager.saveCurrentGame(this)
    }
}