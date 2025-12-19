// data/service/OrderItemsReadService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.OrderItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OrderItemsReadService {

    @GET("orders_items")
    suspend fun getItemsForOrder(
        @Query("order_id") orderIdFilter: String // "eq.<orderId>"
    ): Response<List<OrderItem>>
}
