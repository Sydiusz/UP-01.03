// screens/HomeScreen.kt
package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.shoeshop.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.ui.components.ProductCard
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.FavoritesViewModel
import com.example.shoeshop.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }

    // Home VM
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val favoritesUiState by favoritesViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
            ) {
                // Фоновая картинка
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.vector_1789),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Контент меню поверх картинки
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Левая группа иконок
                    Row {
                        IconButton(onClick = { selected = 0 }) {
                            Icon(
                                painter = painterResource(id = R.drawable.home),
                                contentDescription = "Home",
                                tint = if (selected == 0) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = {
                            selected = 1
                            favoritesViewModel.loadFavorites()   // ← вот это важно
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.favorite),
                                contentDescription = "Favorites",
                                tint = if (selected == 1) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }
                    }

                    // Центральная кнопка корзины
                    Box(
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { onCartClick() },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.bag_2),
                                contentDescription = "Cart",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Правая группа иконок
                    Row {
                        IconButton(onClick = { selected = 2 }) {
                            Icon(
                                painter = painterResource(id = R.drawable.notification),
                                contentDescription = "Notification",
                                tint = if (selected == 2) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { selected = 3 }) {
                            Icon(
                                painter = painterResource(id = R.drawable.profile),
                                contentDescription = "Profile",
                                tint = if (selected == 3) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Индикатор загрузки для Home
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Ошибка Home
        uiState.errorMessage?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Ошибка загрузки") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Верхняя панель с заголовком, поиском и настройками (только для главной вкладки)
            if (selected == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.home),
                        style = AppTypography.headingRegular32,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Center
                    )

                    // Строка с поиском и настройками
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Поле поиска
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { onSearchClick() } // по нажатию открываем поиск
                        ) {
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                placeholder = {
                                    Text(
                                        text = "Поиск...",
                                        style = AppTypography.bodyRegular14
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Поиск",
                                        tint = Color.Gray
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Gray,
                                    unfocusedBorderColor = Color.LightGray,
                                    disabledBorderColor = Color.LightGray,
                                    disabledContainerColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Иконка настроек
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onSettingsClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.sliders),
                                contentDescription = "Настройки",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Основной контент
            Box(modifier = Modifier.fillMaxSize()) {
                when (selected) {
                    0 -> {
                        // Главная: категории + популярное + акции
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            // Секция: Категории
                            item {
                                CategorySection(
                                    categories = uiState.categories,
                                    selectedCategory = uiState.selectedCategory,
                                    onCategorySelected = { categoryName ->
                                        viewModel.selectCategory(categoryName)
                                        onCategoryClick(categoryName) // Навигация на экран категории
                                    }
                                )
                            }

                            // Секция: Популярное
                            item {
                                PopularSection(
                                    products = uiState.popularProducts,
                                    onProductClick = onProductClick,
                                    onFavoriteClick = { product ->
                                        viewModel.toggleFavorite(product)
                                    }
                                )
                            }

                            // Секция: Акции
                            item {
                                PromotionsSection()
                            }
                        }
                    }

                    1 -> {
                        // Вкладка "Избранное" – используем готовый экран FavoritesScreen
                        FavoritesScreen(
                            onBackClick = { selected = 0 }, // назад — на главную вкладку
                            onProductClick = onProductClick
                        )
                    }

                    2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Уведомления",
                                style = AppTypography.headingRegular32
                            )
                        }
                    }

                    3 -> {
                        ProfileScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    categories: List<Category>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(id = R.string.categories),
            style = AppTypography.bodyMedium16,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category.name,
                    isSelected = selectedCategory == category.name,
                    onClick = { onCategorySelected(category.name) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5),
        contentColor = if (isSelected) Color.White else Color.Black
    ) {
        Text(
            text = category,
            style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PopularSection(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onFavoriteClick: (Product) -> Unit
) {
    Column {
        // Заголовок раздела
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.popular),
                style = AppTypography.bodyMedium16,
            )
            Text(
                text = "Все",
                style = AppTypography.bodyRegular12,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    // Можно добавить навигацию на все популярные товары
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Проверяем, есть ли товары
        if (products.isEmpty()) {
            Text(
                text = "Нет товаров",
                style = AppTypography.bodyRegular14,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // Список товаров
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { onProductClick(product) },
                        onFavoriteClick = { onFavoriteClick(product) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionsSection() {
    Column {
        Text(
            text = stringResource(id = R.string.sales),
            style = AppTypography.bodyMedium16,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Summer Sale",
                        style = AppTypography.headingRegular32.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "15% OFF",
                        style = AppTypography.headingRegular32.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                TextButton(
                    onClick = {
                        // Навигация на акции
                    },
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Смотреть",
                        style = AppTypography.bodyMedium16.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onProductClick = {},
        onCartClick = {},
        onSearchClick = {},
        onSettingsClick = {},
        onCategoryClick = {}
    )
}
