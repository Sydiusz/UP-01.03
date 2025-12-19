// ui/viewmodel/CartViewModel.kt
package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.CartRepository
import com.example.shoeshop.data.repository.ProductsRepository
import com.example.shoeshop.ui.screens.CartUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartScreenState(
    val isLoading: Boolean = false,
    val items: List<CartUiItem> = emptyList(),
    val errorMessage: String? = null
)

class CartViewModel(
    private val cartRepository: CartRepository = CartRepository(),
    private val productsRepository: ProductsRepository = ProductsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartScreenState())
    val uiState: StateFlow<CartScreenState> = _uiState

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val userId = SessionManager.userId
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val cartResult = cartRepository.getCartItemsForCurrentUser()
            if (cartResult.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = cartResult.exceptionOrNull()?.message
                    )
                }
                return@launch
            }

            val cartItems = cartResult.getOrDefault(emptyList())

            val productIds: Set<String> = cartItems.map { it.product_id }.toSet()
            val productsResult: Result<List<Product>> =
                productsRepository.getProductsByIds(productIds.toList())

            if (productsResult.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = productsResult.exceptionOrNull()?.message
                    )
                }
                return@launch
            }

            val products: List<Product> = productsResult.getOrDefault(emptyList())
            val byId: Map<String, Product> = products.associateBy { it.id }

            val uiItems: List<CartUiItem> = cartItems.mapNotNull { cartItem ->
                val product = byId[cartItem.product_id] ?: return@mapNotNull null
                CartUiItem(
                    id = cartItem.id,
                    product = product.copy(isInCart = true),
                    count = cartItem.count.toInt()
                )
            }

            _uiState.update { state ->
                state.copy(isLoading = false, items = uiItems)
            }
        }
    }

    fun increment(item: CartUiItem) {
        viewModelScope.launch {
            // локально
            _uiState.update { state ->
                state.copy(
                    items = state.items.map {
                        if (it.id == item.id) it.copy(count = it.count + 1) else it
                    }
                )
            }

            // на сервере (обновить count)
            cartRepository.updateCount(item.id, item.count + 1)
        }
    }

    fun decrement(item: CartUiItem) {
        if (item.count <= 1) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    items = state.items.map {
                        if (it.id == item.id) it.copy(count = it.count - 1) else it
                    }
                )
            }

            cartRepository.updateCount(item.id, item.count - 1)
        }
    }

    fun remove(item: CartUiItem) {
        viewModelScope.launch {
            // оптимистично удаляем
            val old = _uiState.value.items
            _uiState.update { it.copy(items = it.items.filterNot { it.id == item.id }) }

            val result = cartRepository.removeFromCartById(item.id)
            if (result.isFailure) {
                // откат
                _uiState.update { it.copy(items = old) }
            }
        }
    }
    fun clearCart() {
        val currentItems = uiState.value.items

        // оптимистично очищаем UI
        _uiState.value = _uiState.value.copy(items = emptyList())

        viewModelScope.launch {
            currentItems.forEach { item ->
                val result = cartRepository.removeFromCartById(item.id)
                if (result.isFailure) {
                    loadCart()
                    return@launch
                }
            }
        }
    }
    fun addProductFromOrder(
        productId: String?,
        title: String?,
        price: Double,
        count: Long
    ) {
        if (productId == null) return

        viewModelScope.launch {
            val userId = SessionManager.userId ?: return@launch

            // 1. Создаём записи в корзине на сервере (count раз)
            repeat(count.toInt().coerceAtLeast(1)) {
                // если у CartRepository есть метод addToCart(productId: String)
                cartRepository.addToCart(productId)
            }

            // 2. Перечитываем корзину, чтобы uiState.items обновился
            loadCart()
        }
    }


}
