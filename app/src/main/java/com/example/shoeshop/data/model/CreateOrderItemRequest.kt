// data/model/CreateOrderItemRequest.kt
package com.example.shoeshop.data.model

data class CreateOrderItemRequest(
    val title: String?,
    val coast: Double?,
    val count: Long?,
    val order_id: Long?,
    val product_id: String?
)
