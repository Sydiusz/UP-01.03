// ui/components/ProductCard.kt
package com.example.shoeshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shoeshop.R
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.ui.theme.AppTypography

@Composable
fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(120.dp)
            ) {
                // Картинка из Supabase через ProductImage
                ProductImage(
                    productId = product.id,
                    modifier = Modifier.fillMaxSize()
                )

                // избранное (слева сверху)
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (product.isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Избранное",
                        tint = if (product.isFavorite) Color.Red else Color.Black
                    )
                }
            }

            // НИЖНЯЯ ЧАСТЬ: текст + кнопка в правом нижнем углу
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (product.isBestSeller == true) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // цена слева
                    Text(
                        text = product.getFormattedPrice(),  // используем метод из Product
                        style = AppTypography.bodyMedium16.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // кнопка справа
                    IconButton(
                        onClick = onAddToCartClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color(0xFF03A9F4),
                                shape = RoundedCornerShape(13.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (product.isInCart)
                                    R.drawable.cart   // иконка корзины
                                else
                                    R.drawable.add    // иконка "+"
                            ),
                            contentDescription = if (product.isInCart)
                                "В корзине"
                            else
                                "Добавить в корзину",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
