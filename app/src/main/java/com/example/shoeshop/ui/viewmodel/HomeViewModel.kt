package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.CartRepository
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
    private val favouriteRepository: FavouriteRepository = FavouriteRepository(),
    private val cartRepository: CartRepository = CartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    init {
        Log.d(TAG, "$LOG_PREFIX HomeViewModel инициализирован")
        loadData()
    }

    fun loadData() {
        if (_isDataLoaded.value) {
            Log.d(TAG, "$LOG_PREFIX Данные уже загружены, пропускаем loadData()")
            return
        }

        Log.d(TAG, "$LOG_PREFIX Начало загрузки данных...")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val categoriesDeferred = async { repository.getCategories() }
                val bestSellersDeferred = async { repository.getBestSellers() }

                val results = awaitAll(categoriesDeferred, bestSellersDeferred)
                val categoriesResult = results[0] as Result<List<Category>>
                val bestSellersResult = results[1] as Result<List<Product>>

                // 1. Категории
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrDefault(emptyList())
                    val allCategory = Category(id = "all", name = "Все", isSelected = false)
                    val updatedCategories = listOf(allCategory) + categories

                    _uiState.update { state ->
                        state.copy(categories = updatedCategories)
                    }
                }

                // 2. Бестселлеры
                var bestSellers = if (bestSellersResult.isSuccess) {
                    bestSellersResult.getOrDefault(emptyList())
                } else {
                    emptyList()
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

                // 4. Подставляем флаг корзины
                syncCartFlags()

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

    private suspend fun syncCartFlags() {
        val cartResult = cartRepository.getCartItemsForCurrentUser()
        if (cartResult.isSuccess) {
            val cartItems = cartResult.getOrDefault(emptyList())
            val productIdsInCart = cartItems.map { it.product_id }.toSet()

            _uiState.update { state ->
                state.copy(
                    popularProducts = state.popularProducts.map { p ->
                        p.copy(isInCart = productIdsInCart.contains(p.id))
                    }
                )
            }
        }
    }
    fun refreshCartFlags() {
        viewModelScope.launch {
            syncCartFlags()
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
            val baseResult = if (categoryName == "Все") {
                repository.getAllProducts()
            } else {
                val category = uiState.value.categories
                    .firstOrNull { it.name == categoryName && it.id != "all" }

                if (category != null) {
                    repository.getProductsByCategory(category.id)
                } else {
                    return Result.failure(Exception("Категория не найдена: $categoryName"))
                }
            }

            if (baseResult.isFailure) return baseResult

            var products = baseResult.getOrDefault(emptyList())

            // избранное
            val userId = SessionManager.userId
            if (userId != null && products.isNotEmpty()) {
                val favIdsResult = favouriteRepository.getFavoritesForUser(userId)
                val favIds = favIdsResult.getOrDefault(emptyList())
                products = products.map { p ->
                    p.copy(isFavorite = favIds.contains(p.id))
                }
            }

            // флаг корзины
            val cartResult = cartRepository.getCartItemsForCurrentUser()
            if (cartResult.isSuccess) {
                val cartItems = cartResult.getOrDefault(emptyList())
                val productIdsInCart = cartItems.map { it.product_id }.toSet()
                products = products.map { p ->
                    p.copy(isInCart = productIdsInCart.contains(p.id))
                }
            }

            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toggleCart(product: Product) {
        val userId = SessionManager.userId ?: return

        viewModelScope.launch {
            // берём актуальное значение из состояния
            val current = _uiState.value.popularProducts.firstOrNull { it.id == product.id }
            val currentlyInCart = current?.isInCart ?: false

            // оптимистично обновляем UI
            _uiState.update { state ->
                state.copy(
                    popularProducts = state.popularProducts.map { p ->
                        if (p.id == product.id) p.copy(isInCart = !currentlyInCart) else p
                    }
                )
            }

            val result = if (!currentlyInCart) {
                cartRepository.addToCart(product.id)
            } else {
                cartRepository.removeFromCartByProduct(product.id)
            }

            if (result.isFailure) {
                // откат
                _uiState.update { state ->
                    state.copy(
                        popularProducts = state.popularProducts.map { p ->
                            if (p.id == product.id) p.copy(isInCart = currentlyInCart) else p
                        }
                    )
                }
            }
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

            // оптимистично
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
                // откат
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
