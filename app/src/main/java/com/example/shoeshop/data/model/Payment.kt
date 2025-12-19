package com.example.shoeshop.data.model

data class PaymentUi(
    val id: String,          // uuid
    val cardName: String,    // например, "Visa •••• 1234"
    val last4: String        // для отображения
)

