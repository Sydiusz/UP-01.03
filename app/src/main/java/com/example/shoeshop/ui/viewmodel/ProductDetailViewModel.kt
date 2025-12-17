package com.example.shoeshop.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repository: ProductsRepository = ProductsRepository()
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Предполагаем, что у репозитория есть метод getProductById
                // Если нет - нужно его добавить
                val result = repository.getProductById(productId)
                if (result.isSuccess) {
                    _product.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Не удалось загрузить товар"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            // TODO: Обновить состояние избранного в репозитории
            _product.value = product.copy(isFavorite = !product.isFavorite)
        }
    }
}