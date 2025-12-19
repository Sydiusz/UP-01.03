// data/service/OrderService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.CreateOrderRequest
import com.example.shoeshop.data.model.Order
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OrderService {

    // вернёт созданный заказ (id понадобится для orders_items)
    @Headers("Prefer: return=representation")
    @POST("orders")
    suspend fun createOrder(
        @Body body: List<CreateOrderRequest>
    ): Response<List<Order>>
}
