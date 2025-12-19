// ui/viewmodel/OrderDetailsViewModel.kt
package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.model.OrderItem
import com.example.shoeshop.data.repository.OrderDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class OrderDetailsUiState(
    val orderId: Long = 0L,
    val timeLabel: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val deliveryCost: Long = 0,
    val items: List<OrderItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OrderDetailsViewModel(
    private val repo: OrderDetailsRepository = OrderDetailsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun load(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repo.getOrderWithItems(orderId)
            if (result.isSuccess) {
                val (order, items) = result.getOrNull()!!
                val now = OffsetDateTime.now()
                val created = order.created_at?.let { OffsetDateTime.parse(it) }
                val localCreated = created?.atZoneSameInstant(ZoneId.systemDefault())
                    ?.toOffsetDateTime()

                val timeLabel = if (localCreated != null) {
                    val minutes = ChronoUnit.MINUTES.between(localCreated, now)
                    val isToday = localCreated.toLocalDate() == now.toLocalDate()
                    val fmtTime = DateTimeFormatter.ofPattern("HH:mm")
                    if (isToday && minutes >= 0 && minutes < 24 * 60) {
                        "${minutes} мин назад"
                    } else {
                        localCreated.format(fmtTime)
                    }
                } else ""

                _uiState.update {
                    it.copy(
                        orderId = order.id ?: 0L,
                        timeLabel = timeLabel,
                        email = order.email ?: "",
                        phone = order.phone ?: "",
                        address = order.address ?: "",
                        deliveryCost = order.delivery_coast ?: 0,
                        items = items,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}
