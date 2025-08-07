package com.kooshmeen.sudoku.repository

import android.content.Context
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
                val groupData = GroupData(groupName, description, password)
                val response = apiService.createGroup("Bearer $token", groupData)
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

    suspend fun getGroupDetails(groupId: Int): Result<GroupData> {
        return withContext(Dispatchers.IO) {
            try {
                val token = authToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val response = apiService.getGroupDetails("Bearer $token", groupId)
                if (response.isSuccessful) {
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
}
