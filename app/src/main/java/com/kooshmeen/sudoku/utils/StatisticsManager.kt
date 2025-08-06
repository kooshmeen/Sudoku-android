/**
 * Comprehensive statistics manager for tracking player performance
 * Handles local storage of game statistics including completion counts, scores, and time-based tracking
 */

package com.kooshmeen.sudoku.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*

object StatisticsManager {
    private const val PREFS_NAME = "SudokuStatistics"

    // Keys for different statistics
    private const val KEY_GAMES_COMPLETED = "games_completed_"
    private const val KEY_GAMES_PERFECT = "games_perfect_" // without mistakes
    private const val KEY_TOTAL_SCORE = "total_score_"
    private const val KEY_DAILY_SCORE = "daily_score_"
    private const val KEY_WEEKLY_SCORE = "weekly_score_"
    private const val KEY_MONTHLY_SCORE = "monthly_score_"
    private const val KEY_LAST_PLAY_DATE = "last_play_date"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_LONGEST_STREAK = "longest_streak"
    private const val KEY_TOTAL_TIME_PLAYED = "total_time_played"
    private const val KEY_AVERAGE_SCORE = "average_score_"

    data class PlayerStats(
        val easyGamesCompleted: Int = 0,
        val mediumGamesCompleted: Int = 0,
        val hardGamesCompleted: Int = 0,
        val easyGamesPerfect: Int = 0,
        val mediumGamesPerfect: Int = 0,
        val hardGamesPerfect: Int = 0,
        val easyTotalScore: Int = 0,
        val mediumTotalScore: Int = 0,
        val hardTotalScore: Int = 0,
        val dailyScore: Int = 0,
        val weeklyScore: Int = 0,
        val monthlyScore: Int = 0,
        val currentStreak: Int = 0,
        val longestStreak: Int = 0,
        val totalTimePlayed: Long = 0, // in seconds
        val easyAverageScore: Float = 0f,
        val mediumAverageScore: Float = 0f,
        val hardAverageScore: Float = 0f
    )

    /**
     * Record a completed game and update all relevant statistics
     */
    fun recordCompletedGame(
        context: Context,
        difficulty: String,
        score: Int,
        timeSeconds: Int,
        mistakes: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val difficultyKey = difficulty.lowercase()

        prefs.edit {
            // Update game completion counts
            val completedKey = KEY_GAMES_COMPLETED + difficultyKey
            val currentCompleted = prefs.getInt(completedKey, 0)
            putInt(completedKey, currentCompleted + 1)

            // Update perfect games count (no mistakes)
            if (mistakes == 0) {
                val perfectKey = KEY_GAMES_PERFECT + difficultyKey
                val currentPerfect = prefs.getInt(perfectKey, 0)
                putInt(perfectKey, currentPerfect + 1)
            }

            // Update total scores
            val totalScoreKey = KEY_TOTAL_SCORE + difficultyKey
            val currentTotalScore = prefs.getInt(totalScoreKey, 0)
            putInt(totalScoreKey, currentTotalScore + score)

            // Update average score
            val averageKey = KEY_AVERAGE_SCORE + difficultyKey
            val newAverage = (currentTotalScore + score).toFloat() / (currentCompleted + 1)
            putFloat(averageKey, newAverage)

            // Update time-based scores
            updateTimeBasedScores(this, score, context)

            // Update playing streak
            updatePlayingStreak(this, context)

            // Update total time played
            val currentTotalTime = prefs.getLong(KEY_TOTAL_TIME_PLAYED, 0)
            putLong(KEY_TOTAL_TIME_PLAYED, currentTotalTime + timeSeconds)
        }
    }

