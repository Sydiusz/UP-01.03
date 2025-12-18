// data/model/Profile.kt
package com.example.shoeshop.data.model

data class Profile(
    val id: String,
    val user_id: String,      // ← как в БД
    val firstname: String?,
    val lastname: String?,
    val address: String?,
    val phone: String?
)
