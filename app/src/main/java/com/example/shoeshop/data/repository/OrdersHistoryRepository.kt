// data/repository/OrdersHistoryRepository.kt
package com.example.shoeshop.data.repository

import android.util.Log
import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.OrderWithItems

class OrdersHistoryRepository {

    suspend fun getOrdersHistory(): Result<List<OrderWithItems>> {
        val userId = SessionManager.userId
            ?: return Result.failure(Exception("userId is null"))

        return try {
            val resp = RetrofitInstance.ordersReadService.getOrdersForUser("eq.$userId")
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("OrdersHistoryRepo", "getOrders error=$err")
                return Result.failure(Exception(err ?: "HTTP ${resp.code()}"))
            }

            val orders = resp.body().orElse(emptyList())

            val full = orders.map { order ->
                val itemsResp = RetrofitInstance.orderItemsReadService
                    .getItemsForOrder("eq.${order.id}")
                val items = if (itemsResp.isSuccessful) {
                    itemsResp.body().orElse(emptyList())
                } else emptyList()

                OrderWithItems(
                    id = order.id ?: 0L,
                    created_at = order.created_at,
                    status_id = order.status_id,
                    delivery_coast = order.delivery_coast,
                    items = items
                )
            }

            Result.success(full)
        } catch (e: Exception) {
            Log.e("OrdersHistoryRepo", "getOrdersHistory ex=${e.message}", e)
            Result.failure(e)
        }
    }

    private fun <T> List<T>?.orElse(fallback: List<T>): List<T> =
        this ?: fallback
}
