package com.example.shoeshop.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val popularProducts: List<Product> = emptyList(),
    val bestSellers: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: String = "Все"
)

class HomeViewModel(
    private val repository: ProductsRepository = ProductsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Загружаем категории
                val categoriesResult = repository.getCategories()
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrDefault(emptyList())
                    val allCategory = Category(id = "all", name = "Все", isSelected = true)
                    val updatedCategories = listOf(allCategory) + categories

                    _uiState.update { state ->
                        state.copy(categories = updatedCategories)
                    }
                }

                // Загружаем популярные товары (best sellers)
                val bestSellersResult = repository.getBestSellers()
                if (bestSellersResult.isSuccess) {
                    val bestSellers = bestSellersResult.getOrDefault(emptyList())
                    // Преобразуем для отображения
                    val formattedBestSellers = bestSellers.map { product ->
                        product.copy(
                            category = "BEST SELLER",
                            price = product.price // уже Double из базы
                        )
                    }
                    _uiState.update { state ->
                        state.copy(bestSellers = formattedBestSellers)
                    }
                }

                // Загружаем все товары для секции "Популярное"
                val allProductsResult = repository.getAllProducts()
                if (allProductsResult.isSuccess) {
                    val allProducts = allProductsResult.getOrDefault(emptyList())
                    // Берем первые 4 как популярные
                    val popular = allProducts.take(4).map { product ->
                        product.copy(
                            category = when(product.isBestSeller) {
                                true -> "BEST SELLER"
                                else -> "NEW"
                            },
                            price = product.price
                        )
                    }
                    _uiState.update { state ->
                        state.copy(popularProducts = popular)
                    }
                }

            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(errorMessage = "Ошибка загрузки: ${e.message}")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectCategory(categoryName: String) {
        _uiState.update { state ->
            val updatedCategories = state.categories.map { category ->
                category.copy(isSelected = category.name == categoryName)
            }
            state.copy(
                categories = updatedCategories,
                selectedCategory = categoryName
            )
        }

        // Если выбрана не "Все", загружаем товары этой категории
        if (categoryName != "Все") {
            val categoryId = _uiState.value.categories
                .firstOrNull { it.name == categoryName }
                ?.id

            if (categoryId != null && categoryId != "all") {
                loadCategoryProducts(categoryId)
            }
        } else {
            // Если выбрана "Все", показываем все популярные товары
            loadAllProducts()
        }
    }

    private fun loadCategoryProducts(categoryId: String) {
        viewModelScope.launch {
            val result = repository.getProductsByCategory(categoryId)
            if (result.isSuccess) {
                val categoryProducts = result.getOrDefault(emptyList())
                // Обновляем популярные товары на товары категории
                _uiState.update { state ->
                    state.copy(popularProducts = categoryProducts)
                }
            }
        }
    }

    private fun loadAllProducts() {
        viewModelScope.launch {
            val result = repository.getAllProducts()
            if (result.isSuccess) {
                val allProducts = result.getOrDefault(emptyList())
                // Берем первые 4 как популярные
                val popular = allProducts.take(4)
                _uiState.update { state ->
                    state.copy(popularProducts = popular)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}