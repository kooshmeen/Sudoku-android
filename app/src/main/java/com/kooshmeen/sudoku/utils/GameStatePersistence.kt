/**
 * Handles persistence of game state using SharedPreferences
 * Serializes game state to JSON for storage
 */

package com.kooshmeen.sudoku.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.kooshmeen.sudoku.data.*

object GameStatePersistence {
    private const val PREFS_NAME = "SudokuGameState"
    private const val KEY_SAVED_GAME = "saved_game"

    fun saveGameState(context: Context, gameState: GameState) {
        // Only save if game is active and not completed
        if (!gameState.isGameActive || gameState.isGameCompleted) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = serializeGameState(gameState)
        prefs.edit().putString(KEY_SAVED_GAME, json).apply()
    }

    fun loadGameState(context: Context): SavedGameState? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVED_GAME, null) ?: return null
        return deserializeGameState(json)
    }

    fun clearSavedGame(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_SAVED_GAME).apply()
    }

    private fun serializeGameState(gameState: GameState): String {
        val jsonObject = JSONObject().apply {
            put("difficulty", gameState.difficulty)
            put("elapsedTime", gameState.elapsedTimeSeconds)
            put("mistakesCount", gameState.mistakesCount)
            put("grid", serializeGrid(gameState.grid))
            put("solutionGrid", serializeSolutionGrid(gameState.solutionGrid))
        }
        return jsonObject.toString()
    }

    private fun serializeGrid(grid: Array<Array<SudokuCell>>): JSONArray {
        return JSONArray().apply {
            grid.forEach { row ->
                val rowArray = JSONArray().apply {
                    row.forEach { cell ->
                        val cellObj = JSONObject().apply {
                            put("value", cell.value)
                            put("notes", JSONArray(cell.notes.toList()))
                            put("isOriginal", cell.isOriginal)
                            put("hasError", cell.hasError)
                        }
                        put(cellObj)
                    }
                }
                put(rowArray)
            }
        }
    }

    private fun serializeSolutionGrid(solutionGrid: Array<IntArray>): JSONArray {
        return JSONArray().apply {
            solutionGrid.forEach { row ->
                val rowArray = JSONArray().apply {
                    row.forEach { value ->
                        put(value)
                    }
                }
                put(rowArray)
            }
        }
    }

    private fun deserializeGameState(json: String): SavedGameState? {
        return try {
            val jsonObject = JSONObject(json)

            val gridArray = jsonObject.getJSONArray("grid")
            val grid = mutableListOf<List<SavedCellState>>()

            for (i in 0 until gridArray.length()) {
                val rowArray = gridArray.getJSONArray(i)
                val row = mutableListOf<SavedCellState>()

                for (j in 0 until rowArray.length()) {
                    val cellObj = rowArray.getJSONObject(j)
                    val notesArray = cellObj.getJSONArray("notes")
                    val notes = mutableListOf<Int>()

                    for (k in 0 until notesArray.length()) {
                        notes.add(notesArray.getInt(k))
                    }

                    row.add(SavedCellState(
                        value = cellObj.getInt("value"),
                        notes = notes,
                        isOriginal = cellObj.getBoolean("isOriginal"),
                        hasError = cellObj.getBoolean("hasError")
                    ))
                }
                grid.add(row)
            }

            val solutionArray = jsonObject.getJSONArray("solutionGrid")
            val solutionGrid = mutableListOf<List<Int>>()

            for (i in 0 until solutionArray.length()) {
                val rowArray = solutionArray.getJSONArray(i)
                val row = mutableListOf<Int>()

                for (j in 0 until rowArray.length()) {
                    row.add(rowArray.getInt(j))
                }
                solutionGrid.add(row)
            }

            SavedGameState(
                grid = grid,
                difficulty = jsonObject.getString("difficulty"),
                elapsedTimeSeconds = jsonObject.getInt("elapsedTime"),
                mistakesCount = jsonObject.getInt("mistakesCount"),
                solutionGrid = solutionGrid
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
