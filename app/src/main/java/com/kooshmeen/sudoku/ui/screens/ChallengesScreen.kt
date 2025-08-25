/**
 * Challenges screen for viewing and managing pending challenge invitations
 * Shows received challenges that can be accepted or declined
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kooshmeen.sudoku.data.api.ChallengeInvitation
import com.kooshmeen.sudoku.data.api.LiveMatch
import com.kooshmeen.sudoku.repository.SudokuRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sealed class to represent both challenge invitations and live match invitations
 */
sealed class PendingInvitation {
    data class Challenge(val invitation: ChallengeInvitation) : PendingInvitation()
    data class LiveMatch(val match: com.kooshmeen.sudoku.data.api.LiveMatch) : PendingInvitation()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToGame: (difficulty: String, challengeId: Int?) -> Unit = { _, _ -> }
) {
    var pendingInvitations by remember { mutableStateOf<List<PendingInvitation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()

    // Load both pending challenges and live matches
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null

            // Fetch both challenges and live matches concurrently
            val challengesResult = repository.getPendingChallenges()
            val liveMatchesResult = repository.getPendingLiveMatches()

            val allInvitations = mutableListOf<PendingInvitation>()

            challengesResult.fold(
                onSuccess = { challenges ->
                    allInvitations.addAll(challenges.map { PendingInvitation.Challenge(it) })
                },
                onFailure = { exception ->
                    errorMessage = "Failed to load challenges: ${exception.message}"
                }
            )

            liveMatchesResult.fold(
                onSuccess = { matches ->
                    allInvitations.addAll(matches.map { PendingInvitation.LiveMatch(it) })
                },
                onFailure = { exception ->
                    // Don't overwrite challenge error if it exists
                    if (errorMessage == null) {
                        errorMessage = "Failed to load live matches: ${exception.message}"
                    }
                }
            )

            pendingInvitations = allInvitations
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        TopAppBar(
            title = { Text("Challenge Invitations") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Content
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                pendingInvitations.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No pending challenges or live matches",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingInvitations) { invitation ->
                            when (invitation) {
                                is PendingInvitation.Challenge -> {
                                    ChallengeCard(
                                        challenge = invitation.invitation,
                                        onAcceptChallenge = { challengeId ->
                                            scope.launch {
                                                val result = repository.acceptChallenge(challengeId)
                                                result.fold(
                                                    onSuccess = {
                                                        onNavigateToGame(invitation.invitation.difficulty, challengeId)
                                                    },
                                                    onFailure = { exception ->
                                                        errorMessage = exception.message
                                                    }
                                                )
                                            }
                                        },
                                        onRejectChallenge = { challengeId ->
                                            scope.launch {
                                                val result = repository.rejectChallenge(challengeId)
                                                result.fold(
                                                    onSuccess = {
                                                        // Remove from list
                                                        pendingInvitations = pendingInvitations.filter {
                                                            when (it) {
                                                                is PendingInvitation.Challenge -> it.invitation.id != challengeId
                                                                else -> true
                                                            }
                                                        }
                                                    },
                                                    onFailure = { exception ->
                                                        errorMessage = exception.message
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                                is PendingInvitation.LiveMatch -> {
                                    LiveMatchCard(
                                        liveMatch = invitation.match,
                                        onAcceptMatch = { matchId ->
                                            scope.launch {
                                                val result = repository.acceptLiveMatch(matchId)
                                                result.fold(
                                                    onSuccess = {
                                                        // Navigate to live game
                                                        onNavigateToGame(invitation.match.difficulty, matchId)
                                                    },
                                                    onFailure = { exception ->
                                                        errorMessage = exception.message
                                                    }
                                                )
                                            }
                                        },
                                        onRejectMatch = { matchId ->
                                            scope.launch {
                                                val result = repository.cancelLiveMatch(matchId)
                                                result.fold(
                                                    onSuccess = {
                                                        // Remove from list
                                                        pendingInvitations = pendingInvitations.filter {
                                                            when (it) {
                                                                is PendingInvitation.LiveMatch -> it.match.id != matchId
                                                                else -> true
                                                            }
                                                        }
                                                    },
                                                    onFailure = { exception ->
                                                        errorMessage = exception.message
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: ChallengeInvitation,
    onAcceptChallenge: (Int) -> Unit,
    onRejectChallenge: (Int) -> Unit = { _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Challenge from ${challenge.challenger_name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "in ${challenge.group_name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Difficulty badge
                Surface(
                    color = when (challenge.difficulty.lowercase()) {
                        "easy" -> MaterialTheme.colorScheme.tertiary
                        "medium" -> MaterialTheme.colorScheme.secondary
                        "hard" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = challenge.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Challenge details
            if (challenge.challenger_time != null) {
                Text(
                    text = "Challenger completed in ${challenge.challenger_time}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Timestamp
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(challenge.created_at.replace("Z", ""))

            Text(
                text = "Challenged ${dateFormat.format(date ?: Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Accept button
            Button(
                onClick = { onAcceptChallenge(challenge.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Accept Challenge")
            }
            // Reject button
            OutlinedButton(
                onClick = { onRejectChallenge(challenge.id) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reject")
            }
        }
    }
}

@Composable
private fun LiveMatchCard(
    liveMatch: LiveMatch,
    onAcceptMatch: (Int) -> Unit,
    onRejectMatch: (Int) -> Unit = { _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Match with ${liveMatch.challenger_name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "in ${liveMatch.group_name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Difficulty badge
                Surface(
                    color = when (liveMatch.difficulty.lowercase()) {
                        "easy" -> MaterialTheme.colorScheme.tertiary
                        "medium" -> MaterialTheme.colorScheme.secondary
                        "hard" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = liveMatch.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Match details
            Text(
                text = "Join the match and compete live!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Timestamp
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(liveMatch.created_at.replace("Z", ""))

            Text(
                text = "Challenged ${dateFormat.format(date ?: Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Accept button
            Button(
                onClick = { onAcceptMatch(liveMatch.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Join Live Match")
            }
            // Reject button
            OutlinedButton(
                onClick = { onRejectMatch(liveMatch.id) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Decline")
            }
        }
    }
}
