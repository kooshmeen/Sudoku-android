/**
 * Settings screen will contain options for the user to customize the game experience.
 * Settings can include:
 * Color theme (customize each color of both light and dark themes)
 * Delete all local records
 * Delete account (if logged in)
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.repository.SudokuRepository
import com.kooshmeen.sudoku.ui.theme.ColorPalettes
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import com.kooshmeen.sudoku.utils.BestTimeManager
import com.kooshmeen.sudoku.utils.StatisticsManager
import com.kooshmeen.sudoku.utils.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SudokuRepository(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteRecordsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showResetThemeDialog by remember { mutableStateOf(false) }
    var showColorCustomization by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val currentTheme = remember { ThemeManager.loadCustomTheme(context) }
    var customTheme by remember { mutableStateOf(currentTheme) }

    val tabTitles = listOf("General", "Theme", "Data", "Account")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and title
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Tab Row
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

        // Content based on selected tab
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> { // General
                    item { GeneralSettingsSection(isDarkTheme, onThemeToggle) }
                }
                1 -> { // Theme
                    item {
                        ThemeSettingsSection(
                            customTheme = customTheme,
                            onThemeChange = { newTheme ->
                                customTheme = newTheme
                                ThemeManager.saveCustomTheme(context, newTheme)
                                successMessage = "Theme updated successfully!"
                            },
                            onResetTheme = { showResetThemeDialog = true },
                            onShowColorCustomization = { showColorCustomization = !showColorCustomization },
                            showColorCustomization = showColorCustomization,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
                2 -> { // Data
                    item {
                        DataSettingsSection(
                            onDeleteRecords = { showDeleteRecordsDialog = true }
                        )
                    }
                }
                3 -> { // Account
                    item {
                        AccountSettingsSection(
                            repository = repository,
                            onDeleteAccount = { showDeleteAccountDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showDeleteRecordsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteRecordsDialog = false },
            title = { Text("Delete All Records") },
            text = { Text("Are you sure you want to delete all local records and statistics? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        BestTimeManager.clearBestTimes(context)
                        StatisticsManager.clearAllStats(context)
                        showDeleteRecordsDialog = false
                        successMessage = "All records and statistics deleted successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteRecordsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This will permanently delete all your data on the server. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            // TODO: Implement account deletion API call
                            repository.logout()
                            showDeleteAccountDialog = false
                            successMessage = "Account deletion initiated. You have been logged out."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetThemeDialog) {
        AlertDialog(
            onDismissRequest = { showResetThemeDialog = false },
            title = { Text("Reset Theme") },
            text = { Text("Are you sure you want to reset the theme to default colors?") },
            confirmButton = {
                Button(
                    onClick = {
                        ThemeManager.resetToDefaults(context)
                        customTheme = ThemeManager.loadCustomTheme(context)
                        showResetThemeDialog = false
                        successMessage = "Theme reset to defaults!"
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GeneralSettingsSection(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "General Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dark/Light Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Use dark colors for the interface",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeToggle
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingsSection(
    customTheme: ThemeManager.CustomTheme,
    onThemeChange: (ThemeManager.CustomTheme) -> Unit,
    onResetTheme: () -> Unit,
    onShowColorCustomization: () -> Unit,
    showColorCustomization: Boolean,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Text(
                text = "Theme Customization",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Preset Color Palettes
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val primaryTemp = MaterialTheme.colorScheme.primary

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(
                    listOf(
                        "Default" to mapOf("primary" to primaryTemp),
                        "Blue" to ColorPalettes.Blue,
                        "Green" to ColorPalettes.Green,
                        "Orange" to ColorPalettes.Orange,
                        "Red" to ColorPalettes.Red,
                        "Teal" to ColorPalettes.Teal
                    )
                ) { (name, palette) ->
                    PresetColorCard(
                        name = name,
                        primaryColor = palette["primary"] ?: Color.Gray,
                        onClick = {
                            if (name == "Default") {
                                onResetTheme()
                            } else {
                                // Apply preset palette
                                val newTheme = if (isDarkTheme) {
                                    customTheme.copy(
                                        darkPrimary = palette["primary"] ?: customTheme.darkPrimary,
                                        darkSecondary = palette["secondary"] ?: customTheme.darkSecondary,
                                        darkTertiary = palette["tertiary"] ?: customTheme.darkTertiary
                                    )
                                } else {
                                    customTheme.copy(
                                        lightPrimary = palette["primary"] ?: customTheme.lightPrimary,
                                        lightSecondary = palette["secondary"] ?: customTheme.lightSecondary,
                                        lightTertiary = palette["tertiary"] ?: customTheme.lightTertiary
                                    )
                                }
                                onThemeChange(newTheme)
                            }
                        }
                    )
                }
            }

            // Custom Color Controls Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowColorCustomization() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Colors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Fine-tune individual colors",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (showColorCustomization) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showColorCustomization) "Collapse" else "Expand"
                )
            }

            if (showColorCustomization) {
                Spacer(modifier = Modifier.height(16.dp))
                ColorCustomizationSection(
                    customTheme = customTheme,
                    onThemeChange = onThemeChange,
                    isDarkTheme = isDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Button
            OutlinedButton(
                onClick = onResetTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Defaults")
            }
        }
    }
}

@Composable
private fun PresetColorCard(
    name: String,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ColorCustomizationSection(
    customTheme: ThemeManager.CustomTheme,
    onThemeChange: (ThemeManager.CustomTheme) -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current

    Text(
        text = if (isDarkTheme) "Dark Theme Colors" else "Light Theme Colors",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    val colors = if (isDarkTheme) {
        listOf(
            "Primary" to customTheme.darkPrimary,
            "Secondary" to customTheme.darkSecondary,
            "Tertiary" to customTheme.darkTertiary,
            "Background" to customTheme.darkBackground,
            "Surface" to customTheme.darkSurface
        )
    } else {
        listOf(
            "Primary" to customTheme.lightPrimary,
            "Secondary" to customTheme.lightSecondary,
            "Tertiary" to customTheme.lightTertiary,
            "Background" to customTheme.lightBackground,
            "Surface" to customTheme.lightSurface
        )
    }

    colors.forEach { (colorName, color) ->
        ColorPickerRow(
            colorName = colorName,
            color = color,
            onColorChange = { newColor ->
                val validationResult = when (colorName) {
                    "Background" -> ThemeManager.validateColorCombination(
                        newColor,
                        if (isDarkTheme) customTheme.darkOnBackground else customTheme.lightOnBackground,
                        if (isDarkTheme) customTheme.darkSurface else customTheme.lightSurface,
                        if (isDarkTheme) customTheme.darkOnSurface else customTheme.lightOnSurface
                    )
                    "Surface" -> ThemeManager.validateColorCombination(
                        if (isDarkTheme) customTheme.darkBackground else customTheme.lightBackground,
                        if (isDarkTheme) customTheme.darkOnBackground else customTheme.lightOnBackground,
                        newColor,
                        if (isDarkTheme) customTheme.darkOnSurface else customTheme.lightOnSurface
                    )
                    else -> ThemeManager.ValidationResult(true, emptyList())
                }

                if (validationResult.isValid) {
                    val newTheme = updateThemeColor(customTheme, colorName, newColor, isDarkTheme)
                    onThemeChange(newTheme)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ColorPickerRow(
    colorName: String,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showColorPicker = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = colorName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${color.toArgb().toUInt().toString(16).uppercase().takeLast(6)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }

    if (showColorPicker) {
        SimpleColorPicker(
            currentColor = color,
            onColorSelected = { newColor ->
                onColorChange(newColor)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
private fun SimpleColorPicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val commonColors = listOf(
        Color.Red, Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
        Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color.Black,
        Color.White, Color(0xFF212121), Color(0xFF424242), Color(0xFF616161)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Color") },
        text = {
            LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == currentColor) 3.dp else 1.dp,
                                color = if (color == currentColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DataSettingsSection(
    onDeleteRecords: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Delete All Records
            OutlinedButton(
                onClick = onDeleteRecords,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Delete Records"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete All Local Records")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This will permanently delete all your local statistics, best times, and game history. Your online data will remain intact if you're logged in.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountSettingsSection(
    repository: SudokuRepository,
    onDeleteAccount: () -> Unit
) {
    val isLoggedIn = repository.isLoggedIn()
    val currentUser = repository.fetchCurrentUser()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Account Management",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoggedIn && currentUser != null) {
                // Show account info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentUser.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Logout Button
                OutlinedButton(
                    onClick = { repository.logout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Account Button
                OutlinedButton(
                    onClick = onDeleteAccount,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Account"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This will permanently delete your account and all associated data from our servers. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Show not logged in message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = "Not logged in",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Not logged in",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Log in to access account management features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun updateThemeColor(
    theme: ThemeManager.CustomTheme,
    colorName: String,
    newColor: Color,
    isDarkTheme: Boolean
): ThemeManager.CustomTheme {
    return if (isDarkTheme) {
        when (colorName) {
            "Primary" -> theme.copy(darkPrimary = newColor)
            "Secondary" -> theme.copy(darkSecondary = newColor)
            "Tertiary" -> theme.copy(darkTertiary = newColor)
            "Background" -> theme.copy(darkBackground = newColor)
            "Surface" -> theme.copy(darkSurface = newColor)
            else -> theme
        }
    } else {
        when (colorName) {
            "Primary" -> theme.copy(lightPrimary = newColor)
            "Secondary" -> theme.copy(lightSecondary = newColor)
            "Tertiary" -> theme.copy(lightTertiary = newColor)
            "Background" -> theme.copy(lightBackground = newColor)
            "Surface" -> theme.copy(lightSurface = newColor)
            else -> theme
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SudokuTheme {
        SettingsScreen()
    }
}
