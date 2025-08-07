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
    val id: Int,
    val username: String,
    val total_score: String, // Backend sends this as string
    val total_games: String? = null, // Backend sends this as string
    val best_overall_time: Int? = null,
    val rank: Int? = null,
    // Computed properties for backward compatibility
    val player_id: Int = id,
    val score: Int = total_score.toIntOrNull() ?: 0
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
