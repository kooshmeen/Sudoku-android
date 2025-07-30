/**
 * Global game state manager
 * Provides a singleton instance of GameState that can be shared across screens
 */

package com.kooshmeen.sudoku.data

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
     * Start a new game with the specified difficulty
     */
    fun startNewGame(difficulty: String) {
        _gameState.startNewGame(difficulty)
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
