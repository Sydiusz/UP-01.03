package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Константы для тегов логов
private const val TAG = "HomeViewModel"
private const val LOG_PREFIX = "[SHOES_APP]"

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

    private var _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    init {
        Log.d(TAG, "$LOG_PREFIX HomeViewModel инициализирован")
        loadData()
    }

    fun loadData() {
        Log.d(TAG, "$LOG_PREFIX Начало загрузки данных...")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 1. Загружаем категории В ПЕРВУЮ ОЧЕРЕДЬ
                val categoriesResult = repository.getCategories()
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrDefault(emptyList())
                    // Добавляем категорию "Все" первой и НЕ выбираем ее
                    val allCategory = Category(id = "all", name = "Все", isSelected = false)
                    val updatedCategories = listOf(allCategory) + categories

                    _uiState.update { state ->
                        state.copy(categories = updatedCategories)
                    }

                    // Помечаем что категории загружены
                    _isDataLoaded.value = true
                }

                // 2. Загружаем бестселлеры
                val bestSellersResult = repository.getBestSellers()
                if (bestSellersResult.isSuccess) {
                    val bestSellers = bestSellersResult.getOrDefault(emptyList())
                    _uiState.update { state ->
                        state.copy(popularProducts = bestSellers)
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
        Log.d(TAG, "$LOG_PREFIX Выбрана категория: $categoryName")

        _uiState.update { state ->
            val updatedCategories = state.categories.map { category ->
                category.copy(isSelected = category.name == categoryName)
            }
            state.copy(
                categories = updatedCategories,
                selectedCategory = categoryName
            )
        }
    }

    // Метод для загрузки товаров категории (теперь безопасен)
    suspend fun loadCategoryProducts(categoryName: String): Result<List<Product>> {
        return try {
            android.util.Log.d(TAG, "Загрузка товаров категории: '$categoryName'")
            android.util.Log.d(TAG, "Категории доступны: ${uiState.value.categories.size}")

            if (categoryName == "Все") {
                // Для категории "Все" загружаем все товары
                android.util.Log.d(TAG, "Загружаем ВСЕ товары")
                repository.getAllProducts()
            } else {
                // Находим ID категории по имени
                val category = uiState.value.categories
                    .firstOrNull { it.name == categoryName }

                android.util.Log.d(TAG, "Найденная категория: $category")

                if (category != null && category.id != "all") {
                    android.util.Log.d(TAG, "Загружаем товары по categoryId: ${category.id}")
                    repository.getProductsByCategory(category.id)
                } else {
                    android.util.Log.e(TAG, "Категория не найдена или ID = 'all': $categoryName")
                    Result.failure(Exception("Категория не найдена: $categoryName"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Исключение в loadCategoryProducts: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}