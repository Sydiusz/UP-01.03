// data/service/OrdersReadService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.Order
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// data/service/OrdersReadService.kt
interface OrdersReadService {

    @GET("orders")
    suspend fun getOrdersForUser(
        @Query("user_id") userIdFilter: String // "eq.<userId>"
    ): Response<List<Order>>

    @GET("orders")
    suspend fun getOrderById(
        @Query("id") idFilter: String // "eq.<orderId>"
    ): Response<List<Order>>
}

