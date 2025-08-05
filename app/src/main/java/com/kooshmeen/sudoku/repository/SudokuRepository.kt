package com.kooshmeen.sudoku.repository

import android.content.Context
import com.kooshmeen.sudoku.api.ApiClient
import com.kooshmeen.sudoku.data.api.*

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

    fun isLoggedIn(): Boolean = authToken != null

    fun fetchCurrentUser(): User? = currentUser

    fun logout() {
        authToken = null
        currentUser = null
    }
}