// ui/components/ProductCard.kt
package com.example.shoeshop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.ui.theme.AppTypography

@Composable
fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Верхняя часть с изображением и кнопкой избранного
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Изображение товара
                if (product.imageResId != null) {
                    Image(
                        painter = painterResource(id = product.imageResId),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (product.imageUrl.isNotEmpty()) {
                    // Здесь можно добавить загрузку изображения из URL с помощью Coil
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Image",
                            style = AppTypography.bodyRegular12
                        )
                    }
                } else {
                    // Запасной вариант, если нет изображения
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name.take(2).uppercase(),
                            style = AppTypography.headingRegular32
                        )
                    }
                }

                // Кнопка избранного поверх изображения
                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        onFavoriteClick()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Избранное",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Нижняя часть с информацией о товаре
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                // Категория
                product.category?.let { category ->
                    if (category.isNotEmpty()) {
                        Text(
                            text = category,
                            style = AppTypography.bodyRegular12,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Название товара
                Text(
                    text = product.name,
                    style = AppTypography.bodyMedium16,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.height(40.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Цена
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.getFormattedPrice(),
                        style = AppTypography.bodyMedium14,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Оригинальная цена (если есть скидка)
                    product.originalPrice.takeIf { it.isNotEmpty() }?.let { originalPrice ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = originalPrice,
                            style = AppTypography.bodyRegular12.copy(
                                color = Color.Gray,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ProductCardPreview() {
    ProductCard(
        product = Product(
            id = "1",
            name = "Nike Air Max 270",
            price = 129.99,
            description = "Comfortable running shoes",
            category = "RUNNING",
            isBestSeller = true,
            imageUrl = "",
            imageResId = null,
            originalPrice = "149.99"
        ),
        onProductClick = {},
        onFavoriteClick = {}
    )
}