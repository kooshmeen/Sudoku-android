package com.kooshmeen.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import com.kooshmeen.sudoku.ui.screens.MainMenuScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Add theme state here
            var isDarkTheme by remember { mutableStateOf(false) }

            SudokuTheme(darkTheme = isDarkTheme) {  // Pass the state to theme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainMenuScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it }  // Update the state
                    )
                }
            }
        }
    }
}