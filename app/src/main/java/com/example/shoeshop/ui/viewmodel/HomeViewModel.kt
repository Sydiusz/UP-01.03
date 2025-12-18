package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.FavouriteRepository
import com.example.shoeshop.data.repository.ProductsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"
private const val LOG_PREFIX = "[SHOES_APP]"

data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val popularProducts: List<Product> = emptyList(),
    val bestSellers: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: String = ""
)

class HomeViewModel(
    private val repository: ProductsRepository = ProductsRepository(),
    private val favouriteRepository: FavouriteRepository = FavouriteRepository()
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
        if (_isDataLoaded.value) return
        // Если данные уже загружены, повторно не грузим
        if (_isDataLoaded.value) {
            Log.d(TAG, "$LOG_PREFIX Данные уже загружены, пропускаем loadData()")
            return
        }

        Log.d(TAG, "$LOG_PREFIX Начало загрузки данных...")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Запускаем запрос категорий и товаров параллельно
                val categoriesDeferred = async { repository.getCategories() }
                val bestSellersDeferred = async { repository.getBestSellers() }

                val (categoriesResult, bestSellersResult) = awaitAll(
                    categoriesDeferred,
                    bestSellersDeferred
                )

                // 1. Категории
                if (categoriesResult is Result<*>) {
                    val categoriesRes = categoriesResult as Result<List<Category>>
                    if (categoriesRes.isSuccess) {
                        val categories = categoriesRes.getOrDefault(emptyList())
                        val allCategory = Category(id = "all", name = "Все", isSelected = false)
                        val updatedCategories = listOf(allCategory) + categories

                        _uiState.update { state ->
                            state.copy(categories = updatedCategories)
                        }
                    }
                }

                // 2. Бестселлеры
                var bestSellers: List<Product> = emptyList()
                if (bestSellersResult is Result<*>) {
                    val bestRes = bestSellersResult as Result<List<Product>>
                    if (bestRes.isSuccess) {
                        bestSellers = bestRes.getOrDefault(emptyList())
                    }
                }

                // 3. Подставляем избранное
                val userId = SessionManager.userId
                if (userId != null && bestSellers.isNotEmpty()) {
                    val favIdsResult = favouriteRepository.getFavoritesForUser(userId)
                    val favIds = favIdsResult.getOrDefault(emptyList())
                    bestSellers = bestSellers.map { p ->
                        p.copy(isFavorite = favIds.contains(p.id))
                    }
                }

                _uiState.update { state ->
                    state.copy(popularProducts = bestSellers)
                }

                _isDataLoaded.value = true

            } catch (e: Exception) {
                Log.e(TAG, "$LOG_PREFIX Ошибка загрузки: ${e.message}", e)
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

    suspend fun loadCategoryProducts(categoryName: String): Result<List<Product>> {
        return try {
            android.util.Log.d(TAG, "Загрузка товаров категории: '$categoryName'")

            if (categoryName == "Все") {
                android.util.Log.d(TAG, "Загружаем ВСЕ товары через getAllProducts()")
                repository.getAllProducts()
            } else {
                // Берём категории прямо из репозитория, а не из uiState
                val categoriesResult = repository.getCategories()
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrDefault(emptyList())
                    val category = categories.firstOrNull { it.name == categoryName }

                    android.util.Log.d(TAG, "Найденная категория: $category")

                    if (category != null) {
                        repository.getProductsByCategory(category.id)
                    } else {
                        Result.failure(Exception("Категория не найдена: $categoryName"))
                    }
                } else {
                    Result.failure(Exception("Не удалось загрузить категории"))
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

    fun toggleFavorite(product: Product) {
        val userId = SessionManager.userId ?: return

        viewModelScope.launch {
            val favResult = favouriteRepository.isFavorite(userId, product.id)
            val isFavoriteInDb = favResult.getOrDefault(false)

            _uiState.update { state ->
                val newPopular = state.popularProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = !isFavoriteInDb) else it
                }
                state.copy(popularProducts = newPopular)
            }

            val result = if (!isFavoriteInDb) {
                favouriteRepository.addFavorite(userId, product.id)
            } else {
                favouriteRepository.removeFavorite(userId, product.id)
            }

            if (result.isFailure) {
                _uiState.update { state ->
                    val newPopular = state.popularProducts.map {
                        if (it.id == product.id) it.copy(isFavorite = isFavoriteInDb) else it
                    }
                    state.copy(popularProducts = newPopular)
                }
            }
        }
    }
    fun resetSelectedCategory() {
        _uiState.update { state ->
            state.copy(
                selectedCategory = ""
            )
        }
    }
}
