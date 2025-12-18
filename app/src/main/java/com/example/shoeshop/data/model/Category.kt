package com.example.shoeshop.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val name: String,
    val isSelected: Boolean = false
)
