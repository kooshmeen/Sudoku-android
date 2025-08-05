package com.kooshmeen.sudoku.api

import com.kooshmeen.sudoku.data.api.ApiResponse
import com.kooshmeen.sudoku.data.api.GameSubmission
import com.kooshmeen.sudoku.data.api.GroupData
import com.kooshmeen.sudoku.data.api.LoginRequest
import com.kooshmeen.sudoku.data.api.LoginResponse
import com.kooshmeen.sudoku.data.api.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SudokuApiService {
    // Authentication
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Profile management
    @PUT("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body update: Map<String, String>
    ): Response<ApiResponse>

    @PUT("password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): Response<ApiResponse>

    // Game submission
    @POST("submit-game")
    suspend fun submitGame(
        @Header("Authorization") token: String,
        @Body gameData: GameSubmission
    ): Response<ApiResponse>

    // Statistics
    @GET("stats")
    suspend fun getPlayerStats(@Header("Authorization") token: String): Response<Any>

    @GET("medals")
    suspend fun getPlayerMedals(@Header("Authorization") token: String): Response<Any>

    // Leaderboards
    @GET("leaderboard/global")
    suspend fun getGlobalLeaderboard(
        @Query("periodType") periodType: String = "all",
        @Query("limit") limit: Int = 10
    ): Response<Any>

    // Groups
    @POST("groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body groupData: GroupData
    ): Response<ApiResponse>

    @GET("groups")
    suspend fun getAllGroups(): Response<Any>

    @GET("my-groups")
    suspend fun getMyGroups(@Header("Authorization") token: String): Response<Any>

    @GET("groups/{groupId}")
    suspend fun getGroupDetails(@Path("groupId") groupId: Int): Response<Any>

    @POST("groups/{groupId}/join")
    suspend fun joinGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int,
        @Body password: Map<String, String>
    ): Response<ApiResponse>
}