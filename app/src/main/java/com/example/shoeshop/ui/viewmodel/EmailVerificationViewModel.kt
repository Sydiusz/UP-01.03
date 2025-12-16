// EmailVerificationViewModel.kt
package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstproject.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import com.example.shoeshop.data.RetrofitInstance

class EmailVerificationViewModel : ViewModel() {

    // Общее состояние для обоих типов
    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState

    // Отдельное состояние для recovery (если нужны разные данные)
    private val _recoveryState = MutableStateFlow<RecoveryState?>(null)
    val recoveryState: StateFlow<RecoveryState?> = _recoveryState

    /**
     * Верификация OTP для подтверждения email
     */
    fun verifyEmailOtp(email: String, otpCode: String) {
        verifyOtpInternal(email, otpCode, OtpType.EMAIL)
    }

    /**
     * Верификация OTP для восстановления пароля
     */
    fun verifyRecoveryOtp(email: String, otpCode: String) {
        verifyOtpInternal(email, otpCode, OtpType.RECOVERY)
    }

    private fun verifyOtpInternal(email: String, otpCode: String, otpType: OtpType) {
        viewModelScope.launch {
            try {
                _verificationState.value = VerificationState.Loading

                when (otpType) {
                    OtpType.EMAIL -> {
                        val response = RetrofitInstance.userManagementService.verifyOtp(
                            VerifyOtpRequest(
                                email = email,
                                token = otpCode,
                                type = "email"
                            )
                        )

                        if (response.isSuccessful) {
                            response.body()?.let { verifyResponse ->
                                Log.v("verifyEmailOtp", "Email verified successfully for: ${verifyResponse.user.email}")
                                _verificationState.value = VerificationState.Success(
                                    type = OtpType.EMAIL,
                                    data = verifyResponse
                                )
                            } ?: run {
                                _verificationState.value = VerificationState.Error(
                                    "Empty response"
                                )
                            }
                        } else {
                            val errorMessage = parseVerificationError(response.code(), response.message(), otpType)
                            _verificationState.value = VerificationState.Error(errorMessage)
                            Log.e("verifyEmailOtp", "Error code: ${response.code()}, message: ${response.message()}")
                        }
                    }

                    OtpType.RECOVERY -> {
                        val response = RetrofitInstance.userManagementService.verifyRecoveryOtp(
                            VerifyOtpRequest(
                                email = email,
                                token = otpCode,
                                type = "recovery"
                            )
                        )

                        if (response.isSuccessful) {
                            response.body()?.let { recoveryResponse ->
                                Log.v("verifyRecoveryOtp", "Recovery OTP verified successfully")

                                // Устанавливаем общее состояние успеха
                                _verificationState.value = VerificationState.Success(
                                    type = OtpType.RECOVERY,
                                    data = recoveryResponse
                                )

                                // Дополнительно сохраняем recovery-specific данные
                                _recoveryState.value = RecoveryState(
                                    resetToken = recoveryResponse.reset_token,
                                    email = email
                                )
                            } ?: run {
                                _verificationState.value = VerificationState.Error(
                                    "Empty recovery response"
                                )
                            }
                        } else {
                            val errorMessage = parseVerificationError(response.code(), response.message(), otpType)
                            _verificationState.value = VerificationState.Error(errorMessage)
                            Log.e("verifyRecoveryOtp", "Error code: ${response.code()}, message: ${response.message()}")
                        }
                    }
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

    private fun parseVerificationError(code: Int, message: String, otpType: OtpType): String {
        return when (code) {
            400 -> when (otpType) {
                OtpType.EMAIL -> "Invalid OTP code"
                OtpType.RECOVERY -> "Invalid recovery code"
            }
            401 -> when (otpType) {
                OtpType.EMAIL -> "OTP expired or invalid"
                OtpType.RECOVERY -> "Recovery code expired or invalid"
            }
            404 -> when (otpType) {
                OtpType.EMAIL -> "Email not found"
                OtpType.RECOVERY -> "Email not found or no recovery request"
            }
            429 -> "Too many attempts. Please try again later."
            else -> "Verification failed: $message"
        }
    }

    fun resetState() {
        _verificationState.value = VerificationState.Idle
        _recoveryState.value = null
    }

    fun getResetToken(): String? {
        return _recoveryState.value?.resetToken
    }
}

// Типы OTP
enum class OtpType {
    EMAIL, // Подтверждение email
    RECOVERY // Восстановление пароля
}

// Общее состояние верификации
sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    data class Success(
        val type: OtpType,
        val data: Any? = null
    ) : VerificationState()
    data class Error(val message: String) : VerificationState()
}

// Специфичное состояние для recovery
data class RecoveryState(
    val resetToken: String?,
    val email: String
)