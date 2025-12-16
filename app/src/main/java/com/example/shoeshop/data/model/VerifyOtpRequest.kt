package com.example.myfirstproject.data.model

data class VerifyOtpRequest(
    val email: String,
    val token: String,
    val type: String // "email" или "recovery"
)