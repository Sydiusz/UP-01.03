// UserManagementService.kt
package com.example.myfirstproject.data.service

import com.example.myfirstproject.data.model.SignInRequest
import com.example.myfirstproject.data.model.SignInResponse
import com.example.myfirstproject.data.model.SignUpRequest
import com.example.myfirstproject.data.model.SignUpResponse
import com.example.myfirstproject.data.model.VerifyOtpRequest
import com.example.myfirstproject.data.model.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val API_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVpaW1samlubnRjY3J0Z3Vla3htIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk3MjcwMDIsImV4cCI6MjA3NTMwMzAwMn0.SRC95stHTt3wEDQq6egjzUpIqsG6_Ro-niDGBW83LZk"

interface UserManagementService {

    @Headers(
        "apikey: $API_KEY",
        "Content-Type: application/json"
    )
    @POST("auth/v1/signup")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): Response<SignUpResponse>

    @Headers(
        "apikey: $API_KEY",
        "Content-Type: application/json"
    )
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body signInRequest: SignInRequest): Response<SignInResponse>

    @Headers(
        "apikey: $API_KEY",
        "Content-Type: application/json"
    )
    @POST("auth/v1/verify")
    suspend fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest): Response<VerifyOtpResponse>
}