package com.example.shoeshop.data.repository

import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product

class ProductsRepository {
    private val productsService = RetrofitInstance.productsService
    private val categoriesService = RetrofitInstance.categoriesService

    companion object {
        private const val TAG = "ProductsRepository"
    }

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val response = productsService.getProducts()
            android.util.Log.d(TAG, "getAllProducts: код ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d(TAG, "getAllProducts: получено ${body?.size ?: 0} товаров")

                // Подробное логирование структуры
                body?.take(3)?.forEachIndexed { index, product ->
                    android.util.Log.d(TAG,
                        "Товар $index: " +
                                "id=${product.id}, " +
                                "name=${product.name}, " +
                                "price=${product.price}, " +
                                "categoryId=${product.categoryId}, " +
                                "isBestSeller=${product.isBestSeller}, " +
                                "description=${product.description.take(30)}..."
                    )
                }

                // Если товары есть, но categoryId = null, проверьте модель Product
                body?.firstOrNull()?.let { firstProduct ->
                    if (firstProduct.categoryId == null) {
                        android.util.Log.w(TAG, "ВНИМАНИЕ: categoryId = null! Проверьте модель Product")
                    }
                }

                Result.success(body ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Ошибка getAllProducts: ${response.code()}, $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Исключение getAllProducts: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getBestSellers(): Result<List<Product>> {
        return try {
            // Простая фильтрация на клиенте
            val allProductsResult = getAllProducts()
            if (allProductsResult.isSuccess) {
                val allProducts = allProductsResult.getOrDefault(emptyList())
                val bestSellers = allProducts.filter { it.isBestSeller }
                android.util.Log.d(TAG, "getBestSellers: из ${allProducts.size} товаров найдено ${bestSellers.size} бестселлеров")
                Result.success(bestSellers)
            } else {
                allProductsResult
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Исключение getBestSellers: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = categoriesService.getCategories()
            android.util.Log.d(TAG, "getCategories: код ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d(TAG, "getCategories: получено ${body?.size ?: 0} категорий")
                body?.forEach { category ->
                    android.util.Log.d(TAG, "Категория: id=${category.id}, name=${category.name}")
                }
                Result.success(body ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Ошибка getCategories: ${response.code()}, $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Исключение getCategories: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(categoryId: String): Result<List<Product>> {
        return try {
            // ДОБАВЬТЕ "eq." перед categoryId
            val filter = "eq.$categoryId"
            android.util.Log.d(TAG, "Запрос товаров с фильтром: $filter")

            // УБЕДИТЕСЬ, что передаете параметр
            val response = productsService.getProductsByCategory(
                categoryId = filter  // ← ВАЖНО: передаем параметр!
            )

            android.util.Log.d(TAG, "Ответ по категории: код ${response.code()}")
            android.util.Log.d(TAG, "Ответ по категории: успешно ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d(TAG, "Получено товаров категории: ${body?.size ?: 0}")
                Result.success(body ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Ошибка HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Failed to load category products: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Исключение в getProductsByCategory: ${e.message}", e)
            Result.failure(e)
        }
    }
}