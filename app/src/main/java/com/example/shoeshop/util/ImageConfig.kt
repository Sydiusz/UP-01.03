package com.example.shoeshop.util


object ImageConfig {
    private const val BASE_URL =
        "https://yixipuxyofpafnvbaprs.supabase.co/storage/v1/object/public"
    private const val PRODUCTS_BUCKET = "products"

    fun productImageUrl(productId: String): String {
        // имя файла: <productId>.jpg
        return "$BASE_URL/$PRODUCTS_BUCKET/$productId.png"
    }
}
