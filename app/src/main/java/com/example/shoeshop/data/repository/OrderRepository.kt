// data/repository/OrderRepository.kt
package com.example.shoeshop.data.repository

import android.util.Log
import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.CreateOrderItemRequest
import com.example.shoeshop.data.model.CreateOrderRequest
import com.example.shoeshop.ui.screens.CartUiItem

class OrderRepository {

    private val NEW_STATUS_ID = "970aed1e-549c-499b-a649-4bf3f9f93a01"

    suspend fun createOrderWithItems(
        email: String,
        phone: String,
        address: String,
        paymentId: String? = null,
        deliveryCost: Long,
        items: List<CartUiItem>
    ): Result<Unit> {
        val userId = SessionManager.userId
            ?: return Result.failure(Exception("userId is null"))

        Log.d(
            "OrderRepo",
            "createOrderWithItems: userId=$userId, items=${items.size}"
        )

        // 1. создаём сам заказ
        val orderReq = CreateOrderRequest(
            email = email,
            phone = phone,
            address = address,
            user_id = userId,
            payment_id = paymentId,
            delivery_coast = deliveryCost,
            status_id = NEW_STATUS_ID
        )

        return try {
            val orderResp =
                RetrofitInstance.orderService.createOrder(listOf(orderReq))
            Log.d(
                "OrderRepo",
                "createOrder response code=${orderResp.code()} isSuccessful=${orderResp.isSuccessful}"
            )

            if (!orderResp.isSuccessful) {
                val err = orderResp.errorBody()?.string()
                Log.e("OrderRepo", "createOrder errorBody=$err")
                return Result.failure(Exception(err ?: "HTTP ${orderResp.code()}"))
            }

            val createdOrder = orderResp.body()?.firstOrNull()
                ?: return Result.failure(Exception("Order body is empty"))
            val orderId = createdOrder.id
                ?: return Result.failure(Exception("Order id is null"))

            Log.d("OrderRepo", "created orderId=$orderId")

            // 2. создаём позиции заказа
            if (items.isEmpty()) {
                return Result.success(Unit)
            }

            val itemsReq = items.map { cartItem ->
                CreateOrderItemRequest(
                    title = cartItem.product.name,
                    coast = cartItem.product.price,
                    count = cartItem.count.toLong(),
                    order_id = orderId,
                    product_id = cartItem.product.id
                )
            }

            Log.d("OrderRepo", "createOrderItems body=$itemsReq")

            val itemsResp =
                RetrofitInstance.orderItemsService.createOrderItems(itemsReq)

            Log.d(
                "OrderRepo",
                "createOrderItems response code=${itemsResp.code()} isSuccessful=${itemsResp.isSuccessful}"
            )

            if (itemsResp.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = itemsResp.errorBody()?.string()
                Log.e("OrderRepo", "createOrderItems errorBody=$err")
                Result.failure(Exception(err ?: "HTTP ${itemsResp.code()}"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepo", "createOrderWithItems exception=${e.message}", e)
            Result.failure(e)
        }
    }
}