    /**
     * Update daily, weekly, and monthly scores
     */
    private fun updateTimeBasedScores(editor: SharedPreferences.Editor, score: Int, context: Context) {
        val currentDate = Calendar.getInstance()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Get current date strings
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate.time)
        val thisWeek = "${currentDate.get(Calendar.YEAR)}-W${currentDate.get(Calendar.WEEK_OF_YEAR)}"
        val thisMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentDate.time)

        // Check if we need to reset daily score
        val lastDailyUpdate = prefs.getString("last_daily_update", "")
        if (lastDailyUpdate != today) {
            editor.putInt(KEY_DAILY_SCORE, score)
            editor.putString("last_daily_update", today)
        } else {
            val currentDaily = prefs.getInt(KEY_DAILY_SCORE, 0)
            editor.putInt(KEY_DAILY_SCORE, currentDaily + score)
        }

        // Check if we need to reset weekly score
        val lastWeeklyUpdate = prefs.getString("last_weekly_update", "")
        if (lastWeeklyUpdate != thisWeek) {
            editor.putInt(KEY_WEEKLY_SCORE, score)
            editor.putString("last_weekly_update", thisWeek)
        } else {
            val currentWeekly = prefs.getInt(KEY_WEEKLY_SCORE, 0)
            editor.putInt(KEY_WEEKLY_SCORE, currentWeekly + score)
        }

        // Check if we need to reset monthly score
        val lastMonthlyUpdate = prefs.getString("last_monthly_update", "")
        if (lastMonthlyUpdate != thisMonth) {
            editor.putInt(KEY_MONTHLY_SCORE, score)
            editor.putString("last_monthly_update", thisMonth)
        } else {
            val currentMonthly = prefs.getInt(KEY_MONTHLY_SCORE, 0)
            editor.putInt(KEY_MONTHLY_SCORE, currentMonthly + score)
        }
    }

    /**
     * Update playing streak (consecutive days played)
     */
    private fun updatePlayingStreak(editor: SharedPreferences.Editor, context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastPlayDate = prefs.getString(KEY_LAST_PLAY_DATE, "")

        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)

        if (lastPlayDate == today) {
            // Already played today, don't update streak
            return
        }

        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)

        val newStreak = if (lastPlayDate == yesterdayStr) {
            // Consecutive day
            currentStreak + 1
        } else {
            // Streak broken or first time playing
            1
        }

        editor.putString(KEY_LAST_PLAY_DATE, today)
        editor.putInt(KEY_CURRENT_STREAK, newStreak)

        if (newStreak > longestStreak) {
            editor.putInt(KEY_LONGEST_STREAK, newStreak)
        }
    }

    /**
     * Get all player statistics
     */
    fun getPlayerStats(context: Context): PlayerStats {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return PlayerStats(
            easyGamesCompleted = prefs.getInt(KEY_GAMES_COMPLETED + "easy", 0),
            mediumGamesCompleted = prefs.getInt(KEY_GAMES_COMPLETED + "medium", 0),
            hardGamesCompleted = prefs.getInt(KEY_GAMES_COMPLETED + "hard", 0),
            easyGamesPerfect = prefs.getInt(KEY_GAMES_PERFECT + "easy", 0),
            mediumGamesPerfect = prefs.getInt(KEY_GAMES_PERFECT + "medium", 0),
            hardGamesPerfect = prefs.getInt(KEY_GAMES_PERFECT + "hard", 0),
            easyTotalScore = prefs.getInt(KEY_TOTAL_SCORE + "easy", 0),
            mediumTotalScore = prefs.getInt(KEY_TOTAL_SCORE + "medium", 0),
            hardTotalScore = prefs.getInt(KEY_TOTAL_SCORE + "hard", 0),
            dailyScore = prefs.getInt(KEY_DAILY_SCORE, 0),
            weeklyScore = prefs.getInt(KEY_WEEKLY_SCORE, 0),
            monthlyScore = prefs.getInt(KEY_MONTHLY_SCORE, 0),
            currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
            longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0),
            totalTimePlayed = prefs.getLong(KEY_TOTAL_TIME_PLAYED, 0),
            easyAverageScore = prefs.getFloat(KEY_AVERAGE_SCORE + "easy", 0f),
            mediumAverageScore = prefs.getFloat(KEY_AVERAGE_SCORE + "medium", 0f),
            hardAverageScore = prefs.getFloat(KEY_AVERAGE_SCORE + "hard", 0f)
        )
    }

    /**
     * Clear all statistics
     */
    fun clearAllStats(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }

    /**
     * Format time in a human-readable way
     */
    fun formatPlayTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes, seconds)
            minutes > 0 -> String.format(Locale.getDefault(), "%dm %ds", minutes, seconds)
            else -> String.format(Locale.getDefault(), "%ds", seconds)
        }
    }

    /**
     * Calculate completion rate for a difficulty
     */
    fun getCompletionRate(completed: Int, perfect: Int): Float {
        return if (completed > 0) (perfect.toFloat() / completed.toFloat()) * 100 else 0f
    }
}
