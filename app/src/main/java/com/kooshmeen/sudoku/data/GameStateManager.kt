/**
 * Global game state manager
 * Provides a singleton instance of GameState that can be shared across screens
 */

package com.kooshmeen.sudoku.data

import android.content.Context
import com.kooshmeen.sudoku.utils.GameStatePersistence

object GameStateManager {
    private val _gameState = GameState()

    val gameState: GameState get() = _gameState

    /**
     * Check if there's an active game that can be continued
     */
    fun hasActiveGame(): Boolean {
        return _gameState.hasActiveGame()
    }

    /**
     * Check if there's a saved game that can be loaded
     */
    fun hasSavedGame(context: Context): Boolean {
        return GameStatePersistence.loadGameState(context) != null
    }

    /**
     * Load saved game
     */
    fun loadSavedGame(context: Context): Boolean {
        val savedState = GameStatePersistence.loadGameState(context) ?: return false
        _gameState.loadFromSavedState(savedState)
        return true
    }

    /**
     * Save current game
     */
    fun saveCurrentGame(context: Context) {
        GameStatePersistence.saveGameState(context, _gameState)
    }

    /**
     * Start a new game with the specified difficulty
     */
    fun startNewGame(difficulty: String, context: Context? = null) {
        context?.let { GameStatePersistence.clearSavedGame(it) }
        _gameState.clearGrid()
        _gameState.startNewGame(difficulty)
    }

    /**
     * Load a challenge game with specific puzzle data
     */
    fun loadChallengeGame(puzzleData: Map<*, *>, difficulty: String, context: Context? = null) {
        context?.let { GameStatePersistence.clearSavedGame(it) }
        _gameState.clearGrid()
        _gameState.loadChallengeGame(puzzleData, difficulty)
    }

    /**
     * Continue the current active game
     */
    fun continueGame() {
        _gameState.continueGame()
    }

    /**
     * End the current game
     */
    fun endGame() {
        _gameState.endGame()
    }
}
