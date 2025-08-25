/**
 * Group Members screen for viewing members of a specific group
 * Shows all group members with their roles and join dates
 */

package com.kooshmeen.sudoku.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.data.api.GroupData
import com.kooshmeen.sudoku.data.api.GroupMember
import com.kooshmeen.sudoku.repository.SudokuRepository
import com.kooshmeen.sudoku.ui.components.PendingChallengeCard
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    groupId: Int,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToGame: (difficulty: String, challengeId: Int) -> Unit = { _, _ -> }
) {
    var groupData by remember { mutableStateOf<GroupData?>(null) }
    var members by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showChallengeDialog by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    var showPendingChallengeCard by remember { mutableStateOf(false) }
    var pendingMatchId by remember { mutableStateOf<Int?>(null) }
    var challengedPlayerName by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()

    // Load group data and members
    LaunchedEffect(groupId) {
        isLoading = true
        errorMessage = null

        scope.launch {
            // First get group details
            val groupResult = repository.getGroupDetails(groupId)
            groupResult.fold(
                onSuccess = { group ->
                    groupData = group.group
                    // Then get group members
                    val membersResult = repository.getGroupMembers(groupId)
                    membersResult.fold(
                        onSuccess = { membersList ->
                            members = membersList.sortedWith(
                                compareBy<GroupMember> { member ->
                                    when ((member.role ?: "member").lowercase()) {
                                        "leader" -> 0
                                        else -> 1
                                    }
                                }.thenBy { it.username ?: "Unknown User" }
                            )
                            isLoading = false
                        },
                        onFailure = { exception ->
                            errorMessage = exception.message ?: "Failed to load group members"
                            isLoading = false
                        }
                    )
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Failed to load group details"
                    isLoading = false
                }
            )
        }
    }

    groupData?.let { group ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with back button and title
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = group.group_name ?: "Unkown Group Name", // Add null safety
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Group info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Members",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${members.size} members",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        group.group_description?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Error Message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    members.isEmpty() && errorMessage == null -> {
                        EmptyMembersMessage(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(members) { member ->
                                MemberCard(
                                    member = member,
                                    isCurrentUser = repository.fetchCurrentUser()?.id == member.id,
                                    onChallengeClick = if (member.player_id != repository.fetchCurrentUser()?.id) {
                                        {
                                            showChallengeDialog = true
                                            selectedMemberId = member.player_id
                                            Log.d("GroupMembersScreen", "Selected member for challenge: ${member.username} with ID ${member.player_id}")
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    } ?: run {
        // Show loading or error state when group data is not loaded
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading group details...")
            } else {
                errorMessage?.let { message ->
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
    if (showChallengeDialog && selectedMemberId != null) {
        ChallengeDialog(
            onDismiss = {
                showChallengeDialog = false
                selectedMemberId = null
                challengedPlayerName = null
            },
            onChallengeCreated = { difficulty, type, matchId ->
                if (type == "offline") {
                    // Handle offline challenge creation - this is called after the challenge is already created in ChallengeDialog
                    showChallengeDialog = false
                    selectedMemberId = null
                    challengedPlayerName = null

                    // For offline challenges, we need to check if challenger needs to play first
                    matchId?.let { challengeId ->
                        Log.d("GroupMembersScreen", "Starting offline challenge game for challenger with ID: $challengeId")
                        onNavigateToGame(difficulty, challengeId)
                    }
                } else if (type == "online") {
                    // For live challenges, the challenge has already been created in ChallengeDialog
                    // Just show the pending challenge card with the returned matchId
                    showPendingChallengeCard = true
                    pendingMatchId = matchId
                    challengedPlayerName = members.find { it.player_id == selectedMemberId }?.username
                    showChallengeDialog = false
                    selectedMemberId = null
                }
            },
            repository = repository,
            groupId = groupId,
            selectedMemberId = selectedMemberId,
            challengedPlayerName = members.find { it.player_id == selectedMemberId }?.username
        )
    }

    // Show PendingChallengeCard if the state is set
    if (showPendingChallengeCard && pendingMatchId != null && challengedPlayerName != null) {
        PendingChallengeCard(
            challengerName = challengedPlayerName!!,
            matchId = pendingMatchId!!,
            onCancelChallenge = { matchId ->
                // Handle live match cancellation - return the actual result
                val result = repository.cancelLiveMatch(matchId)
                result.fold(
                    onSuccess = {
                        Log.d("GroupMembersScreen", "Live match cancelled successfully")
                    },
                    onFailure = { exception ->
                        Log.e("GroupMembersScreen", "Failed to cancel live match: ${exception.message}")
                        errorMessage = "Failed to cancel challenge: ${exception.message}"
                    }
                )
                result
            },
            onDismiss = {
                showPendingChallengeCard = false
                pendingMatchId = null
                challengedPlayerName = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun MemberCard(
    member: GroupMember,
    isCurrentUser: Boolean,
    onChallengeClick: (() -> Unit)? = null // Add challenge click listener
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when ((member.role ?: "member").lowercase()) { // Add null safety
                            "leader" -> Color(0xFFFFD700) // Gold for leader
                            else -> MaterialTheme.colorScheme.primary // Default for member
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when ((member.role ?: "member").lowercase()) { // Add null safety
                        "leader" -> Icons.Default.Star
                        else -> Icons.Default.Person
                    },
                    contentDescription = member.role ?: "Member",
                    tint = when ((member.role ?: "member").lowercase()) { // Add null safety
                        "leader" -> Color.Black
                        else -> MaterialTheme.colorScheme.onPrimary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Member info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.username ?: "Unknown User", // Add null safety
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "YOU",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Role badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when ((member.role ?: "member").lowercase()) { // Add null safety
                                "leader" -> Color(0xFFFFD700).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (member.role ?: "Member").replaceFirstChar { // Add null safety
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when ((member.role ?: "member").lowercase()) { // Add null safety
                            "leader" -> Color(0xFFB8860B)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                // Join date
                member.joined_at?.let { joinDate ->
                    if (joinDate.isNotBlank()) { // Add additional check for empty strings
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Joined ${formatJoinDate(joinDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // W/D/L Statistics
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wins
                    StatsBadge(
                        label = "W",
                        value = member.wins ?: 0,
                        backgroundColor = Color(0xFF4CAF50), // Green
                        textColor = Color.White
                    )

                    // Draws
                    StatsBadge(
                        label = "D",
                        value = member.draws ?: 0,
                        backgroundColor = Color(0xFFFFC107), // Yellow
                        textColor = Color.Black
                    )

                    // Losses
                    StatsBadge(
                        label = "L",
                        value = member.losses ?: 0,
                        backgroundColor = Color(0xFFF44336), // Red
                        textColor = Color.White
                    )
                }
            }

            // Challenge button (only show if not current user)
            onChallengeClick?.let { challengeClick ->
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = challengeClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Challenge Player",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsBadge(
    label: String,
    value: Int,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}

@Preview
@Composable
fun MemberCardPreview() {
    SudokuTheme {
        MemberCard(
            member = GroupMember(
                id = 1,
                group_id = 1,
                player_id = 1,
                username = "JohnDoe",
                role = "leader",
                joined_at = "2023-10-01T12:00:00.000Z"
            ),
            isCurrentUser = true
        )
    }
}

@Composable
private fun EmptyMembersMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.GroupOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Members Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "This group doesn't have any members yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ChallengeDialog(
    onDismiss: () -> Unit,
    onChallengeCreated: (String, String, Int?) -> Unit,
    repository: SudokuRepository? = null,
    groupId: Int = 0,
    selectedMemberId: Int? = null,
    challengedPlayerName: String? = null
) {
    var selectedDifficulty by remember { mutableStateOf("medium") }
    var selectedType by remember { mutableStateOf("offline") }
    var isCreatingChallenge by remember { mutableStateOf(false) }
    var isPendingLiveChallenge by remember { mutableStateOf(false) }
    var pendingMatchId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isPendingLiveChallenge) "Live Challenge Pending"
                else "Create Challenge"
            )
        },
        text = {
            if (isPendingLiveChallenge) {
                // Show pending challenge UI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Waiting for ${challengedPlayerName ?: "opponent"} to accept...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Show challenge creation UI
                Column {
                    Text("Select challenge type:")
                    Spacer(modifier = Modifier.height(12.dp))

                    // Challenge type selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("offline", "online").forEach { type ->
                            FilterChip(
                                onClick = { selectedType = type },
                                label = {
                                    Text(
                                        when(type) {
                                            "offline" -> "Offline"
                                            "online" -> "Live"
                                            else -> type
                                        }
                                    )
                                },
                                selected = selectedType == type,
                                enabled = !isCreatingChallenge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Select difficulty:")
                    Spacer(modifier = Modifier.height(8.dp))

                    // Difficulty selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("easy", "medium", "hard").forEach { difficulty ->
                            FilterChip(
                                onClick = { selectedDifficulty = difficulty },
                                label = { Text(difficulty.uppercase()) },
                                selected = selectedDifficulty == difficulty,
                                enabled = !isCreatingChallenge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Explanation text
                    Text(
                        text = when(selectedType) {
                            "offline" -> "Offline: You play first, then your opponent gets the same puzzle with your time to beat."
                            "online" -> "Live: Both players start the same puzzle simultaneously when accepted."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isPendingLiveChallenge) {
                // Cancel live challenge button
                OutlinedButton(
                    onClick = {
                        pendingMatchId?.let { matchId ->
                            scope.launch {
                                repository?.let { repo ->
                                    val result = repo.cancelLiveMatch(matchId)
                                    result.fold(
                                        onSuccess = {
                                            onDismiss()
                                        },
                                        onFailure = { exception ->
                                            errorMessage = exception.message ?: "Failed to cancel challenge"
                                        }
                                    )
                                }
                            }
                        } ?: onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Challenge")
                }
            } else {
                // Create challenge button
                Button(
                    onClick = {
                        if (repository != null && selectedMemberId != null) {
                            // Handle both live and offline challenge creation
                            isCreatingChallenge = true
                            errorMessage = null

                            scope.launch {
                                Log.d("ChallengeDialog", "Creating $selectedType challenge against player ID $selectedMemberId with difficulty $selectedDifficulty in group $groupId")
                                val result = repository.createChallenge(
                                    challengedId = selectedMemberId,
                                    groupId = groupId,
                                    difficulty = selectedDifficulty,
                                    challengeType = selectedType
                                )

                                result.fold(
                                    onSuccess = { response ->
                                        if (selectedType == "online") {
                                            // For live challenges, pass the matchId
                                            response.matchId?.let { matchId ->
                                                onChallengeCreated(selectedDifficulty, selectedType, matchId)
                                            }
                                        } else {
                                            // For offline challenges, pass the challengeId
                                            onChallengeCreated(selectedDifficulty, selectedType, response.challengeId)
                                        }
                                    },
                                    onFailure = { exception ->
                                        errorMessage = exception.message ?: "Failed to create challenge"
                                        isCreatingChallenge = false
                                    }
                                )
                            }
                        } else {
                            // Fallback - should not normally happen
                            onChallengeCreated(selectedDifficulty, selectedType, null)
                        }
                    },
                    enabled = !isCreatingChallenge
                ) {
                    if (isCreatingChallenge) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating...")
                    } else {
                        Text("Create Challenge")
                    }
                }
            }
        },
        dismissButton = {
            if (!isPendingLiveChallenge) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isCreatingChallenge
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

private fun formatJoinDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e2: Exception) {
            "Recently"
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupMembersScreenPreview() {
    SudokuTheme {
        GroupMembersScreen(
            groupId = 1
        )
    }
}
