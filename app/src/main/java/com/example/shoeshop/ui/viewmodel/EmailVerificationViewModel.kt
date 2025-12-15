// EmailVerificationViewModel.kt
package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.myfirstproject.data.model.VerifyOtpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import com.example.shoeshop.data.RetrofitInstance
class EmailVerificationViewModel : ViewModel() {
    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState

    fun verifyOtp(email: String, otpCode: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.userManagementService.verifyOtp(
                    VerifyOtpRequest(
                        email = email,
                        token = otpCode,
                        type = "email"
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { verifyResponse ->

                        Log.v("verifyOtp", "Email verified successfully for: ${verifyResponse.user.email}")
                        _verificationState.value = VerificationState.Success
                    }
                } else {
                    val errorMessage = parseVerificationError(response.code(), response.message())
                    _verificationState.value = VerificationState.Error(errorMessage)
                    Log.e("verifyOtp", "Error code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is ConnectException -> "No internet connection"
                    is SocketTimeoutException -> "Connection timeout"
                    else -> "Verification failed: ${e.message}"
                }
                _verificationState.value = VerificationState.Error(errorMessage)
                Log.e("EmailVerificationViewModel", "Exception: ${e.message}", e)
            }
        }
    }

    private fun parseVerificationError(code: Int, message: String): String {
        return when (code) {
            400 -> "Invalid OTP code"
            401 -> "OTP expired or invalid"
            404 -> "Email not found"
            429 -> "Too many attempts. Please try again later."
            else -> "Verification failed: $message"
        }
    }

    fun resetState() {
        _verificationState.value = VerificationState.Idle
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}