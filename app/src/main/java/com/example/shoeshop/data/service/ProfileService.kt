// data/service/ProfileService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.Profile
import retrofit2.Response
import retrofit2.http.*

interface ProfileService {

    @GET("profiles")
    suspend fun getProfile(
        @Query("user_id") filter: String
    ): Response<List<Profile>>

    @PATCH("profiles")
    suspend fun updateProfile(
        @Query("user_id") filter: String,  // ← "user_id=eq.<id>"
        @Body body: Map<String, String?>
    ): Response<Unit>

    @POST("profiles")
    suspend fun createProfile(
        @Body body: Map<String, String?>
    ): Response<Unit>
}
