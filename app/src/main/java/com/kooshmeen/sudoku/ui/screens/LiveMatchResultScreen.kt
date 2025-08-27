/**
 * Live Match result screen showing the outcome of a live 1v1 match
 * Displays both players' times, scores, and declares the winner
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.data.api.LiveMatchCompletionResponse
import com.kooshmeen.sudoku.repository.SudokuRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiveMatchResultScreen(
    matchId: Int,
    timeSeconds: Int,
    mistakes: Int,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToGroupMembers: () -> Unit = {},
    onNavigateToChallenges: () -> Unit = {}
) {
    var matchResult by remember { mutableStateOf<LiveMatchCompletionResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPolling by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf<String?>(null) } // Track if user is challenger or challenged

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()

    // Submit live match completion and get results
    LaunchedEffect(matchId) {
        scope.launch {
            // First, get the match details to determine current user's role
            val matchStatusResult = repository.getLiveMatchStatus(matchId)
            matchStatusResult.fold(
                onSuccess = { matchStatus ->
                    val currentUserId = repository.fetchCurrentUser()?.id
                    currentUserRole = when (currentUserId) {
                        matchStatus.challenger_id -> "challenger"
                        matchStatus.challenged_id -> "challenged"
                        else -> null
                    }
                },
                onFailure = { exception ->
                    println("Failed to get match status: ${exception.message}")
                }
            )

            val result = repository.completeLiveMatch(matchId, timeSeconds, mistakes)
            result.fold(
                onSuccess = { response ->
                    matchResult = response
                    isLoading = false

                    // If status is "waiting_for_opponent", start polling for final results
                    if (response.status == "waiting_for_opponent") {
                        isPolling = true
                    }
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Failed to load live match result"
                    isLoading = false
                }
            )
        }
    }

    // Polling logic for when waiting for opponent to finish
    LaunchedEffect(isPolling) {
        if (isPolling) {
            while (isPolling) {
                delay(1000) // Poll every second
                scope.launch {
                    val statusResult = repository.getLiveMatchStatus(matchId)
                    statusResult.fold(
                        onSuccess = { matchStatus ->
                            // Check if match is completed or results are ready
                            if (matchStatus.status == "completed" || matchStatus.status == "results_ready") {
                                // Re-submit completion to get final results
                                val finalResult = repository.completeLiveMatch(matchId, timeSeconds, mistakes)
                                finalResult.fold(
                                    onSuccess = { finalResponse ->
                                        if (finalResponse.status == "match_completed") {
                                            matchResult = finalResponse
                                            isPolling = false // Stop polling
                                        }
                                    },
                                    onFailure = { exception ->
                                        // Continue polling on error
                                        println("Error getting final results: ${exception.message}")
                                    }
                                )
                            }
                        },
                        onFailure = { exception ->
                            // Continue polling on error, but log it
                            println("Polling error: ${exception.message}")
                        }
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }

            matchResult != null -> {
                val result = matchResult!!

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Winner announcement
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (result.status) {
                                "match_completed" -> {
                                    when {
                                        result.winner == "draw" -> MaterialTheme.colorScheme.secondaryContainer
                                        result.winner == currentUserRole -> MaterialTheme.colorScheme.primaryContainer // User won
                                        result.winner != null && result.winner != currentUserRole -> MaterialTheme.colorScheme.errorContainer // User lost
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                }
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (result.status) {
                                    "match_completed" -> {
                                        when {
                                            result.winner == "draw" -> Icons.Default.Balance
                                            result.winner == currentUserRole -> Icons.Default.EmojiEvents // User won
                                            result.winner != null && result.winner != currentUserRole -> Icons.Default.ThumbDown // User lost
                                            else -> Icons.Default.Help
                                        }
                                    }
                                    "waiting_for_opponent" -> Icons.Default.AccessTime
                                    else -> Icons.Default.Help
                                },
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = when (result.status) {
                                    "match_completed" -> {
                                        when {
                                            result.winner == "draw" -> MaterialTheme.colorScheme.onSecondaryContainer
                                            result.winner == currentUserRole -> MaterialTheme.colorScheme.onPrimaryContainer // User won
                                            result.winner != null && result.winner != currentUserRole -> MaterialTheme.colorScheme.onErrorContainer // User lost
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    }
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = when (result.status) {
                                    "match_completed" -> {
                                        when {
                                            result.winner == "draw" -> "It's a Draw!"
                                            result.winner == currentUserRole -> "🎉 You Won! 🎉"
                                            result.winner != null && result.winner != currentUserRole -> "You Lost"
                                            else -> "Live Match Complete"
                                        }
                                    }
                                    "waiting_for_opponent" -> "Waiting for Opponent..."
                                    else -> "Live Match Complete"
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (result.status) {
                                    "match_completed" -> {
                                        when {
                                            result.winner == "draw" -> MaterialTheme.colorScheme.onSecondaryContainer
                                            result.winner == currentUserRole -> MaterialTheme.colorScheme.onPrimaryContainer
                                            result.winner != null && result.winner != currentUserRole -> MaterialTheme.colorScheme.onErrorContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    }
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            if (result.status == "waiting_for_opponent") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your opponent is still playing. Results will be available once they finish.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Results comparison (only show if match is completed)
                    if (result.status == "match_completed") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Live Match Results",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Show results based on current user's role
                                if (currentUserRole == "challenger") {
                                    // Current user is challenger
                                    ResultRow(
                                        label = "Your Time",
                                        value = formatTime(result.challengerTime ?: timeSeconds),
                                        isWinner = result.winner == "challenger"
                                    )
                                    ResultRow(
                                        label = "Your Score",
                                        value = result.challengerScore?.toString() ?: "N/A",
                                        isWinner = result.winner == "challenger"
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    ResultRow(
                                        label = "Opponent Time",
                                        value = formatTime(result.challengedTime ?: 0),
                                        isWinner = result.winner == "challenged"
                                    )
                                    ResultRow(
                                        label = "Opponent Score",
                                        value = result.challengedScore?.toString() ?: "N/A",
                                        isWinner = result.winner == "challenged"
                                    )
                                } else {
                                    // Current user is challenged
                                    ResultRow(
                                        label = "Your Time",
                                        value = formatTime(result.challengedTime ?: timeSeconds),
                                        isWinner = result.winner == "challenged"
                                    )
                                    ResultRow(
                                        label = "Your Score",
                                        value = result.challengedScore?.toString() ?: "N/A",
                                        isWinner = result.winner == "challenged"
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    ResultRow(
                                        label = "Opponent Time",
                                        value = formatTime(result.challengerTime ?: 0),
                                        isWinner = result.winner == "challenger"
                                    )
                                    ResultRow(
                                        label = "Opponent Score",
                                        value = result.challengerScore?.toString() ?: "N/A",
                                        isWinner = result.winner == "challenger"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Back button with role-based navigation
                    Button(
                        onClick = {
                            // Use the user's role to determine navigation
                            if (currentUserRole == "challenger") {
                                onNavigateToGroupMembers()
                            } else {
                                onNavigateToChallenges()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (currentUserRole == "challenger") "Back to Group Members"
                            else "Back to Challenges"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    isWinner: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWinner) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Winner",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                color = if (isWinner) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
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
fun PreviewLiveMatchResultScreen() {
    LiveMatchResultScreen(
        matchId = 1,
        timeSeconds = 123,
        mistakes = 2,
        onNavigateBack = {},
        modifier = Modifier
    )
}
