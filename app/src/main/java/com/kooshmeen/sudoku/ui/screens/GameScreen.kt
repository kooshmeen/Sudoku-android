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

import android.util.Log
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.data.GameState
import com.kooshmeen.sudoku.data.GameStateManager
import com.kooshmeen.sudoku.repository.SudokuRepository
import com.kooshmeen.sudoku.ui.components.InputRow
import com.kooshmeen.sudoku.ui.components.SudokuGrid
import com.kooshmeen.sudoku.ui.components.UtilityRow
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import com.kooshmeen.sudoku.utils.BestTimeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.flatten
import kotlin.collections.toList

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit = { /* Default no-op */ },
    isDarkTheme: Boolean = true, // Default value for dark theme
    onNavigateToMenu: () -> Unit = { /* Default no-op */ },
    onNavigateToChallengeResult: (Int, Int, Int) -> Unit = { _, _, _ -> }, // challengeId, timeSeconds, mistakes
    onNavigateToLiveMatchResult: (Int, Int, Int) -> Unit = { _, _, _ -> }, // liveMatchId, timeSeconds, mistakes
    challengeId: Int? = null, // Optional challenge ID for challenges mode
    liveMatchId: Int? = null, // Optional live match ID for live matches
    difficulty: String? = null, // Optional difficulty override for challenges
    challengeRole: String? = null, // "challenger" or "challenged" to distinguish roles
    isLiveMatch: Boolean = false // Flag to indicate if this is a live match
) {
    val gameState = GameStateManager.gameState
    var showCompletionDialog by remember { mutableStateOf(false) }

    var opponentTime by remember { mutableStateOf<Int?>(null) }
    var isChallenge by remember { mutableStateOf(challengeId != null) }
    var challengeData by remember { mutableStateOf<Map<String, Any>?>(null) }

    // Live match specific state variables
    var opponentFinished by remember { mutableStateOf(false) }
    var isPolling by remember { mutableStateOf(isLiveMatch) }

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()

    // Live match polling logic
    LaunchedEffect(isLiveMatch, liveMatchId, isPolling) {
        gameState.resetGameCompletion()
        if (isLiveMatch && liveMatchId != null && isPolling) {
            while (isPolling) {
                delay(1000) // Poll every second
                scope.launch {
                    val result = repository.getLiveMatchStatus(liveMatchId)
                    result.fold(
                        onSuccess = { matchStatus ->
                            // Update opponent status based on match data
                            val currentUserId = repository.fetchCurrentUser()?.id
                            if (currentUserId == matchStatus.challenger_id) {
                                // Current user is challenger, check challenged status
                                opponentFinished = matchStatus.challenged_finished
                                if (matchStatus.challenged_time != null) {
                                    opponentTime = matchStatus.challenged_time
                                }
                            } else {
                                // Current user is challenged, check challenger status
                                opponentFinished = matchStatus.challenger_finished
                                if (matchStatus.challenger_time != null) {
                                    opponentTime = matchStatus.challenger_time
                                }
                            }

                            // Stop polling if match is completed
                            if (matchStatus.status == "completed" || matchStatus.status == "results_ready") {
                                isPolling = false
                            }
                        },
                        onFailure = { exception ->
                            // Handle polling error - maybe reduce frequency or stop
                            println("Live match polling error: ${exception.message}")
                        }
                    )
                }
            }
        }
    }

    // Load challenge data if this is a challenge game
    LaunchedEffect(challengeId, difficulty) {
        gameState.resetGameCompletion()
        challengeId?.let { id ->
            isChallenge = true

            // Check if this is accepting an existing challenge (challenged player)
            // or creating a new puzzle for offline challenge (challenger)
            scope.launch {
                val result = repository.getChallengeData(id)
                result.fold(
                    onSuccess = { data ->
                        challengeData = data
                        opponentTime = (data["challengerTime"] as? Number)?.toInt()

                        // If puzzle data exists, load that specific puzzle
                        val puzzleData = data["puzzleData"] as? Map<*, *>
                        if (puzzleData != null) {
                            // Load the exact same puzzle the challenger played
                            GameStateManager.loadChallengeGame(puzzleData, difficulty ?: "medium", context)
                        } else {
                            // This is the challenger creating a new puzzle
                            difficulty?.let { diff ->
                                GameStateManager.startNewGame(diff, context)
                            }
                        }
                    },
                    onFailure = { exception ->
                        // If no challenge data exists, this is a new challenge being created
                        difficulty?.let { diff ->
                            GameStateManager.startNewGame(diff, context)
                        }
                    }
                )
            }
        }
    }

    // Load live match data if this is a live match
    LaunchedEffect(liveMatchId, isLiveMatch) {
        gameState.resetGameCompletion()
        if (isLiveMatch && liveMatchId != null) {
            scope.launch {
                val result = repository.getLiveMatchStatus(liveMatchId)
                result.fold(
                    onSuccess = { matchStatus ->
                        // Parse the puzzle data from JSON string
                        try {
                            val puzzleDataString = matchStatus.puzzle_data
                            if (puzzleDataString.isNotEmpty()) {
                                // Parse the JSON string to extract puzzle data
                                // The JSON contains: {"puzzle":[...], "solution":[...], "difficulty":"..."}
                                val gson = com.google.gson.Gson()
                                val puzzleData = gson.fromJson(puzzleDataString, Map::class.java) as Map<String, Any>
                                GameStateManager.loadChallengeGame(puzzleData, matchStatus.difficulty, context)
                            } else {
                                // Fallback to creating new game
                                difficulty?.let { diff ->
                                    GameStateManager.startNewGame(diff, context)
                                }
                            }
                        } catch (e: Exception) {
                            // Handle JSON parsing error
                            println("Error parsing puzzle data: ${e.message}")
                            difficulty?.let { diff ->
                                GameStateManager.startNewGame(diff, context)
                            }
                        }
                    },
                    onFailure = { exception ->
                        // Handle error loading live match data
                        difficulty?.let { diff ->
                            GameStateManager.startNewGame(diff, context)
                        }
                    }
                )
            }
        }
    }

    // Timer effect - only run when game is active and not paused
    LaunchedEffect(gameState.isGameActive, gameState.isPaused) {
        while (gameState.isGameActive && !gameState.isPaused) {
            delay(1000)
            gameState.updateTimer()
        }
    }

    // Check for game completion
    LaunchedEffect(gameState.isGameCompleted) {
        if (gameState.isGameCompleted) {
            showCompletionDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.background) // Use MaterialTheme for background color
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Timer with color logic for live matches
            Text(
                text = "Elapsed: ${gameState.getFormattedTime()}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
                color = if (isLiveMatch) {
                    if (opponentFinished) MaterialTheme.colorScheme.error // Red when opponent finished
                    else MaterialTheme.colorScheme.primary // Blue when opponent hasn't finished
                } else MaterialTheme.colorScheme.onSurface // Default color for non-live matches
            )

            // Show opponent info for both challenges and live matches
            if ((isChallenge || isLiveMatch) && opponentTime != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = null,
                        tint = if (isLiveMatch && opponentFinished)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = formatTime(opponentTime!!),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiveMatch && opponentFinished)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (isLiveMatch) "Live Opponent" else "Opponent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
        Spacer(Modifier.height(8.dp))

        // Display number of mistakes and highest possible score
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mistakes: ${gameState.mistakesCount}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.weight(1f)) // Push the highest possible score to the end
            Text(
                text = "Max Score: ${gameState.highestPossibleScore()}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        // Grid - show overlay when paused
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            SudokuGrid(
                grid = gameState.grid,
                selectedCell = null,
                selectedNumber = gameState.selectedNumber, // Pass selectedNumber
                onCellClick = { row, col ->
                    gameState.inputToCell(row, col)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Pause overlay
            if (gameState.isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 1f)),
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

        // Compute which numbers are disabled (already filled in all spots)
        val numberCounts = IntArray(9) { 0 }
        for (row in gameState.grid) {
            for (cell in row) {
                if (cell.value in 1..9) {
                    numberCounts[cell.value - 1]++
                }
            }
        }
        val disabledNumbers = numberCounts.mapIndexed { idx, count -> if (count >= 9) { idx + 1 }
        else null }.filterNotNull()

        // If selected number is disabled, select the next available number
        if (gameState.selectedNumber != null && disabledNumbers.contains(gameState.selectedNumber)) {
            gameState.selectNextAvailableNumber()
        }

        // Input numbers row
        InputRow(
            modifier = Modifier.fillMaxWidth(),
            input = List(9) { it + 1 },
            onInputChange = { index, value ->
                val number = value.toInt()
                if (!disabledNumbers.contains(number)) {
                    gameState.selectNumber(number)
                }
            },
            selectedNumber = gameState.selectedNumber,
            disabledNumbers = disabledNumbers // Pass disabled numbers
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
            onUndoClick = { gameState.undo() },
            onAutofillNotesClick = { gameState.autofillNotes() }
        )

        // Completion Dialog
        if (showCompletionDialog) {
            // Check for new best time
            val (isNewBest, isNewBestNoMistakes) = BestTimeManager.setBestTime(
                context = LocalContext.current,
                difficulty = gameState.difficulty,
                time = gameState.getFormattedTime(),
                numMistakes = gameState.mistakesCount,
            )

            val context = LocalContext.current

            LaunchedEffect(Unit) {
                // Initialize repository if not done
                gameState.initializeRepository(context)

                // Record game completion in local statistics
                gameState.recordGameCompletion(context)

                // Try to submit score
                val submitted = gameState.submitScoreToServer()
                if (!submitted) { // off line mode or not connected to server
                    // Show a toast or some indication that score submission failed
                    // Toast.makeText(context, "Score submission failed. Please try again later.", Toast.LENGTH_SHORT).show()
                    // For now, we will just log it
                    println("Score submission failed. Please try again later.")
                }

                // log the isChallenge, isLiveMatch, challengeId, liveMatchId, challengeRole values
                Log.d("GameScreen", "isChallenge: $isChallenge, isLiveMatch: $isLiveMatch, challengeId: $challengeId, liveMatchId: $liveMatchId, challengeRole: $challengeRole")
                // challenge - handle offline challenges differently based on role
                if (isChallenge && challengeId != null) {
                    if (challengeRole == "challenger") {
                        // For offline challenges, the challenger needs to send puzzle data along with completion
                        val challengeResult = repository.completeChallengerGame(
                            challengeId = challengeId,
                            timeSeconds = gameState.elapsedTimeSeconds,
                            numberOfMistakes = gameState.mistakesCount,
                            puzzleData = mapOf<String, Any>(
                                "puzzle" to gameState.grid.flatMap { row ->
                                    row.map { cell -> if (cell.isOriginal) cell.value else 0 }
                                },
                                "solution" to gameState.solutionGrid.flatMap { it.toList() },
                                "difficulty" to gameState.difficulty
                            )
                        )
                        challengeResult.fold(
                            onSuccess = {
                                // Successfully submitted challenger's game completion
                                println("Challenger game completed successfully. Invitation sent to challenged player.")
                            },
                            onFailure = { exception ->
                                // Handle error submitting challenger's game
                                println("Error submitting challenger game: ${exception.message}")
                            }
                        )
                    } else if (challengeRole == "challenged") {
                        // For challenged player, complete the challenge and navigate to results
                        showCompletionDialog = false
                        GameStateManager.endGame()
                        onNavigateToChallengeResult(challengeId, gameState.elapsedTimeSeconds, gameState.mistakesCount)
                        return@LaunchedEffect
                    }
                } else if (isLiveMatch && liveMatchId != null) {
                    // log if the app has reached here
                    Log.d("GameScreen", "Submitting live match completion for matchId: $liveMatchId")
                    // Handle live match completion
                    val liveMatchResult = repository.completeLiveMatch(
                        matchId = liveMatchId,
                        timeSeconds = gameState.elapsedTimeSeconds,
                        mistakes = gameState.mistakesCount,
                        update = false
                    )
                    liveMatchResult.fold(
                        onSuccess = {
                            // Successfully submitted live match completion
                            println("Live match completed successfully.")
                            // Navigate to live match results
                            onNavigateToLiveMatchResult(liveMatchId, gameState.elapsedTimeSeconds, gameState.mistakesCount)
                        },
                        onFailure = { exception ->
                            // Handle error submitting live match completion
                            println("Error submitting live match completion: ${exception.message}")
                        }
                    )
                }
            }

            GameStateManager.endGame()
            AlertDialog(
                onDismissRequest = {
                    showCompletionDialog = false
                    GameStateManager.endGame()
                },
                title = { Text("Congratulations! Final score: ${gameState.highestPossibleScore()}") },
                text = {
                    if (isNewBest) {
                        Text("You completed the game in a new best time for ${gameState.difficulty}: ${gameState.getFormattedTime()}!")
                    } else {
                        Text("You completed the game in ${gameState.getFormattedTime()}! Your best time for ${gameState.difficulty} is ${BestTimeManager.getBestTimeFormatted(LocalContext.current, gameState.difficulty)}.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCompletionDialog = false
                            GameStateManager.endGame()
                            onNavigateToMenu()
                        }
                    ) {
                        Text("Back to Menu")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCompletionDialog = false
                            GameStateManager.endGame()
                        }
                    ) {
                        Text("Stay Here")
                    }
                }
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    var isDarkTheme by remember { mutableStateOf(false) }

    SudokuTheme(darkTheme = isDarkTheme) {
        GameScreen(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = it },

        )
    }
}
