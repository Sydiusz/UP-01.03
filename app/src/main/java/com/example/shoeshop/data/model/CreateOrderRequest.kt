package com.example.shoeshop.data.model

data class CreateOrderRequest(
    val email: String?,
    val phone: String?,
    val address: String?,
    val user_id: String,
    val payment_id: String?,
    val delivery_coast: Long?,
    val status_id: String
)
