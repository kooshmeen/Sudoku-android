/**
 * Keeps persistent track of the best times for different Sudoku difficulties.
 */

package com.kooshmeen.sudoku.utils

import android.annotation.SuppressLint
import android.content.Context

object BestTimeManager {
    private const val PREFS_NAME = "BestTimes"

    /**
     * Retrieves the best time for a given difficulty.
     * Returns Long.MAX_VALUE if no time is set for that difficulty.
     */
    fun getBestTime(context: Context, difficulty: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(difficulty, Long.MAX_VALUE) // Return max value if
        // no time is set
    }

    /**
     * Retrieves the best time with no mistakes for a given difficulty.
     */
    fun getBestTimeNoMistake(context: Context, difficulty: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("${difficulty}_no_mistake", Long.MAX_VALUE) // Return max value if no time is set
    }

    /**
     * Retrieves the best time formatted to make human readable.
     */
    @SuppressLint("DefaultLocale")
    fun getBestTimeFormatted(context: Context, difficulty: String): String {
        val timeL = getBestTime(context, difficulty)
        return if (timeL == Long.MAX_VALUE) {
            "No best time"
        } else {
            val minutes = (timeL / 60000).toInt()
            val seconds = ((timeL % 60000) / 1000).toInt()
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Retrieves the best time with no mistakes formatted to make human readable.
     */
    @SuppressLint("DefaultLocale")
    fun getBestTimeNoMistakeFormatted(context: Context, difficulty: String): String {
        val timeL = getBestTimeNoMistake(context, difficulty)
        return if (timeL == Long.MAX_VALUE) {
            "No best time"
        } else {
            val minutes = (timeL / 60000).toInt()
            val seconds = ((timeL % 60000) / 1000).toInt()
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Sets a new best time for the given difficulty
     * Returns a Pair<isNewBest, isNewBestNoMistake>
     * where isNewBest indicates if the new time is better than the previous best
     */
    fun setBestTime(context: Context, difficulty: String, time: String): Pair<Boolean, Boolean> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Convert time string to milliseconds
        val parts = time.split(":")
        val minutes = parts[0].toIntOrNull() ?: 0
        val seconds = parts[1].toIntOrNull() ?: 0
        val newTime = (minutes * 60 + seconds) * 1000L

        // Get current best times
        val currentBest = getBestTime(context, difficulty)
        val currentBestNoMistake = getBestTimeNoMistake(context, difficulty)

        // Check if the new time is better
        val isNewBest = newTime < currentBest
        val isNewBestNoMistake = newTime < currentBestNoMistake

        // Update best times if necessary
        if (isNewBest) {
            editor.putLong(difficulty, newTime)
        }
        if (isNewBestNoMistake) {
            editor.putLong("${difficulty}_no_mistake", newTime)
        }

        editor.apply()
        return Pair(isNewBest, isNewBestNoMistake)
    }
}
