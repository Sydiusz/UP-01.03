package com.example.myfirstproject.data.model

data class VerifyOtpResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: User
)