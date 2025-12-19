// data/repository/OrdersHistoryRepository.kt
package com.example.shoeshop.data.repository

import android.util.Log
import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.CreateOrderItemRequest
import com.example.shoeshop.data.model.CreateOrderRequest
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
    // 👇 НОВОЕ: повторить заказ (создать новый такой же)
    suspend fun repeatOrder(order: OrderWithItems): Result<Unit> {
        val userId = SessionManager.userId
            ?: return Result.failure(Exception("userId is null"))

        return try {
            // 1. Создаём новый заказ
            val createOrderReq = CreateOrderRequest(
                email = null,                         // подставь, если нужно
                phone = null,
                address = null,
                user_id = userId,
                payment_id = null,
                delivery_coast = order.delivery_coast,
                status_id = order.status_id ?: "new"  // String
            )

            val orderResp = RetrofitInstance.orderService.createOrder(
                listOf(createOrderReq)
            )
            if (!orderResp.isSuccessful) {
                val err = orderResp.errorBody()?.string()
                return Result.failure(Exception(err ?: "HTTP ${orderResp.code()}"))
            }

            val createdOrders = orderResp.body().orEmpty()
            val newOrderId = createdOrders.firstOrNull()?.id
                ?: return Result.failure(Exception("New order id is null"))

            // 2. Создаём позиции для нового заказа
            if (order.items.isNotEmpty()) {
                val itemsReq = order.items.map { item ->
                    CreateOrderItemRequest(
                        title = item.title,
                        coast = item.coast,
                        count = item.count,
                        order_id = newOrderId,
                        product_id = item.product_id
                    )
                }

                val itemsResp = RetrofitInstance.orderItemsService.createOrderItems(itemsReq)
                if (!itemsResp.isSuccessful) {
                    val err = itemsResp.errorBody()?.string()
                    return Result.failure(Exception(err ?: "HTTP ${itemsResp.code()}"))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteOrder(orderId: Long): Result<Unit> {
        return try {
            // 1. Удаляем все позиции заказа
            val itemsResp = RetrofitInstance.orderItemsWriteService
                .deleteItemsForOrder("eq.$orderId")
            if (!itemsResp.isSuccessful) {
                val err = itemsResp.errorBody()?.string()
                return Result.failure(Exception(err ?: "HTTP ${itemsResp.code()}"))
            }

            // 2. Удаляем сам заказ
            val orderResp = RetrofitInstance.orderWriteService
                .deleteOrder("eq.$orderId")
            if (!orderResp.isSuccessful) {
                val err = orderResp.errorBody()?.string()
                return Result.failure(Exception(err ?: "HTTP ${orderResp.code()}"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun <T> List<T>?.orElse(fallback: List<T>): List<T> =
        this ?: fallback
}
