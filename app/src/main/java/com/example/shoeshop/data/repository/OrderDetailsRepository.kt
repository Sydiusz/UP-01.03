// data/repository/OrderDetailsRepository.kt
package com.example.shoeshop.data.repository

import android.util.Log
import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.model.Order
import com.example.shoeshop.data.model.OrderItem

class OrderDetailsRepository {

    suspend fun getOrderWithItems(orderId: Long): Result<Pair<Order, List<OrderItem>>> {
        return try {
            // 1. грузим сам заказ по его id (bigint)
            val orderResp = RetrofitInstance.ordersReadService
                .getOrderById("eq.$orderId")
            if (!orderResp.isSuccessful) {
                val err = orderResp.errorBody()?.string()
                Log.e("OrderDetailsRepo", "getOrder error=$err")
                return Result.failure(Exception(err ?: "HTTP ${orderResp.code()}"))
            }

            val order = orderResp.body().orEmpty().firstOrNull()
                ?: return Result.failure(Exception("Order not found"))

            // 2. грузим все позиции из orders_items по order_id
            val itemsResp = RetrofitInstance.orderItemsReadService
                .getItemsForOrder("eq.$orderId")
            if (!itemsResp.isSuccessful) {
                val err = itemsResp.errorBody()?.string()
                Log.e("OrderDetailsRepo", "getItems error=$err")
                return Result.failure(Exception(err ?: "HTTP ${itemsResp.code()}"))
            }

            val items = itemsResp.body().orEmpty()

            Result.success(order to items)
        } catch (e: Exception) {
            Log.e("OrderDetailsRepo", "getOrderWithItems ex=${e.message}", e)
            Result.failure(e)
        }
    }
}
