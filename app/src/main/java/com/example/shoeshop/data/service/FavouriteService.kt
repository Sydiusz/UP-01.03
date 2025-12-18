// data/service/FavouriteService.kt
package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.FavouriteItem
import retrofit2.Response
import retrofit2.http.*

interface FavouriteService {

    @GET("favourite")
    suspend fun getFavorites(
        @Query("user_id") userIdFilter: String,        // "eq.<user_id>"
        @Query("product_id") productIdFilter: String? = null, // "eq.<product_id>" или null
        @Query("select") select: String = "*"
    ): Response<List<FavouriteItem>>

    @POST("favourite")
    suspend fun addFavorite(
        @Body item: FavouriteItem
    ): Response<Unit>   // тело нам не нужно

    @DELETE("favourite")
    suspend fun removeFavorite(
        @Query("user_id") userIdFilter: String,   // "eq.<user_id>"
        @Query("product_id") productIdFilter: String // "eq.<product_id>"
    ): Response<Unit>

}
