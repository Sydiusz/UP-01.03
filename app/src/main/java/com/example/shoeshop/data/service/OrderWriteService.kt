// data/service/OrderWriteService.kt
package com.example.shoeshop.data.service

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Query

interface OrderWriteService {
    // DELETE /orders?id=eq.<orderId>
    @DELETE("orders")
    suspend fun deleteOrder(
        @Query("id") idFilter: String // "eq.<orderId>"
    ): Response<Unit>
}
