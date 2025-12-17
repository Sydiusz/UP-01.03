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

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("is_best_seller")
    val isBestSeller: Boolean = false,

    val originalPrice: String = "", // можно оставить для отображения старой цены
    val imageUrl: String = "",
    val imageResId: Int? = null
) {
    // Форматированная цена
    fun getFormattedPrice(): String {
        return "P${String.format("%.2f", price)}"
    }
}