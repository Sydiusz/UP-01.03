// ui/viewmodel/FavoritesViewModel.kt
package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.CartRepository
import com.example.shoeshop.data.repository.FavouriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null
)

class FavoritesViewModel(
    private val favouriteRepository: FavouriteRepository = FavouriteRepository(),
    private val cartRepository: CartRepository = CartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val userId = SessionManager.userId ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // 1. получаем избранные товары
                val favProductsResult = favouriteRepository.getFavoriteProducts(userId)
                if (!favProductsResult.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = favProductsResult.exceptionOrNull()?.message
                        )
                    }
                    return@launch
                }

                var products = favProductsResult.getOrDefault(emptyList())

                // 2. подставляем флаг корзины по таблице cart
                val cartResult = cartRepository.getCartItemsForCurrentUser()
                if (cartResult.isSuccess) {
                    val cartItems = cartResult.getOrDefault(emptyList())
                    val idsInCart = cartItems.map { it.product_id }.toSet()
                    products = products.map { p ->
                        p.copy(isInCart = idsInCart.contains(p.id))
                    }
                }

                _uiState.update { it.copy(isLoading = false, products = products) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun toggleFavorite(product: Product) {
        val userId = SessionManager.userId ?: return

        viewModelScope.launch {
            val favResult = favouriteRepository.isFavorite(userId, product.id)
            val isFavoriteInDb = favResult.getOrDefault(false)

            // оптимистично меняем локальный список избранного
            _uiState.update { state ->
                state.copy(
                    products = state.products.map { p ->
                        if (p.id == product.id) p.copy(isFavorite = !isFavoriteInDb) else p
                    }
                )
            }

            val result = if (!isFavoriteInDb) {
                favouriteRepository.addFavorite(userId, product.id)
            } else {
                favouriteRepository.removeFavorite(userId, product.id)
            }

            // если запрос провалился — откатываем
            if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        products = state.products.map { p ->
                            if (p.id == product.id) p.copy(isFavorite = isFavoriteInDb) else p
                        }
                    )
                }
            }
        }
    }

    // локальное переключение флага корзины в избранном
    fun toggleCartLocal(productId: String) {
        _uiState.update { state ->
            state.copy(
                products = state.products.map { p ->
                    if (p.id == productId) p.copy(isInCart = !p.isInCart) else p
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
