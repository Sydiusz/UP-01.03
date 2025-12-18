package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.data.repository.FavouriteRepository
import com.example.shoeshop.data.repository.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val repository: ProductsRepository = ProductsRepository(),
    private val favouriteRepository: FavouriteRepository = FavouriteRepository()
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
                val result = repository.getProductById(productId)
                if (result.isSuccess) {
                    var loaded = result.getOrNull()

                    // если есть categoryId — подтянем имя категории
                    if (loaded?.categoryId != null) {
                        try {
                            val categoriesResult = repository.getCategories()
                            if (categoriesResult.isSuccess) {
                                val categories = categoriesResult.getOrDefault(emptyList())
                                val category = categories.firstOrNull { it.id == loaded.categoryId }
                                if (category != null) {
                                    loaded = loaded.copy(displayCategory = category.name)
                                }
                            }
                        } catch (_: Exception) {
                            // если не получилось, просто оставим id
                        }
                    }

                    // подтягиваем флаг избранного
                    val userId = SessionManager.userId
                    if (userId != null && loaded != null) {
                        try {
                            val isFavResult = favouriteRepository.isFavorite(userId, loaded.id)
                            val isFav = isFavResult.getOrDefault(false)
                            loaded = loaded.copy(isFavorite = isFav)
                        } catch (_: Exception) {
                            // если не получилось, просто не трогаем isFavorite
                        }
                    }

                    _product.value = loaded
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

    fun toggleFavorite(product: Product) {
        val userId = SessionManager.userId ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            val favResult = favouriteRepository.isFavorite(userId, product.id)
            val isFavoriteInDb = favResult.getOrDefault(false)

            // оптимистично обновляем UI
            _product.value = product.copy(isFavorite = !isFavoriteInDb)

            val result = if (!isFavoriteInDb) {
                favouriteRepository.addFavorite(userId, product.id)
            } else {
                favouriteRepository.removeFavorite(userId, product.id)
            }

            if (result.isFailure) {
                // откат, если запрос не удался
                _product.value = product
                _error.value =
                    "Не удалось обновить избранное: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}
