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
import com.kooshmeen.sudoku.ui.screens.GameScreen
import com.kooshmeen.sudoku.ui.screens.MainMenu
import com.kooshmeen.sudoku.ui.screens.RecordScreen
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = it }
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
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Save game state when app goes to background
        GameStateManager.saveCurrentGame(this)
    }
}