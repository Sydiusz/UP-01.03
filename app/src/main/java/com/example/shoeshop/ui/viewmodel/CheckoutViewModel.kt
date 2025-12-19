package com.example.shoeshop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.repository.OrderRepository
import com.example.shoeshop.ui.screens.CartUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val paymentId: String? = null,
    val deliveryCost: Long = 60,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isEditingPhone: Boolean = false,
    val isEditingEmail: Boolean = false
)

class CheckoutViewModel(
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun updatePaymentId(paymentId: String?) {
        _uiState.update { it.copy(paymentId = paymentId) }
    }

    fun createOrder(items: List<CartUiItem>) {
        val state = _uiState.value
        android.util.Log.d(
            "CheckoutVM",
            "createOrder() clicked, email='${state.email}', phone='${state.phone}', address='${state.address}', items=${items.size}"
        )

        if (state.email.isBlank() || state.phone.isBlank() || state.address.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Заполните все поля") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            val result = orderRepository.createOrderWithItems(
                email = state.email,
                phone = state.phone,
                address = state.address,
                paymentId = null,
                deliveryCost = state.deliveryCost,
                items = items
            )
            _uiState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSuccess = true)
                else it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }
    fun updateFromProfile(email: String, phone: String, address: String) {
        _uiState.update {
            it.copy(
                email = if (it.email.isBlank()) email else it.email,
                phone = if (it.phone.isBlank()) phone else it.phone,
                address = if (it.address.isBlank()) address else it.address
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
    fun toggleEditPhone() {
        _uiState.update { it.copy(isEditingPhone = !it.isEditingPhone) }
    }

    fun toggleEditEmail() {
        _uiState.update { it.copy(isEditingEmail = !it.isEditingEmail) }
    }
    fun updateAddressFromLocationOrProfile(
        isLocationEnabled: Boolean,
        profileAddress: String
    ) {
        if (isLocationEnabled) {
            // сюда подставь адрес, полученный из геолокации
            val locationAddress = profileAddress // временно
            _uiState.update { it.copy(address = locationAddress) }
        } else {
            _uiState.update {
                if (it.address.isBlank()) it.copy(address = profileAddress) else it
            }
        }
    }
}
