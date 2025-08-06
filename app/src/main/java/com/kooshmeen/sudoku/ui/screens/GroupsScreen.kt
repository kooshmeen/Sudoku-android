/**
 * Groups screen for managing Sudoku groups
 * Includes functionality to view, join, create, and manage groups
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.data.api.GroupData
import com.kooshmeen.sudoku.repository.SudokuRepository
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToGroupLeaderboard: (Int) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var allGroups by remember { mutableStateOf<List<GroupData>>(emptyList()) }
    var myGroups by remember { mutableStateOf<List<GroupData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showJoinGroupDialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<GroupData?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repository = remember { SudokuRepository(context) }
    val scope = rememberCoroutineScope()
    val isLoggedIn = repository.isLoggedIn()

    val tabTitles = if (isLoggedIn) listOf("All Groups", "My Groups") else listOf("All Groups")

    // Load data based on selected tab
    LaunchedEffect(selectedTabIndex, isLoggedIn) {
        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                when (selectedTabIndex) {
                    0 -> { // All Groups
                        val result = if (searchQuery.isBlank()) {
                            repository.getAllGroups()
                        } else {
                            repository.searchGroups(searchQuery)
                        }
                        result.fold(
                            onSuccess = { groups ->
                                allGroups = groups
                                isLoading = false
                            },
                            onFailure = { exception ->
                                errorMessage = exception.message ?: "Failed to load groups"
                                isLoading = false
                            }
                        )
                    }
                    1 -> { // My Groups (only if logged in)
                        if (isLoggedIn) {
                            val result = repository.getMyGroups()
                            result.fold(
                                onSuccess = { groups ->
                                    myGroups = groups
                                    isLoading = false
                                },
                                onFailure = { exception ->
                                    errorMessage = exception.message ?: "Failed to load my groups"
                                    isLoading = false
                                }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and title
        TopAppBar(
            title = {
                Text(
                    text = "Groups",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
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
            actions = {
                if (isLoggedIn) {
                    IconButton(onClick = { showCreateGroupDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Group",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Search bar for All Groups tab
        if (selectedTabIndex == 0) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isNotBlank()) {
                        isSearching = true
                        scope.launch {
                            val result = repository.searchGroups(it)
                            result.fold(
                                onSuccess = { groups ->
                                    allGroups = groups
                                    isSearching = false
                                },
                                onFailure = {
                                    isSearching = false
                                }
                            )
                        }
                    } else {
                        // Reload all groups when search is cleared
                        scope.launch {
                            val result = repository.getAllGroups()
                            result.fold(
                                onSuccess = { groups -> allGroups = groups },
                                onFailure = { /* Handle error */ }
                            )
                        }
                    }
                },
                label = { Text("Search Groups") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
        }

        // Tab Row
        if (isLoggedIn) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Error/Success Messages
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    IconButton(onClick = { errorMessage = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        successMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { successMessage = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                isLoading || isSearching -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                selectedTabIndex == 0 && allGroups.isEmpty() -> {
                    EmptyStateMessage(
                        title = if (searchQuery.isBlank()) "No Groups Available" else "No Groups Found",
                        subtitle = if (searchQuery.isBlank()) "There are no public groups to display"
                                  else "Try adjusting your search terms",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                selectedTabIndex == 1 && myGroups.isEmpty() -> {
                    EmptyStateMessage(
                        title = "No Groups Joined",
                        subtitle = "Join or create a group to get started",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    val groupsToShow = if (selectedTabIndex == 0) allGroups else myGroups

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(groupsToShow) { group ->
                            GroupCard(
                                group = group,
                                isLoggedIn = isLoggedIn,
                                isMyGroup = selectedTabIndex == 1,
                                onJoinClick = {
                                    selectedGroup = group
                                    showJoinGroupDialog = true
                                },
                                onLeaveClick = { groupId ->
                                    scope.launch {
                                        val result = repository.leaveGroup(groupId)
                                        result.fold(
                                            onSuccess = {
                                                successMessage = "Left group successfully"
                                                // Refresh my groups
                                                repository.getMyGroups().fold(
                                                    onSuccess = { myGroups = it },
                                                    onFailure = { }
                                                )
                                            },
                                            onFailure = {
                                                errorMessage = it.message ?: "Failed to leave group"
                                            }
                                        )
                                    }
                                },
                                onDeleteClick = { groupId ->
                                    scope.launch {
                                        val result = repository.deleteGroup(groupId)
                                        result.fold(
                                            onSuccess = {
                                                successMessage = "Group deleted successfully"
                                                // Refresh my groups
                                                repository.getMyGroups().fold(
                                                    onSuccess = { myGroups = it },
                                                    onFailure = { }
                                                )
                                            },
                                            onFailure = {
                                                errorMessage = it.message ?: "Failed to delete group"
                                            }
                                        )
                                    }
                                },
                                onLeaderboardClick = { groupId ->
                                    onNavigateToGroupLeaderboard(groupId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showCreateGroupDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateGroupDialog = false },
                onCreateGroup = { name, description, password ->
                    scope.launch {
                        val result = repository.createGroup(name, description, password)
                        result.fold(
                            onSuccess = {
                                successMessage = "Group created successfully"
                                showCreateGroupDialog = false
                                // Refresh groups
                                if (selectedTabIndex == 1) {
                                    repository.getMyGroups().fold(
                                        onSuccess = { myGroups = it },
                                        onFailure = { }
                                    )
                                } else {
                                    repository.getAllGroups().fold(
                                        onSuccess = { allGroups = it },
                                        onFailure = { }
                                    )
                                }
                            },
                            onFailure = {
                                errorMessage = it.message ?: "Failed to create group"
                            }
                        )
                    }
                }
            )
        }

        if (showJoinGroupDialog && selectedGroup != null) {
            JoinGroupDialog(
                group = selectedGroup!!,
                onDismiss = {
                    showJoinGroupDialog = false
                    selectedGroup = null
                },
                onJoinGroup = { groupId, password ->
                    scope.launch {
                        val result = repository.joinGroup(groupId, password)
                        result.fold(
                            onSuccess = {
                                successMessage = "Joined group successfully"
                                showJoinGroupDialog = false
                                selectedGroup = null
                                // Refresh my groups if on that tab
                                if (selectedTabIndex == 1) {
                                    repository.getMyGroups().fold(
                                        onSuccess = { myGroups = it },
                                        onFailure = { }
                                    )
                                }
                            },
                            onFailure = {
                                errorMessage = it.message ?: "Failed to join group"
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: GroupData,
    isLoggedIn: Boolean,
    isMyGroup: Boolean,
    onJoinClick: () -> Unit,
    onLeaveClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onLeaderboardClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.group_name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (group.is_private == true) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Private Group",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    group.group_description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Members",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${group.member_count ?: 0} members",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        if (isMyGroup && group.user_role != null) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = group.user_role!!.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Column {
                    if (isMyGroup) {
                        // Actions for groups the user is in
                        IconButton(onClick = { group.id?.let { onLeaderboardClick(it) } }) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = "View Leaderboard",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (group.user_role == "owner") {
                            IconButton(onClick = { group.id?.let { onDeleteClick(it) } }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Group",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            IconButton(onClick = { group.id?.let { onLeaveClick(it) } }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Leave Group",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        // Actions for groups the user can join
                        if (isLoggedIn) {
                            Button(
                                onClick = onJoinClick,
                                modifier = Modifier.size(width = 80.dp, height = 36.dp)
                            ) {
                                Text(
                                    text = "Join",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreateGroup: (String, String?, String?) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Group") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                    Text("Private Group", modifier = Modifier.padding(start = 8.dp))
                }

                if (isPrivate) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateGroup(
                        groupName.trim(),
                        description.trim().ifBlank { null },
                        if (isPrivate && password.isNotBlank()) password else null
                    )
                },
                enabled = groupName.trim().isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun JoinGroupDialog(
    group: GroupData,
    onDismiss: () -> Unit,
    onJoinGroup: (Int, String?) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Group") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Join \"${group.group_name}\"?",
                    style = MaterialTheme.typography.bodyLarge
                )

                group.group_description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (group.is_private == true) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Group Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    group.id?.let { groupId ->
                        onJoinGroup(
                            groupId,
                            if (group.is_private == true && password.isNotBlank()) password else null
                        )
                    }
                },
                enabled = group.is_private != true || password.isNotBlank()
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyStateMessage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupsScreenPreview() {
    SudokuTheme {
        GroupsScreen()
    }
}
