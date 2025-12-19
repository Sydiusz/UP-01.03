// data/model/OrderItem.kt
package com.example.shoeshop.data.model

data class OrderItem(
    val id: String,
    val title: String?,
    val coast: Double?,
    val count: Long?,
    val order_id: Long?,
    val product_id: String?
)
