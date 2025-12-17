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


    // Измените типы на nullable или инициализируйте по умолчанию
    val displayCategory: String? = null,
    val originalPrice: String = "",  // Инициализируйте пустой строкой
    val imageUrl: String = "",
    val imageResId: Int? = null,

    val isFavorite: Boolean = false // Добавляем поле для состояния избранного
) {
    fun getFormattedPrice(): String {
        return "₽${String.format("%.2f", price)}"
    }
}