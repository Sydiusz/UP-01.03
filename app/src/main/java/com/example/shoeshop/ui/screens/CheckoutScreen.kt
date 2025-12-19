package com.example.shoeshop.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shoeshop.R
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.CartViewModel
import com.example.shoeshop.ui.viewmodel.CheckoutUiState
import com.example.shoeshop.ui.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    checkoutViewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onOrderCreated: () -> Unit
) {
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val checkoutState by checkoutViewModel.uiState.collectAsStateWithLifecycle()

    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(checkoutState.isSuccess) {
        if (checkoutState.isSuccess) {
            cartViewModel.clearCart()
            checkoutViewModel.resetSuccess()
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.cart),
                        style = AppTypography.headingRegular32
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
            if (checkoutState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        ContactInfoBlock(
                            state = checkoutState,
                            onEmailChange = checkoutViewModel::updateEmail,
                            onPhoneChange = checkoutViewModel::updatePhone,
                            onAddressChange = checkoutViewModel::updateAddress,
                            onToggleEditEmail = checkoutViewModel::toggleEditEmail,
                            onToggleEditPhone = checkoutViewModel::toggleEditPhone
                        )

                        Spacer(Modifier.height(16.dp))

                        SummaryBlock(
                            subtotal = cartState.items.sumOf { it.product.price * it.count },
                            delivery = checkoutState.deliveryCost.toDouble(),
                            total = cartState.items.sumOf { it.product.price * it.count } +
                                    checkoutState.deliveryCost
                        )
                    }

                    Button(
                        onClick = { checkoutViewModel.createOrder(cartState.items) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.confirm),
                            style = AppTypography.bodyMedium16
                        )
                    }
                }
            }

            checkoutState.errorMessage?.let { msg ->
                SnackbarHost(
                    hostState = remember { SnackbarHostState() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Snackbar { Text(text = msg) }
                }
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { /* не закрываем тапом вокруг */ },
                    confirmButton = {}, // не используем стандартную кнопку
                    title = null,
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.confetti),
                                contentDescription = "Конфетти",
                                modifier = Modifier
                                    .size(140.dp)        // увеличенный размер
                                    .padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Вы успешно оформили заказ",
                                style = AppTypography.bodyMedium16
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    onOrderCreated()
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Вернуться к покупкам")
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White
                )
            }


        }
    }
}

@Composable
private fun ContactInfoBlock(
    state: CheckoutUiState,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onToggleEditEmail: () -> Unit,
    onToggleEditPhone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Контактная информация",
            style = AppTypography.bodyMedium16
        )
        Spacer(Modifier.height(8.dp))

        // Email
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.weight(1f),
                enabled = state.isEditingEmail
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onToggleEditEmail) {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Редактировать email"
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Phone
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = { Text("Телефон") },
                modifier = Modifier.weight(1f),
                enabled = state.isEditingPhone
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onToggleEditPhone) {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Редактировать телефон"
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.address,
            onValueChange = onAddressChange,
            label = { Text("Адрес") },
            modifier = Modifier.fillMaxWidth()
        )
        // здесь позже можно добавить карту и способ оплаты
    }
}
