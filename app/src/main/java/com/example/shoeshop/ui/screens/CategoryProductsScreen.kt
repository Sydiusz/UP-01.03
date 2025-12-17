package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.ui.components.BackButton
import com.example.shoeshop.ui.components.ProductCard
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoryName: String,
    onProductClick: (Product) -> Unit,
    onBackClick: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Загружаем товары категории
    val categoryProducts = remember(categoryName) {
        mutableStateOf<List<Product>>(emptyList())
    }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(categoryName) {
        // Если категорий нет - загружаем
        if (uiState.categories.isEmpty()) {
            viewModel.loadData()
            // Подождем пока загрузятся
            delay(1000)
        }

        isLoading = true
        val result = viewModel.loadCategoryProducts(categoryName)
        if (result.isSuccess) {
            categoryProducts.value = result.getOrDefault(emptyList())
        } else {
            // Логируем ошибку
            android.util.Log.e("CategoryScreen", "Ошибка: ${result.exceptionOrNull()?.message}")
        }
        isLoading = false

        viewModel.selectCategory(categoryName)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        style = AppTypography.headingRegular32
                    )
                },
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Полоска с категориями
                CategorySection(
                    categories = uiState.categories,
                    selectedCategory = categoryName,
                    onCategorySelected = { newCategoryName ->
                        onCategorySelected(newCategoryName)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Товары категории в 2 колонки
                if (categoryProducts.value.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет товаров в категории",
                            style = AppTypography.bodyRegular14,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(categoryProducts.value) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = { onProductClick(product) },
                                onFavoriteClick = { /* обработка избранного */ },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}