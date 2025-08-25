package com.kooshmeen.sudoku.repository

import android.content.Context
import android.util.Log
import com.kooshmeen.sudoku.api.ApiClient
import com.kooshmeen.sudoku.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SudokuRepository(private val context: Context) {
    private val apiService = ApiClient.apiService
    private val prefs = context.getSharedPreferences("sudoku_prefs", Context.MODE_PRIVATE)

    private var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()

    private var currentUser: User?
        get() {
            val id = prefs.getInt("user_id", -1)
            val username = prefs.getString("user_username", null)
            val email = prefs.getString("user_email", null)
            return if (id != -1 && username != null && email != null) {
                User(id, username, email)
            } else null
        }
        set(value) {
            if (value != null) {
                prefs.edit().apply {
                    putInt("user_id", value.id)
                    putString("user_username", value.username)
                    putString("user_email", value.email)
                    apply()
                }
            } else {
                prefs.edit().apply {
                    remove("user_id")
                    remove("user_username")
                    remove("user_email")
                    apply()
                }
            }
        }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                authToken = loginResponse.token
                currentUser = loginResponse.user
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<ApiResponse> {
        return try {
            val response = apiService.register(RegisterRequest(username, email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitGame(difficulty: String, timeSeconds: Int, mistakes: Int): Result<ApiResponse> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.submitGame(
                "Bearer $token",
                GameSubmission(timeSeconds, difficulty.lowercase(), mistakes)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit game"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Leaderboard methods
    suspend fun getLeaderboardTotal(): Result<LeaderboardResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTop100GlobalAllTime()

                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load leaderboard: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLeaderboardMonth(): Result<LeaderboardResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTop100GlobalMonth()

                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load monthly leaderboard: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLeaderboardWeek(): Result<LeaderboardResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTop100GlobalWeek()

                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load weekly leaderboard: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLeaderboardDay(): Result<LeaderboardResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTop100GlobalDay()

                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load daily leaderboard: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun isLoggedIn(): Boolean = authToken != null

    fun fetchCurrentUser(): User? = currentUser

    fun logout() {
        authToken = null
        currentUser = null
    }

    // Group Management Methods
    suspend fun getAllGroups(): Result<List<GroupData>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllGroups()
                if (response.isSuccessful) {
                    response.body()?.let { groupsResponse ->
                        Result.success(groupsResponse.groups)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load groups: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchGroups(query: String): Result<List<GroupData>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchGroups(query)
                if (response.isSuccessful) {
                    response.body()?.let { groupsResponse ->
                        Result.success(groupsResponse.groups)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to search groups: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMyGroups(): Result<List<GroupData>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.getMyGroups("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { groupsResponse ->
                        Result.success(groupsResponse.groups)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load my groups: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createGroup(groupName: String, description: String?, password: String?): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                // Create a map to send only the necessary data to the server
                val groupRequest = mutableMapOf<String, Any?>(
                    "group_name" to groupName,
                    "group_description" to description
                )
                if (password != null) {
                    groupRequest["group_password"] = password
                }

                val response = apiService.createGroup("Bearer $token", groupRequest)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to create group: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun joinGroup(groupId: Int, password: String?): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = if (password != null) {
                    apiService.joinGroupWithPassword("Bearer $token", groupId, mapOf("password" to password))
                } else {
                    apiService.joinGroup("Bearer $token", groupId)
                }
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to join group: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun leaveGroup(groupId: Int): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.leaveGroup("Bearer $token", groupId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to leave group: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteGroup(groupId: Int): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.deleteGroup("Bearer $token", groupId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to delete group: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroupDetails(groupId: Int): Result<GroupDetailsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.getGroupDetails("Bearer $token", groupId)
                Log.d("SudokuRepository", "response body: ${response.body()}")
                if (response.isSuccessful) {
                    Log.d("SudokuRepository", "Group details response: ${response.body()}")
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load group details: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroupLeaderboard(groupId: Int): Result<LeaderboardResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.getGroupLeaderboard("Bearer $token", groupId)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load group leaderboard: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGroupMembers(groupId: Int): Result<List<GroupMember>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.getGroupMembers("Bearer $token", groupId)
                if (response.isSuccessful) {
                    response.body()?.let { groupMembersResponse ->
                        Result.success(groupMembersResponse.members)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to load group members: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Create a challenge invitation with type
     */
    suspend fun createChallenge(
        groupId: Int,
        challengedId: Int,
        difficulty: String,
        challengeType: String
    ): Result<ChallengeCreationResponse> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val request = CreateChallengeRequest(challengedId, difficulty, challengeType)
            val response = apiService.createChallenge("Bearer $token", groupId, request)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reject a challenge invitation
     */
    suspend fun rejectChallenge(challengeId: Int): Result<String> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.rejectChallenge("Bearer $token", challengeId)

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Challenge rejected")
            } else {
                Result.failure(Exception("Failed to reject challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete challenger's game for offline challenges
     */
    suspend fun completeChallengerGame(
        challengeId: Int,
        timeSeconds: Int,
        numberOfMistakes: Int,
        puzzleData: Map<String, Any>
    ): Result<String> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val request = ChallengerCompletionRequest(
                timeSeconds = timeSeconds,
                numberOfMistakes = numberOfMistakes,
                puzzleData = puzzleData
            )

            val response = apiService.completeChallengerGame("Bearer $token", challengeId, request)

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Game completed")
            } else {
                Result.failure(Exception("Failed to complete game: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get pending challenges for current user
     */
    suspend fun getPendingChallenges(): Result<List<ChallengeInvitation>> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.getPendingChallenges("Bearer $token")

            if (response.isSuccessful) {
                val challenges = response.body()?.challenges ?: emptyList()
                Result.success(challenges)
            } else {
                Result.failure(Exception("Failed to get challenges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get pending live matches for current user
     */
    suspend fun getPendingLiveMatches(): Result<List<LiveMatch>> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.getPendingLiveMatches("Bearer $token")

            if (response.isSuccessful) {
                val matches = response.body()?.get("matches") as? List<LiveMatch> ?: emptyList()
                Result.success(matches)
            } else {
                Result.failure(Exception("Failed to get live matches: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept a live match invitation
     */
    suspend fun acceptLiveMatch(matchId: Int): Result<Map<String, Any>> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.acceptLiveMatch("Bearer $token", matchId)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Failed to accept live match: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept a challenge invitation
     */
    suspend fun acceptChallenge(challengeId: Int): Result<Map<String, Any>> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.acceptChallenge("Bearer $token", challengeId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Failed to accept challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete a challenge
     */
    suspend fun completeChallenge(
        challengeId: Int,
        timeSeconds: Int,
        numberOfMistakes: Int
    ): Result<ChallengeCompletionResponse> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val request = ChallengeCompletionRequest(timeSeconds, numberOfMistakes)
            val response = apiService.completeChallenge("Bearer $token", challengeId, request)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to complete challenge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get challenge data including puzzle information
     */
    suspend fun getChallengeData(challengeId: Int): Result<Map<String, Any>> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val response = apiService.getChallengeData("Bearer $token", challengeId)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Failed to get challenge data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update group member statistics (wins/losses/draws)
     */
    suspend fun updateMemberStats(
        groupId: Int,
        memberId: Int,
        wins: Int? = null,
        losses: Int? = null,
        draws: Int? = null
    ): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val statsUpdate = mutableMapOf<String, Any>()
                wins?.let { statsUpdate["wins"] = it }
                losses?.let { statsUpdate["losses"] = it }
                draws?.let { statsUpdate["draws"] = it }

                val response = apiService.updateMemberStats("Bearer $token", groupId, memberId, statsUpdate)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to update member stats: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get the data from a live match
     */
    suspend fun getLiveMatchStatus(matchId: Int): Result<LiveMatchStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.getLiveMatchDetails("Bearer $token", matchId)

                if (response.isSuccessful) {
                    response.body()?.let { liveMatchResponse ->
                        Result.success(liveMatchResponse.match)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to get live match status: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Cancel a live match
     */
    suspend fun cancelLiveMatch(matchId: Int): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token =
                    authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.cancelLiveMatch("Bearer $token", matchId)

                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("Failed to cancel live match: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Complete a live match
     */
    suspend fun completeLiveMatch(
        matchId: Int,
        timeSeconds: Int,
        mistakes: Int
    ): Result<LiveMatchCompletionResponse> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))
            val request = LiveMatchCompletionRequest(timeSeconds, mistakes)
            val response = apiService.completeLiveMatch("Bearer $token", matchId, request)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to complete live match: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
