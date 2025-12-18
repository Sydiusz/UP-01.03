// data/model/FavouriteItem.kt
package com.example.shoeshop.data.model

import com.google.gson.annotations.SerializedName

data class FavouriteItem(
    @SerializedName("id")
    val id: String? = null,          // uuid в таблице favourite

    @SerializedName("product_id")
    val productId: String,

    @SerializedName("user_id")
    val userId: String
)
