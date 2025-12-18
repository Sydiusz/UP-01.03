package com.example.shoeshop.data.repository

import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product
import java.util.Collections.emptyList

class ProductsRepository {
    private val productsService = RetrofitInstance.productsService
    private val categoriesService = RetrofitInstance.categoriesService

    companion object {
        private const val TAG = "ProductsRepository"
    }

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val response = productsService.getProducts(
                select = "id,title,cost,description,category_id,is_best_seller" // только нужные поля
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
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

    // data/repository/ProductsRepository.kt
    suspend fun getProductById(productId: String): Result<Product> {
        return try {
            android.util.Log.d(TAG, "Запрос товара по ID (REST): $productId")
            val response = productsService.getProductById(idFilter = "eq.$productId")

            if (response.isSuccessful) {
                val body = response.body().orEmpty()
                if (body.isNotEmpty()) {
                    Result.success(body.first())
                } else {
                    Result.failure(Exception("Товар с ID '$productId' не найден"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Ошибка getProductById: ${response.code()}, $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Исключение getProductById: ${e.message}", e)
            Result.failure(e)
        }
    }

}