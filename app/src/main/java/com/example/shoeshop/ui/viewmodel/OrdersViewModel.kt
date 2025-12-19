// ui/viewmodel/OrdersViewModel.kt
package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.model.OrderWithItems
import com.example.shoeshop.data.repository.OrdersHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class OrderListItemUi(
    val id: Long,
    val title: String,
    val price: Double,
    val delivery: Double,
    val createdAt: OffsetDateTime,
    val timeLabel: String
)

data class OrdersSection(
    val header: String,
    val items: List<OrderListItemUi>
)

data class OrdersUiState(
    val sections: List<OrdersSection> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OrdersViewModel(
    private val repo: OrdersHistoryRepository = OrdersHistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repo.getOrdersHistory()
            if (result.isSuccess) {
                val sections = mapToSections(result.getOrDefault(emptyList()))
                _uiState.value = OrdersUiState(sections = sections)
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

    private fun mapToSections(orders: List<OrderWithItems>): List<OrdersSection> {
        val now = OffsetDateTime.now()
        val localZone = ZoneId.systemDefault()
        val fmtDate = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val fmtTime = DateTimeFormatter.ofPattern("HH:mm")

        val items = orders.mapNotNull { order ->
            val createdRaw = order.created_at ?: return@mapNotNull null
            val created = OffsetDateTime.parse(createdRaw)
                .atZoneSameInstant(localZone)
                .toOffsetDateTime()

            val minutes = ChronoUnit.MINUTES.between(created, now)
            val isToday = created.toLocalDate() == now.toLocalDate()

            val timeLabel = if (isToday && minutes >= 0 && minutes < 24 * 60) {
                "${minutes} мин назад"
            } else {
                created.format(fmtTime)
            }

            val first = order.items.firstOrNull()
            OrderListItemUi(
                id = order.id,
                title = first?.title ?: "Заказ №${order.id}",
                price = first?.coast ?: 0.0,
                delivery = (order.delivery_coast ?: 0L).toDouble(),
                createdAt = created,
                timeLabel = timeLabel
            )
        }.sortedByDescending { it.createdAt }

        val today = items.filter { it.createdAt.toLocalDate() == now.toLocalDate() }
        val yesterday = items.filter { it.createdAt.toLocalDate() == now.minusDays(1).toLocalDate() }
        val others = items.filter {
            val d = it.createdAt.toLocalDate()
            d != now.toLocalDate() && d != now.minusDays(1).toLocalDate()
        }

        val sections = mutableListOf<OrdersSection>()
        if (today.isNotEmpty()) sections += OrdersSection("Недавний", today)
        if (yesterday.isNotEmpty()) sections += OrdersSection("Вчера", yesterday)

        others.groupBy { it.createdAt.toLocalDate() }
            .toSortedMap(compareByDescending { it })
            .forEach { (date, list) ->
                sections += OrdersSection(date.format(fmtDate), list)
            }

        return sections
    }
}
