// data/service/OrderItemsWriteService.kt
package com.example.shoeshop.data.service

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Query

interface OrderItemsWriteService {
    // DELETE /orders_items?order_id=eq.<orderId>
    @DELETE("orders_items")
    suspend fun deleteItemsForOrder(
        @Query("order_id") orderIdFilter: String // "eq.<orderId>"
    ): Response<Unit>
}
