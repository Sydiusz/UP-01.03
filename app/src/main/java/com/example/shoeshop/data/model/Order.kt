// data/model/Order.kt
package com.example.shoeshop.data.model

data class Order(
    val id: Long? = null,
    val created_at: String? = null,   // было OffsetDateTime
    val email: String?,
    val phone: String?,
    val address: String?,
    val user_id: String?,
    val payment_id: String?,
    val delivery_coast: Long?,
    val status_id: String?
)
