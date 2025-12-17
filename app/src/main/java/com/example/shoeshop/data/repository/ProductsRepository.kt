package com.example.shoeshop.data.repository

import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product

class ProductsRepository {

    private val productsService = RetrofitInstance.productsService
    private val categoriesService = RetrofitInstance.categoriesService

    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val response = productsService.getProducts()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBestSellers(): Result<List<Product>> {
        return try {
            val response = productsService.getBestSellers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load best sellers: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = categoriesService.getCategories()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(categoryId: String): Result<List<Product>> {
        return try {
            val response = productsService.getProductsByCategory(categoryId = categoryId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load category products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}