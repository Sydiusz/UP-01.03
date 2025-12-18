// data/repository/CartRepository.kt
package com.example.shoeshop.data.repository

import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.CartItem

class CartRepository {

    suspend fun getCartItemsForCurrentUser(): Result<List<CartItem>> {
        val userId = SessionManager.userId ?: return Result.success(emptyList())
        return try {
            val resp = RetrofitInstance.cartService.getCartItems("eq.$userId")
            if (resp.isSuccessful) {
                Result.success(resp.body().orEmpty())
            } else {
                Result.failure(Exception(resp.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isInCart(productId: String): Result<Boolean> {
        val userId = SessionManager.userId ?: return Result.success(false)
        return try {
            val resp = RetrofitInstance.cartService.getCartItemForProduct(
                userIdFilter = "eq.$userId",
                productIdFilter = "eq.$productId"
            )
            if (resp.isSuccessful) {
                Result.success(resp.body().orEmpty().isNotEmpty())
            } else {
                Result.failure(Exception(resp.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToCart(productId: String): Result<Unit> {
        val userId = SessionManager.userId ?: return Result.failure(Exception("userId is null"))
        return try {
            val body = mapOf(
                "user_id" to userId,
                "product_id" to productId,
                "count" to "1"  // String вместо Long
            )
            val resp = RetrofitInstance.cartService.addToCart(body)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(resp.errorBody()?.string()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun removeFromCartByProduct(productId: String): Result<Unit> {
        val userId = SessionManager.userId ?: return Result.failure(Exception("userId is null"))
        return try {
            val respFind = RetrofitInstance.cartService.getCartItemForProduct(
                userIdFilter = "eq.$userId",
                productIdFilter = "eq.$productId"
            )
            if (!respFind.isSuccessful) {
                return Result.failure(Exception(respFind.errorBody()?.string()))
            }
            val item = respFind.body().orEmpty().firstOrNull()
                ?: return Result.success(Unit)

            val respDel = RetrofitInstance.cartService.removeFromCart("eq.${item.id}")
            if (respDel.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(respDel.errorBody()?.string()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCount(cartId: String, newCount: Int): Result<Unit> {
        return try {
            val body = mapOf("count" to newCount)
            val resp = RetrofitInstance.cartService.updateCart(
                idFilter = "eq.$cartId",
                body = body
            )
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(resp.errorBody()?.string()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCartById(cartId: String): Result<Unit> {
        return try {
            val resp = RetrofitInstance.cartService.removeFromCart("eq.$cartId")
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(resp.errorBody()?.string()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
