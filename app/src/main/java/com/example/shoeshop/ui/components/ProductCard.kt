// ui/components/ProductCard.kt
package com.example.shoeshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.ui.theme.AppTypography

@Composable
fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Изображение продукта
            Box(
                modifier = modifier
                    .height(120.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                if (product.imageResId != null) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = product.imageResId),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name.take(2).uppercase(),
                            style = AppTypography.headingRegular32.copy(
                                fontSize = 24.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }

                // Кнопка избранного (СЛЕВА)
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Добавить в избранное",
                        tint = Color.Black
                    )
                }
            }

            // Информация о продукте
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // BEST SELLER текст над названием
                if (product.isBestSeller) {
                    Text(
                        text = "BEST SELLER",
                        style = AppTypography.bodyRegular12.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = product.name,
                    style = AppTypography.bodyMedium16,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Отображаем цену в формате P{цена}
                Text(
                    text = "P${String.format("%.2f", product.price)}",
                    style = AppTypography.bodyMedium16.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}



