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
        return prefs.getLong(difficulty, Long.MAX_VALUE) // Return max value if no time is set
    }

    /**
     * Retreives the best time formatted to make human readable.
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
     * Sets a new best time for the given difficulty.
     * Returns true if the new time is better than the current best time, false otherwise.
     */
    fun setBestTime(context: Context, difficulty: String, time: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val timeL: Long = try {
            // Convert the time string to milliseconds
            val parts = time.split(":")
            if (parts.size == 2) {
                val minutes = parts[0].toLongOrNull() ?: 0
                val seconds = parts[1].toLongOrNull() ?: 0
                (minutes * 60 + seconds) * 1000 // Convert to milliseconds
            } else {
                Long.MAX_VALUE // Invalid format, return max value
            }
        } catch (e: Exception) {
            Long.MAX_VALUE // In case of any parsing error, return max value
        }

        // Only update if the new time is better
        if (timeL < getBestTime(context, difficulty)) {
            editor.putLong(difficulty, timeL)
            editor.apply()
            return true // Best time updated
        }
        return false // No update made
    }
}
