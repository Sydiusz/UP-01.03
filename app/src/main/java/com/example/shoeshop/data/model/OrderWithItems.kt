// data/model/OrderWithItems.kt
package com.example.shoeshop.data.model

data class OrderWithItems(
    val id: Long,
    val created_at: String?,
    val status_id: String?,
    val delivery_coast: Long?,
    val items: List<OrderItem>
)