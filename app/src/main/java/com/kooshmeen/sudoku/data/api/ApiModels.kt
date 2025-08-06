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
    val username: String,
    val score: Int,
    val rank: Int? = null
)

data class LeaderboardResponse(
    val leaderboard: List<LeaderboardEntry>,
    val total: Int? = null
)

data class GroupData(
    val group_name: String,
    val group_description: String?,
    val group_password: String?,
    val id: Int? = null,
    val created_by: Int? = null,
    val created_at: String? = null,
    val member_count: Int? = null,
    val is_private: Boolean? = null,
    val user_role: String? = null // "owner", "member", etc.
)

data class ApiResponse(
    val message: String
)

data class GroupsResponse(
    val groups: List<GroupData>,
    val total: Int? = null
)
