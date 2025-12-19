// ui/screens/OrdersScreen.kt
package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.R
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.OrdersViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel = viewModel(),
    onRepeatOrder: (Long) -> Unit = {},
    onCancelOrder: (Long) -> Unit = {},
    onOrderClick: (Long) -> Unit = {}      // ← только id
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.orders),
                        style = AppTypography.headingRegular32
                    )
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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.sections.forEach { section ->
                            item {
                                Text(
                                    text = when (section.header) {
                                        "Недавний" -> stringResource(id = R.string.recent)
                                        "Вчера" -> stringResource(id = R.string.yesterday)
                                        else -> section.header
                                    },
                                    style = AppTypography.bodyMedium16,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(section.items) { order ->
                                OrderSwipeItem(
                                    number = "№ ${order.id}",
                                    title = order.title,
                                    price = order.price,
                                    delivery = order.delivery,
                                    timeLabel = order.timeLabel,
                                    onRepeat = { onRepeatOrder(order.id) },
                                    onCancel = { onCancelOrder(order.id) },
                                    onClick = { onOrderClick(order.id) }   // ← только id
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OrderSwipeItem(
    number: String,
    title: String,
    price: Double,
    delivery: Double,
    timeLabel: String,
    onRepeat: () -> Unit,
    onCancel: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberDismissState { value ->
        when (value) {
            DismissValue.DismissedToStart -> {
                onCancel()
                false
            }
            DismissValue.DismissedToEnd -> {
                onRepeat()
                false
            }
            else -> true
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(
            DismissDirection.StartToEnd,
            DismissDirection.EndToStart
        ),
        background = {
            val (color, iconRes, alignment) = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> Triple(
                    Color(0xFF2F80ED),
                    R.drawable.add,
                    Alignment.CenterStart
                )
                DismissDirection.EndToStart -> Triple(
                    Color(0xFFFF6B6B),
                    R.drawable.trash,
                    Alignment.CenterEnd
                )
                else -> Triple(Color.Transparent, null, Alignment.Center)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(color),
                contentAlignment = alignment
            ) {
                iconRes?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .size(24.dp)
                    )
                }
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bag_2),
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = number,
                            style = AppTypography.bodyMedium16,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = title,
                            style = AppTypography.bodyRegular14
                        )
                        Text(
                            text = "₽${String.format("%.2f", price)}",
                            style = AppTypography.bodyMedium16
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = timeLabel,
                            style = AppTypography.bodyRegular12,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "₽${String.format("%.2f", delivery)}",
                            style = AppTypography.bodyRegular12,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    )
}
