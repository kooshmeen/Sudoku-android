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
import kotlinx.coroutines.launch

@Composable
fun LiveMatchResultScreen(
    matchId: Int,
    timeSeconds: Int,
    mistakes: Int,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    var matchResult by remember { mutableStateOf<LiveMatchCompletionResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()

    // Submit live match completion and get results
    LaunchedEffect(matchId) {
        scope.launch {
            val result = repository.completeLiveMatch(matchId, timeSeconds, mistakes)
            result.fold(
                onSuccess = { response ->
                    matchResult = response
                    isLoading = false

                    // The backend should handle updating win/loss stats automatically
                    // when the live match is completed, but we could add additional
                    // client-side handling here if needed
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Failed to load live match result"
                    isLoading = false
                }
            )
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
                                    when (result.winner) {
                                        "challenged" -> MaterialTheme.colorScheme.primaryContainer
                                        "challenger" -> MaterialTheme.colorScheme.errorContainer
                                        "draw" -> MaterialTheme.colorScheme.secondaryContainer
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
                                        when (result.winner) {
                                            "challenged" -> Icons.Default.EmojiEvents
                                            "challenger" -> Icons.Default.ThumbDown
                                            "draw" -> Icons.Default.Balance
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
                                        when (result.winner) {
                                            "challenged" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "challenger" -> MaterialTheme.colorScheme.onErrorContainer
                                            "draw" -> MaterialTheme.colorScheme.onSecondaryContainer
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
                                        when (result.winner) {
                                            "challenged" -> "🎉 You Won! 🎉"
                                            "challenger" -> "You Lost"
                                            "draw" -> "It's a Draw!"
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
                                        when (result.winner) {
                                            "challenged" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "challenger" -> MaterialTheme.colorScheme.onErrorContainer
                                            "draw" -> MaterialTheme.colorScheme.onSecondaryContainer
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

                                // Your results
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

                                // Opponent results
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

                    Spacer(modifier = Modifier.weight(1f))

                    // Back button
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (result.status == "waiting_for_opponent") "Back to Challenges" else "Back to Challenges")
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
