package com.example.shoeshop.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val name: String,

    @SerializedName("cost")
    val price: Double,

    @SerializedName("description")
    val description: String,

    @SerializedName("category_id")
    val categoryId: String?,

    @SerializedName("is_best_seller")
    val isBestSeller: Boolean = false,

    // ВСЕ дополнительные поля — безопасные
    val displayCategory: String? = null,
    val originalPrice: String? = null,
    val imageUrl: String? = null,
    val imageResId: Int? = null,
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false

) {
    fun getFormattedPrice(): String {
        return "₽${String.format("%.2f", price)}"
    }
}
