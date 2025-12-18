package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    homeViewModel: HomeViewModel,
    categoryName: String,
    onProductClick: (Product) -> Unit,
    onBackClick: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val categoryProducts = remember(categoryName) {
        mutableStateOf<List<Product>>(emptyList())
    }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(categoryName) {
        // если категорий нет – загружаем
        if (uiState.categories.isEmpty()) {
            homeViewModel.loadData()
        }

        isLoading = true
        val result = homeViewModel.loadCategoryProducts(categoryName)
        if (result.isSuccess) {
            // товары категории; при желании здесь можно проставить isFavorite
            categoryProducts.value = result.getOrDefault(emptyList())
        } else {
            android.util.Log.e("CategoryScreen", "Ошибка: ${result.exceptionOrNull()?.message}")
        }
        isLoading = false

        homeViewModel.selectCategory(categoryName)
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
                    BackButton(
                        onClick = {
                            homeViewModel.resetSelectedCategory()
                            onBackClick()
                        }
                    )
                }

            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                categoryProducts.value.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        CategorySection(
                            categories = uiState.categories,
                            selectedCategory = categoryName,
                            onCategorySelected = { newCategoryName ->
                                onCategorySelected(newCategoryName)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        // полоска с категориями
                        CategorySection(
                            categories = uiState.categories,
                            selectedCategory = categoryName,
                            onCategorySelected = { newCategoryName ->
                                onCategorySelected(newCategoryName)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                                    onFavoriteClick = {
                                        homeViewModel.toggleFavorite(product)
                                        categoryProducts.value = categoryProducts.value.map {
                                            if (it.id == product.id) it.copy(isFavorite = !it.isFavorite) else it
                                        }
                                    },
                                    onAddToCartClick = {
                                        homeViewModel.toggleCart(product)
                                        categoryProducts.value = categoryProducts.value.map {
                                            if (it.id == product.id) it.copy(isInCart = !it.isInCart) else it
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
