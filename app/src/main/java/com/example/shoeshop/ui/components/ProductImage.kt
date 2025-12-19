// ui/components/ProductImage.kt
package com.example.shoeshop.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shoeshop.util.ImageConfig

@Composable
fun ProductImage(
    productId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val url = ImageConfig.productImageUrl(productId)

    LaunchedEffect(productId) {
        Log.d("ProductImage", "Loading: $url")
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,   // чтобы не обрезало кроссом
        modifier = modifier                // ИСПОЛЬЗУЕМ переданный modifier
    )
}
