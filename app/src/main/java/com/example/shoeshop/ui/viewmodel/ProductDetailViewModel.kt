package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.CartRepository
import com.example.shoeshop.data.repository.FavouriteRepository
import com.example.shoeshop.data.repository.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repository: ProductsRepository = ProductsRepository(),
    private val favouriteRepository: FavouriteRepository = FavouriteRepository(),
    private val cartRepository: CartRepository = CartRepository()   // 👈 ДОБАВИЛИ
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _related = MutableStateFlow<List<Product>>(emptyList())
    val related: StateFlow<List<Product>> = _related.asStateFlow()

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.getProductById(productId)
                if (result.isSuccess) {
                    var loaded = result.getOrNull()

                    // категория
                    if (loaded?.categoryId != null) {
                        val categoriesResult = repository.getCategories()
                        if (categoriesResult.isSuccess) {
                            val categories = categoriesResult.getOrDefault(emptyList())
                            val category = categories.firstOrNull { it.id == loaded.categoryId }
                            if (category != null) {
                                loaded = loaded.copy(displayCategory = category.name)
                            }
                        }
                    }

                    val userId = SessionManager.userId

                    // избранное
                    if (userId != null && loaded != null) {
                        val isFavResult = favouriteRepository.isFavorite(userId, loaded.id)
                        val isFav = isFavResult.getOrDefault(false)
                        loaded = loaded.copy(isFavorite = isFav)
                    }

                    // КОРЗИНА 👇
                    if (userId != null && loaded != null) {
                        val cartResult = cartRepository.getCartItemsForCurrentUser()
                        if (cartResult.isSuccess) {
                            val cartItems = cartResult.getOrDefault(emptyList())
                            val productIdsInCart = cartItems.map { it.product_id }.toSet()
                            loaded = loaded.copy(
                                isInCart = productIdsInCart.contains(loaded.id)
                            )
                        }
                    }

                    _product.value = loaded
                    loadRelated(loaded)
                } else {
                    _error.value = result.exceptionOrNull()?.message
                        ?: "Не удалось загрузить товар"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    private suspend fun loadRelated(base: Product?) {
        if (base?.categoryId == null) return
        try {
            val allResult = repository.getProductsByCategory(base.categoryId!!)
            if (allResult.isSuccess) {
                var list = allResult.getOrDefault(emptyList())
                    .map { it.copy(displayCategory = base.displayCategory) }

                val userId = SessionManager.userId

                // избранное
                if (userId != null && list.isNotEmpty()) {
                    val favIdsResult = favouriteRepository.getFavoritesForUser(userId)
                    val favIds = favIdsResult.getOrDefault(emptyList())
                    list = list.map { p ->
                        p.copy(isFavorite = favIds.contains(p.id))
                    }
                }

                // КОРЗИНА ДЛЯ related 👇
                if (userId != null && list.isNotEmpty()) {
                    val cartResult = cartRepository.getCartItemsForCurrentUser()
                    if (cartResult.isSuccess) {
                        val cartItems = cartResult.getOrDefault(emptyList())
                        val idsInCart = cartItems.map { it.product_id }.toSet()
                        list = list.map { p ->
                            p.copy(isInCart = idsInCart.contains(p.id))
                        }
                    }
                }

                _related.value = list

                val idx = list.indexOfFirst { it.id == base.id }
                _selectedIndex.value = if (idx >= 0) idx else 0
            }
        } catch (_: Exception) { }
    }



    fun toggleFavorite(product: Product) {
        val userId = SessionManager.userId ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            val favResult = favouriteRepository.isFavorite(userId, product.id)
            val isFavoriteInDb = favResult.getOrDefault(false)
            val newIsFavorite = !isFavoriteInDb

            // обновляем основной продукт
            _product.value = _product.value?.copy(isFavorite = newIsFavorite)

            // обновляем тот же товар в related
            _related.value = _related.value.map {
                if (it.id == product.id) it.copy(isFavorite = newIsFavorite) else it
            }

            val result = if (newIsFavorite) {
                favouriteRepository.addFavorite(userId, product.id)
            } else {
                favouriteRepository.removeFavorite(userId, product.id)
            }

            if (result.isFailure) {
                // откат
                _product.value = _product.value?.copy(isFavorite = isFavoriteInDb)
                _related.value = _related.value.map {
                    if (it.id == product.id) it.copy(isFavorite = isFavoriteInDb) else it
                }
                _error.value =
                    "Не удалось обновить избранное: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    fun selectRelated(index: Int) {
        _selectedIndex.value = index
    }

}
