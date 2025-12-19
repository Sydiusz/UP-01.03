// ui/screens/OrderDetailsScreen.kt
package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.R
import com.example.shoeshop.data.model.OrderItem
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.OrderDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: Long,
    onBackClick: () -> Unit,
    viewModel: OrderDetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = stringResource(id = R.string.back_to_shopping)
                        )
                    }
                },
                title = {
                    Text(
                        text = state.orderId.toString(),
                        style = AppTypography.headingRegular32
                    )
                },
                actions = {
                    Text(
                        text = state.timeLabel,
                        style = AppTypography.bodyRegular12,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = msg, color = Color.Red)
                }
            } ?: LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // список всех товаров заказа
                items(state.items) { item ->
                    OrderItemCard(item = item)
                }

                // контактная информация
                item {
                    ContactInfoCard(
                        email = state.email,
                        phone = state.phone,
                        address = state.address
                    )
                }

                // карта (заглушка как в макете)
                item {
                    MapCard()
                }

                // способ оплаты (пока просто текст)
                item {
                    PaymentCard(delivery = state.deliveryCost)
                }
            }
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
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
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bag_2),
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "",
                    style = AppTypography.bodyMedium16
                )
                Text(
                    text = "₽${String.format("%.2f", item.coast ?: 0.0)}",
                    style = AppTypography.bodyMedium16
                )
            }
            Text(
                text = "${item.count ?: 0} шт",
                style = AppTypography.bodyRegular12,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ContactInfoCard(
    email: String,
    phone: String,
    address: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.contact_information),
                style = AppTypography.bodyMedium16
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.email),
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (email.isNotBlank()) email else stringResource(id = R.string.not_specified),
                        style = AppTypography.bodyMedium16
                    )
                    Text(
                        text = stringResource(id = R.string.email),
                        style = AppTypography.bodyRegular12,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.call),
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (phone.isNotBlank()) phone else stringResource(id = R.string.not_specified),
                        style = AppTypography.bodyMedium16
                    )
                    Text(
                        text = stringResource(id = R.string.phone_number),
                        style = AppTypography.bodyRegular12,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.address),
                style = AppTypography.bodyMedium16,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = if (address.isNotBlank()) address else stringResource(id = R.string.not_specified),
                style = AppTypography.bodyRegular14,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MapCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.view_map),
                style = AppTypography.bodyMedium16,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PaymentCard(delivery: Long) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.payment_method),
                style = AppTypography.bodyMedium16
            )
            Text(
                text = "Доставка: ₽$delivery",
                style = AppTypography.bodyRegular14
            )
        }
    }
}
