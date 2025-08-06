/**
 * Comprehensive records screen displaying player statistics and achievements
 * Shows best times, completion stats, scores, and streaks in a beautiful Material Design layout
 */

package com.kooshmeen.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kooshmeen.sudoku.ui.theme.SudokuTheme
import com.kooshmeen.sudoku.utils.BestTimeManager
import com.kooshmeen.sudoku.utils.StatisticsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val stats = remember { StatisticsManager.getPlayerStats(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Best Times", "Statistics", "Achievements")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and title
        TopAppBar(
            title = {
                Text(
                    text = "Records & Statistics",
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

        // Content based on selected tab
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> { // Overview
                    item { OverviewSection(stats) }
                    item { QuickStatsCards(stats) }
                    item { ScoreProgressSection(stats) }
                }
                1 -> { // Best Times
                    item { BestTimesSection() }
                }
                2 -> { // Statistics
                    item { DetailedStatsSection(stats) }
                    item { DifficultyBreakdownSection(stats) }
                }
                3 -> { // Achievements
                    item { AchievementsSection(stats) }
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(stats: StatisticsManager.PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Total Games Completed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${stats.easyGamesCompleted + stats.mediumGamesCompleted + stats.hardGamesCompleted}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 36.sp
            )
            Text(
                text = "Perfect Games: ${stats.easyGamesPerfect + stats.mediumGamesPerfect + stats.hardGamesPerfect}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun QuickStatsCards(stats: StatisticsManager.PlayerStats) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            listOf(
                Triple("Current Streak", "${stats.currentStreak} days", Icons.Default.Star),
                Triple("Total Score", "${stats.easyTotalScore + stats.mediumTotalScore + stats.hardTotalScore}", Icons.Default.Score),
                Triple("Time Played", StatisticsManager.formatPlayTime(stats.totalTimePlayed), Icons.Default.Schedule),
                Triple("This Week", "${stats.weeklyScore}", Icons.Default.CalendarMonth)
            )
        ) { (title, value, icon) ->
            QuickStatCard(title, value, icon)
        }
    }
}

@Composable
private fun QuickStatCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScoreProgressSection(stats: StatisticsManager.PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Score Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ScoreProgressItem("Today", stats.dailyScore, Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(8.dp))
            ScoreProgressItem("This Week", stats.weeklyScore, Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(8.dp))
            ScoreProgressItem("This Month", stats.monthlyScore, Color(0xFF9C27B0))
        }
    }
}

@Composable
private fun ScoreProgressItem(period: String, score: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = period,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BestTimesSection() {
    val context = LocalContext.current
    val difficulties = listOf("Easy", "Medium", "Hard")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Best Times",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            difficulties.forEach { difficulty ->
                BestTimeItem(
                    difficulty = difficulty,
                    bestTime = BestTimeManager.getBestTimeFormatted(context, difficulty),
                    bestTimeNoMistake = BestTimeManager.getBestTimeNoMistakeFormatted(context, difficulty)
                )
                if (difficulty != difficulties.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BestTimeItem(difficulty: String, bestTime: String, bestTimeNoMistake: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (difficulty) {
                "Easy" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                "Medium" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                "Hard" -> Color(0xFFF44336).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = difficulty,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (difficulty) {
                        "Easy" -> Color(0xFF2E7D32)
                        "Medium" -> Color(0xFFE65100)
                        "Hard" -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "Best: $bestTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Perfect: $bestTimeNoMistake",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Timer",
                tint = when (difficulty) {
                    "Easy" -> Color(0xFF4CAF50)
                    "Medium" -> Color(0xFFFF9800)
                    "Hard" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun DetailedStatsSection(stats: StatisticsManager.PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Detailed Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            StatRow("Total Games", "${stats.easyGamesCompleted + stats.mediumGamesCompleted + stats.hardGamesCompleted}")
            StatRow("Perfect Games", "${stats.easyGamesPerfect + stats.mediumGamesPerfect + stats.hardGamesPerfect}")
            StatRow("Current Streak", "${stats.currentStreak} days")
            StatRow("Longest Streak", "${stats.longestStreak} days")
            StatRow("Total Time", StatisticsManager.formatPlayTime(stats.totalTimePlayed))
            StatRow("Total Score", "${stats.easyTotalScore + stats.mediumTotalScore + stats.hardTotalScore}")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DifficultyBreakdownSection(stats: StatisticsManager.PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Difficulty Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DifficultyCard(
                difficulty = "Easy",
                completed = stats.easyGamesCompleted,
                perfect = stats.easyGamesPerfect,
                totalScore = stats.easyTotalScore,
                averageScore = stats.easyAverageScore,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(12.dp))
            DifficultyCard(
                difficulty = "Medium",
                completed = stats.mediumGamesCompleted,
                perfect = stats.mediumGamesPerfect,
                totalScore = stats.mediumTotalScore,
                averageScore = stats.mediumAverageScore,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(12.dp))
            DifficultyCard(
                difficulty = "Hard",
                completed = stats.hardGamesCompleted,
                perfect = stats.hardGamesPerfect,
                totalScore = stats.hardTotalScore,
                averageScore = stats.hardAverageScore,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: String,
    completed: Int,
    perfect: Int,
    totalScore: Int,
    averageScore: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = difficulty,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "$completed games",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Perfect Games",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$perfect",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        text = "Total Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalScore",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        text = "Average Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f", averageScore),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (completed > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val completionRate = StatisticsManager.getCompletionRate(completed, perfect)
                Text(
                    text = "Perfect Rate: ${String.format("%.1f", completionRate)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun AchievementsSection(stats: StatisticsManager.PlayerStats) {
    val achievements = listOf(
        Achievement("First Victory", "Complete your first game", stats.easyGamesCompleted + stats.mediumGamesCompleted + stats.hardGamesCompleted > 0),
        Achievement("Perfect Player", "Complete a game without mistakes", stats.easyGamesPerfect + stats.mediumGamesPerfect + stats.hardGamesPerfect > 0),
        Achievement("Speed Demon", "Complete 10 games", stats.easyGamesCompleted + stats.mediumGamesCompleted + stats.hardGamesCompleted >= 10),
        Achievement("Dedication", "Play for 7 consecutive days", stats.longestStreak >= 7),
        Achievement("Master", "Complete 50 games", stats.easyGamesCompleted + stats.mediumGamesCompleted + stats.hardGamesCompleted >= 50),
        Achievement("Perfectionist", "Complete 10 perfect games", stats.easyGamesPerfect + stats.mediumGamesPerfect + stats.hardGamesPerfect >= 10),
        Achievement("Marathon", "Play for 100 minutes total", stats.totalTimePlayed >= 6000),
        Achievement("High Scorer", "Score over 10,000 points total", stats.easyTotalScore + stats.mediumTotalScore + stats.hardTotalScore >= 10000)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            achievements.forEach { achievement ->
                AchievementItem(achievement)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

@Composable
private fun AchievementItem(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (achievement.isUnlocked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (achievement.isUnlocked) "Unlocked" else "Locked",
            tint = if (achievement.isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenPreview() {
    SudokuTheme {
        RecordScreen()
    }
}