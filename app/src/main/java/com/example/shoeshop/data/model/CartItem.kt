// data/model/CartItem.kt
package com.example.shoeshop.data.model

data class CartItem(
    val id: String,
    val product_id: String,
    val user_id: String,
    val count: Long
)