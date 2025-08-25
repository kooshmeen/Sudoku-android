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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kooshmeen.sudoku.data.GameStateManager
import com.kooshmeen.sudoku.ui.screens.AuthScreen
import com.kooshmeen.sudoku.ui.screens.ChallengeResultScreen
import com.kooshmeen.sudoku.ui.screens.ChallengesScreen
import com.kooshmeen.sudoku.ui.screens.GameScreen
import com.kooshmeen.sudoku.ui.screens.GroupMembersScreen
import com.kooshmeen.sudoku.ui.screens.GroupsScreen
import com.kooshmeen.sudoku.ui.screens.LeaderboardScreen
import com.kooshmeen.sudoku.ui.screens.LiveMatchResultScreen
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
                                    NavController.navigate("game/medium")
                                },
                                onContinueGame = {
                                    NavController.navigate("game/medium")
                                },
                                onStartNewGame = { difficulty ->
                                    NavController.navigate("game/$difficulty")
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
                                onNavigateToChallenges = {
                                    NavController.navigate("challenges_screen")
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
                composable(
                    "game/{difficulty}?challengeId={challengeId}&liveMatchId={liveMatchId}&challengeRole={challengeRole}",
                    arguments = listOf(
                        navArgument("difficulty") { type = NavType.StringType },
                        navArgument("challengeId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("liveMatchId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("challengeRole") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    SudokuTheme(darkTheme = isDarkTheme) {
                        val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "medium"
                        val challengeIdString = backStackEntry.arguments?.getString("challengeId")
                        val challengeId = challengeIdString?.takeIf { it != "null" }?.toIntOrNull()
                        val liveMatchIdString = backStackEntry.arguments?.getString("liveMatchId")
                        val liveMatchId = liveMatchIdString?.takeIf { it != "null" }?.toIntOrNull()
                        val challengeRole = backStackEntry.arguments?.getString("challengeRole")?.takeIf { it != "null" }

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            GameScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                onThemeToggle = { isDarkTheme = it },
                                isDarkTheme = isDarkTheme,
                                onNavigateToMenu = {
                                    NavController.navigateUp()
                                },
                                onNavigateToChallengeResult = { challengeId, timeSeconds, mistakes ->
                                    NavController.navigate("challenge_result/$challengeId/$timeSeconds/$mistakes")
                                },
                                onNavigateToLiveMatchResult = { liveMatchId, timeSeconds, mistakes ->
                                    NavController.navigate("live_match_result/$liveMatchId/$timeSeconds/$mistakes")
                                },
                                challengeId = challengeId,
                                liveMatchId = liveMatchId,
                                difficulty = difficulty,
                                challengeRole = challengeRole,
                                isLiveMatch = liveMatchId != null
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
                                    onNavigateToGame = { difficulty, challengeId ->
                                        // Navigate to game with challenge context for challenger
                                        NavController.navigate("game/$difficulty?challengeId=$challengeId&challengeRole=challenger")
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                )
                            }
                        }
                    }
                }
                composable("challenges_screen") {
                    SudokuTheme(darkTheme = isDarkTheme) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            ChallengesScreen(
                                onNavigateBack = {
                                    NavController.navigateUp()
                                },
                                onNavigateToGame = { difficulty, challengeId, liveMatchId ->
                                    // Navigate to game with appropriate context
                                    when {
                                        challengeId != null -> {
                                            // Regular challenge - challenged player
                                            NavController.navigate("game/$difficulty?challengeId=$challengeId&challengeRole=challenged")
                                        }
                                        liveMatchId != null -> {
                                            // Live match
                                            NavController.navigate("game/$difficulty?liveMatchId=$liveMatchId")
                                        }
                                        else -> {
                                            // Fallback to regular game
                                            NavController.navigate("game/$difficulty")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
                composable("challenge_result/{challengeId}/{timeSeconds}/{mistakes}") { backStackEntry ->
                    SudokuTheme(darkTheme = isDarkTheme) {
                        val challengeId = backStackEntry.arguments?.getString("challengeId")?.toIntOrNull() ?: 0
                        val timeSeconds = backStackEntry.arguments?.getString("timeSeconds")?.toIntOrNull() ?: 0
                        val mistakes = backStackEntry.arguments?.getString("mistakes")?.toIntOrNull() ?: 0

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            ChallengeResultScreen(
                                challengeId = challengeId,
                                timeSeconds = timeSeconds,
                                mistakes = mistakes,
                                onNavigateBack = {
                                    // Navigate back to challenges or groups
                                    NavController.popBackStack("challenges_screen", false)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
                composable("live_match_result/{matchId}/{timeSeconds}/{mistakes}") { backStackEntry ->
                    SudokuTheme(darkTheme = isDarkTheme) {
                        val matchId = backStackEntry.arguments?.getString("matchId")?.toIntOrNull() ?: 0
                        val timeSeconds = backStackEntry.arguments?.getString("timeSeconds")?.toIntOrNull() ?: 0
                        val mistakes = backStackEntry.arguments?.getString("mistakes")?.toIntOrNull() ?: 0

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            LiveMatchResultScreen(
                                matchId = matchId,
                                timeSeconds = timeSeconds,
                                mistakes = mistakes,
                                onNavigateBack = {
                                    // Navigate back to challenges or groups
                                    NavController.popBackStack("challenges_screen", false)
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

    override fun onPause() {
        super.onPause()
        // Save game state when app goes to background
        GameStateManager.saveCurrentGame(this)
    }
}