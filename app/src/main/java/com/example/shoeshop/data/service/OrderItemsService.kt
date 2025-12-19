// data/service/OrderItemsService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.CreateOrderItemRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OrderItemsService {

    @Headers("Prefer: return=minimal")
    @POST("orders_items")
    suspend fun createOrderItems(
        @Body body: List<CreateOrderItemRequest>
    ): Response<Unit>
}
