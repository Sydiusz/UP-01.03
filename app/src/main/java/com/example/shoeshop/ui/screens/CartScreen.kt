// ui/screens/CartScreen.kt
package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shoeshop.R
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.ui.theme.AppTypography
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class CartUiItem(
    val id: String,
    val product: Product,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    items: List<CartUiItem>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onIncrement: (CartUiItem) -> Unit,
    onDecrement: (CartUiItem) -> Unit,
    onRemove: (CartUiItem) -> Unit,
    onCheckoutClick: () -> Unit
) {
    val subtotal = remember(items) { items.sumOf { it.product.price * it.count } }
    val delivery = if (items.isEmpty()) 0.0 else 60.20
    val total = subtotal + delivery

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.cart),
                        style = AppTypography.headingRegular32.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_shopping)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${items.size} ${stringResource(R.string.items)}",
                        style = AppTypography.bodyRegular14,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items.forEach { cartItem ->
                            CartSwipeItem(
                                item = cartItem,
                                onIncrement = { onIncrement(cartItem) },
                                onDecrement = { onDecrement(cartItem) },
                                onRemove = { onRemove(cartItem) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SummaryBlock(
                        subtotal = subtotal,
                        delivery = delivery,
                        total = total
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onCheckoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.checkout),
                            style = AppTypography.bodyMedium16.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CartSwipeItem(
    item: CartUiItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    var showQuantityPanel by remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = { newValue ->
            when (newValue) {
                DismissValue.DismissedToStart -> {
                    onRemove()
                    false
                }
                DismissValue.DismissedToEnd -> {
                    showQuantityPanel = true
                    false
                }
                else -> true
            }
        }
    )

    // общий clip для всего swipe-элемента
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(
                DismissDirection.StartToEnd,
                DismissDirection.EndToStart
            ),
            background = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF6B6B)),   // уже обрезан Box-ом сверху
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .size(24.dp)
                    )
                }
            },
            dismissContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),          // фон карточки
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showQuantityPanel) {
                        Row(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2F80ED))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmallCircleButton(
                                text = "−",
                                enabled = item.count > 1,
                                onClick = onDecrement
                            )
                            Text(
                                text = item.count.toString(),
                                style = AppTypography.bodyMedium16.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            SmallCircleButton(
                                text = "+",
                                enabled = true,
                                onClick = onIncrement
                            )
                            TextButton(onClick = { showQuantityPanel = false }) {
                                Text("Done", color = Color.White)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        CartItemCard(item = item)
                    }
                }
            }
        )
    }
}
@Composable
private fun SmallCircleButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)                        // маленький круг
            .clip(CircleShape)
            .background(Color.White)
            .let {
                if (enabled) it.clickable(onClick = onClick) else it
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF2F80ED),
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
private fun CartItemCard(item: CartUiItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // превью товара (пока инициалы)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.product.name.take(2).uppercase(),
                style = AppTypography.bodyMedium16,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.product.name,
                style = AppTypography.bodyMedium16,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "P${String.format("%.2f", item.product.price)}",
                style = AppTypography.bodyMedium16.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
@Composable
fun SummaryBlock(
    subtotal: Double,
    delivery: Double,
    total: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Subtotal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.subtotal),
                style = AppTypography.bodyRegular14,
                color = Color.Gray
            )
            Text(
                text = "P${String.format("%.2f", subtotal)}",
                style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Delivery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.delivery),
                style = AppTypography.bodyRegular14,
                color = Color.Gray
            )
            Text(
                text = "P${String.format("%.2f", delivery)}",
                style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(8.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.total_cost),
                style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "P${String.format("%.2f", total)}",
                style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
