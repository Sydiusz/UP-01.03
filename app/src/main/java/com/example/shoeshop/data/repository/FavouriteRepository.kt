package com.example.shoeshop.data.repository

import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.model.FavouriteItem
import com.example.shoeshop.data.model.Product

class FavouriteRepository {

    private val service = RetrofitInstance.favouriteService
    private val productsService = RetrofitInstance.productsService

    suspend fun addFavorite(userId: String, productId: String): Result<Unit> {
        return try {
            val response = service.addFavorite(
                FavouriteItem(productId = productId, userId = userId)
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(userId: String, productId: String): Result<Unit> {
        return try {
            val response = service.removeFavorite(
                userIdFilter = "eq.$userId",
                productIdFilter = "eq.$productId"
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorite(userId: String, productId: String): Result<Boolean> {
        return try {
            val response = service.getFavorites(
                userIdFilter = "eq.$userId",
                productIdFilter = "eq.$productId"
            )
            if (response.isSuccessful) {
                Result.success(!response.body().isNullOrEmpty())
            } else {
                Result.failure(
                    Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // все избранные productId пользователя
    suspend fun getFavoritesForUser(userId: String): Result<List<String>> {
        return try {
            val response = service.getFavorites(
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful) {
                val ids = response.body().orEmpty().map { it.productId }
                Result.success(ids)
            } else {
                Result.failure(
                    Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // сами товары избранного
    suspend fun getFavoriteProducts(userId: String): Result<List<Product>> {
        return try {
            val favIdsResult = getFavoritesForUser(userId)
            if (favIdsResult.isFailure) return Result.failure(favIdsResult.exceptionOrNull()!!)

            val ids = favIdsResult.getOrDefault(emptyList())
            if (ids.isEmpty()) return Result.success(emptyList())

            // запрос вида id=in.(id1,id2,...)
            val filter = "in.(${ids.joinToString(",")})"
            val response = productsService.getProductsByIds(filter)
            if (response.isSuccessful) {
                val products = response.body().orEmpty().map { it.copy(isFavorite = true) }
                Result.success(products)
            } else {
                Result.failure(
                    Exception("HTTP ${response.code()}: ${response.errorBody()?.string()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
