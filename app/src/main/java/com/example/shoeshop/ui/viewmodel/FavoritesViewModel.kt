package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Product
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
    private val favouriteRepository: FavouriteRepository = FavouriteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val userId = SessionManager.userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = favouriteRepository.getFavoriteProducts(userId)
            if (result.isSuccess) {
                _uiState.update { it.copy(products = result.getOrDefault(emptyList())) }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleFavorite(product: Product) {
        val userId = SessionManager.userId ?: return
        viewModelScope.launch {
            val isFavResult = favouriteRepository.isFavorite(userId, product.id)
            val isFav = isFavResult.getOrDefault(false)

            // оптимистично: убираем/добавляем в список
            if (isFav) {
                _uiState.update { state ->
                    state.copy(products = state.products.filterNot { it.id == product.id })
                }
                favouriteRepository.removeFavorite(userId, product.id)
            } else {
                favouriteRepository.addFavorite(userId, product.id)
                loadFavorites()
            }
        }
    }
}
