// data/model/OnboardingSlide.kt
package com.example.shoeshop.data.model

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val buttonText: String,
    val backgroundColor: Long,
    val imageRes: Int // Добавляем поле для ресурса изображения
)