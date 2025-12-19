// data/service/CartService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.CartItem
import retrofit2.Response
import retrofit2.http.*

interface CartService {

    @GET("cart")
    suspend fun getCartItems(
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<CartItem>>

    @GET("cart")
    suspend fun getCartItemForProduct(
        @Query("user_id") userIdFilter: String,
        @Query("product_id") productIdFilter: String,
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 1
    ): Response<List<CartItem>>

    // ВАЖНО: Map<String, String?> вместо Any?
    @POST("cart")
    suspend fun addToCart(
        @Body body: Map<String, String>  // или Map<String, Any?> но лучше data class
    ): Response<Unit>

    @DELETE("cart")
    suspend fun removeFromCart(
        @Query("id") idFilter: String    // "eq.<cartItemId>"
    ): Response<Unit>
    @PATCH("cart")
    suspend fun updateCart(
        @Query("id") idFilter: String,          // "eq.<cartId>"
        @Body body: Map<String, Int>
    ): Response<Unit>

}
