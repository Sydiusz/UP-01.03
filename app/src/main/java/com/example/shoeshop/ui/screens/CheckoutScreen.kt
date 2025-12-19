package com.example.shoeshop.ui.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shoeshop.R
import com.example.shoeshop.data.services.LocationService
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.CartViewModel
import com.example.shoeshop.ui.viewmodel.CheckoutUiState
import com.example.shoeshop.ui.viewmodel.CheckoutViewModel
import kotlinx.coroutines.launch
import android.Manifest
import androidx.compose.foundation.lazy.LazyColumn
import com.example.shoeshop.data.model.PaymentUi

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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }
    // временные заглушки способов оплаты
    val payments = listOf(
        PaymentUi(id = "1", cardName = "Visa •••• 1234", last4 = "1234"),
        PaymentUi(id = "2", cardName = "MasterCard •••• 5678", last4 = "5678")
    )
    var expanded by remember { mutableStateOf(false) }


    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val coordinates = locationService.getCurrentLocation()
                if (coordinates != null) {
                    val newAddress = locationService.getAddressFromCoordinates(
                        coordinates.first,
                        coordinates.second
                    )
                    if (newAddress != null) {
                        checkoutViewModel.updateAddress(newAddress.fullAddress)
                    }
                }
            }
        }
    }

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
                        text = stringResource(R.string.cart),
                        style = AppTypography.headingSemiBold16,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
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
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ContactInfoBlock(
                            state = checkoutState,
                            onEmailChange = checkoutViewModel::updateEmail,
                            onPhoneChange = checkoutViewModel::updatePhone,
                            onAddressChange = checkoutViewModel::updateAddress,
                            onToggleEditEmail = checkoutViewModel::toggleEditEmail,
                            onToggleEditPhone = checkoutViewModel::toggleEditPhone
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray)
                                .clickable {
                                    val permission = Manifest.permission.ACCESS_FINE_LOCATION
                                    if (
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            permission
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        scope.launch {
                                            val coordinates = locationService.getCurrentLocation()
                                            if (coordinates != null) {
                                                val addr = locationService.getAddressFromCoordinates(
                                                    coordinates.first,
                                                    coordinates.second
                                                )
                                                if (addr != null) {
                                                    checkoutViewModel.updateAddress(addr.fullAddress)
                                                }
                                            }
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(permission)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.map),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.payment_method),
                            style = AppTypography.bodyMedium16,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { expanded = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = payments.firstOrNull { it.id == checkoutState.paymentId }?.cardName
                                        ?: "Выберите карту",
                                    style = AppTypography.bodyMedium16
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_down),
                                    contentDescription = null
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                payments.forEach { payment ->
                                    DropdownMenuItem(
                                        text = { Text(payment.cardName) },
                                        onClick = {
                                            checkoutViewModel.updatePaymentId(payment.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SummaryBlock(
                            subtotal = cartState.items.sumOf { it.product.price * it.count },
                            delivery = checkoutState.deliveryCost.toDouble(),
                            total = cartState.items.sumOf { it.product.price * it.count } +
                                    checkoutState.deliveryCost
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) } // небольшой отступ над кнопкой
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
            label = { Text(stringResource(R.string.address)) },
            modifier = Modifier.fillMaxWidth()
        )
        // здесь позже можно добавить карту и способ оплаты
    }
}
