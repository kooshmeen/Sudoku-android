package com.kooshmeen.sudoku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.data.GameStateManager
import com.kooshmeen.sudoku.repository.SudokuRepository
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PendingChallengeCard(
    challengerName: String,
    matchId: Int,
    onCancelChallenge: suspend (Int) -> Result<*>,
    onDismiss: () -> Unit,
    onNavigateToGame: (String, Int?, Int?) -> Unit = { _, _, _ -> }, // difficulty, challengeId, liveMatchId
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var matchStatus by remember { mutableStateOf("pending") }
    var isGeneratingPuzzle by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Waiting for $challengerName to accept...") }

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()

    // Polling logic for match status
    LaunchedEffect(matchId) {
        while (matchStatus == "pending" || matchStatus == "accepted") {
            delay(1000) // Poll every second
            scope.launch {
                val result = repository.getLiveMatchStatus(matchId)
                result.fold(
                    onSuccess = { liveMatchStatus ->
                        val newStatus = liveMatchStatus.status
                        if (newStatus != matchStatus) {
                            matchStatus = newStatus

                            when (newStatus) {
                                "accepted" -> {
                                    // Challenger needs to generate and upload puzzle
                                    statusMessage = "Match accepted! Generating puzzle..."
                                    isGeneratingPuzzle = true

                                    // Generate puzzle using GameStateManager
                                    val difficulty = liveMatchStatus.difficulty
                                    GameStateManager.startNewGame(difficulty, context)

                                    // Create puzzle data from the generated game
                                    val puzzleData = mapOf(
                                        "puzzle" to GameStateManager.gameState.grid.flatMap { row ->
                                            row.map { cell -> if (cell.isOriginal) cell.value else 0 }
                                        },
                                        "solution" to GameStateManager.gameState.solutionGrid.flatMap { it.toList() },
                                        "difficulty" to difficulty
                                    )

                                    // Upload puzzle to activate the match
                                    val uploadResult = repository.startLiveMatch(matchId, puzzleData)
                                    uploadResult.fold(
                                        onSuccess = {
                                            statusMessage = "Puzzle uploaded! Starting game..."
                                            isGeneratingPuzzle = false
                                            delay(1000) // Brief pause before navigation
                                            onNavigateToGame(difficulty, null, matchId)
                                            onDismiss()
                                        },
                                        onFailure = { exception ->
                                            errorMessage = "Failed to upload puzzle: ${exception.message}"
                                            isGeneratingPuzzle = false
                                        }
                                    )
                                }
                                "active" -> {
                                    // Match is active, navigate to game
                                    statusMessage = "Match is active! Starting game..."
                                    val difficulty = liveMatchStatus.difficulty
                                    onNavigateToGame(difficulty, null, matchId)
                                    onDismiss()
                                }
                                "cancelled" -> {
                                    statusMessage = "Match was cancelled"
                                    onDismiss()
                                }
                            }
                        }
                    },
                    onFailure = { exception ->
                        errorMessage = "Polling error: ${exception.message}"
                    }
                )
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header text
            Text(
                text = "Live Challenge Pending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status message
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = if (isGeneratingPuzzle) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error message if any
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cancel button (only show if not generating puzzle)
            if (!isGeneratingPuzzle) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            val result = onCancelChallenge(matchId)
                            result.fold(
                                onSuccess = {
                                    onDismiss() // Dismiss the card on successful cancellation
                                },
                                onFailure = { exception ->
                                    errorMessage = exception.message ?: "Failed to cancel challenge"
                                }
                            )
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoading) "Cancelling..." else "Cancel Challenge")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PendingChallengeCardPreview() {
    SudokuTheme {
        PendingChallengeCard(
            challengerName = "JohnDoe",
            matchId = 123,
            onCancelChallenge = { Result.success("Cancelled") },
            onDismiss = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}
