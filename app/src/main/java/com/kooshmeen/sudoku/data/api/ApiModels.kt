package com.kooshmeen.sudoku.data.api

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class GameSubmission(
    val timeSeconds: Int,
    val difficulty: String,
    val numberOfMistakes: Int
)

data class LeaderboardEntry(
    val player_id: Int,
    val score: Int,
    val period_type: String
)

data class GroupData(
    val group_name: String,
    val group_description: String?,
    val group_password: String?
)

data class ApiResponse(
    val message: String
)