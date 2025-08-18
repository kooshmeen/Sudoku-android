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
    val id: Int? = null,
    val created_by: Int? = null,
    val created_at: String? = null,
    val member_count: Int? = null,
    val is_private: Boolean? = null, // indicates if group requires password
    val user_role: String? = null // 'owner', 'member', etc.
)

data class ApiResponse(
    val message: String
)

data class GroupsResponse(
    val groups: List<GroupData>,
    val total: Int? = null
)

data class GroupMember(
    val id: Int,
    val group_id: Int,
    val player_id: Int,
    val username: String?,
    val email: String? = null,
    val role: String?, // 'leader', 'member', etc.
    val joined_at: String? = null,
    val wins: Int? = null,
    val losses: Int? = null,
    val draws: Int? = null
)

data class GroupMembersResponse(
    val members: List<GroupMember>,
    val total: Int? = null
)

/**
 * Data class for challenge invitations
 */
data class ChallengeInvitation(
    val id: Int,
    val challenger_id: Int,
    val challenged_id: Int,
    val group_id: Int,
    val difficulty: String,
    val challenger_name: String,
    val group_name: String,
    val challenger_time: Int?,
    val status: String,
    val created_at: String
)

/**
 * Data class for challenge completion
 */
data class ChallengeCompletionRequest(
    val timeSeconds: Int,
    val numberOfMistakes: Int
)

/**
 * Data class for challenge completion response
 */
data class ChallengeCompletionResponse(
    val message: String,
    val winner: String?,
    val challengerScore: Int?,
    val challengedScore: Int?,
    val challengerTime: Int?,
    val challengedTime: Int?
)

/**
 * Response wrapper for challenges list
 */
data class ChallengesResponse(
    val challenges: List<ChallengeInvitation>
)

/**
 * Data class for creating a challenge with type
 */
data class CreateChallengeRequest(
    val challengedId: Int,
    val difficulty: String,
    val challengeType: String // "online" or "offline"
)

/**
 * Data class for challenge creation response
 */
data class ChallengeCreationResponse(
    val message: String,
    val challengeId: Int?,
    val matchId: Int?,
    val puzzleData: Map<String, Any>?,
    val requiresChallengerCompletion: Boolean?,
    val status: String?
)

/**
 * Data class for live matches
 */
data class LiveMatch(
    val id: Int,
    val challenger_id: Int,
    val challenged_id: Int,
    val group_id: Int,
    val difficulty: String,
    val challenger_name: String,
    val group_name: String,
    val status: String,
    val created_at: String
)