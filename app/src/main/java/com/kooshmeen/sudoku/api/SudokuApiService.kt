package com.kooshmeen.sudoku.api

import com.kooshmeen.sudoku.data.api.ApiResponse
import com.kooshmeen.sudoku.data.api.GameSubmission
import com.kooshmeen.sudoku.data.api.GroupData
import com.kooshmeen.sudoku.data.api.GroupsResponse
import com.kooshmeen.sudoku.data.api.LeaderboardResponse
import com.kooshmeen.sudoku.data.api.LoginRequest
import com.kooshmeen.sudoku.data.api.LoginResponse
import com.kooshmeen.sudoku.data.api.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SudokuApiService {
    // Public routes (no authentication required)
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("groups")
    suspend fun getAllGroups(): Response<GroupsResponse>

    @GET("groups/search")
    suspend fun searchGroups(@Query("q") query: String): Response<GroupsResponse>

    @GET("leaderboard/global")
    suspend fun getGlobalLeaderboard(): Response<LeaderboardResponse>

    // Specific leaderboard routes
    @GET("leaderboard/global/all-time")
    suspend fun getTop100GlobalAllTime(): Response<LeaderboardResponse>

    @GET("leaderboard/global/monthly")
    suspend fun getTop100GlobalMonth(): Response<LeaderboardResponse>

    @GET("leaderboard/global/weekly")
    suspend fun getTop100GlobalWeek(): Response<LeaderboardResponse>

    @GET("leaderboard/global/daily")
    suspend fun getTop100GlobalDay(): Response<LeaderboardResponse>

    // Protected routes (authentication required)
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

    // Player statistics and medals
    @GET("stats")
    suspend fun getPlayerStats(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("medals")
    suspend fun getPlayerMedals(
        @Header("Authorization") token: String
    ): Response<List<Map<String, Any>>>

    // Group management routes
    @POST("groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body groupData: GroupData
    ): Response<ApiResponse>

    @GET("my-groups")
    suspend fun getMyGroups(
        @Header("Authorization") token: String
    ): Response<List<GroupData>>

    @GET("groups/{groupId}")
    suspend fun getGroupDetails(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int
    ): Response<GroupData>

    @POST("groups/{groupId}/join")
    suspend fun joinGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int,
        @Body password: Map<String, String>? = null
    ): Response<ApiResponse>

    @DELETE("groups/{groupId}/leave")
    suspend fun leaveGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int
    ): Response<ApiResponse>

    @DELETE("groups/{groupId}")
    suspend fun deleteGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int
    ): Response<ApiResponse>

    // Group leaderboard
    @GET("groups/{groupId}/leaderboard")
    suspend fun getGroupLeaderboard(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int
    ): Response<LeaderboardResponse>

    // Group member management (leaders only)
    @PUT("groups/{groupId}/members/{memberId}/role")
    suspend fun setMemberRole(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Int,
        @Path("memberId") memberId: Int,
        @Body roleData: Map<String, String>
    ): Response<ApiResponse>

    // Admin routes (for awarding medals)
    @POST("players/{playerId}/medals")
    suspend fun awardMedal(
        @Header("Authorization") token: String,
        @Path("playerId") playerId: Int,
        @Body medalData: Map<String, Any>
    ): Response<ApiResponse>
}