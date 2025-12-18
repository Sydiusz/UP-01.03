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
import com.example.shoeshop.data.SessionManager

class EmailVerificationViewModel : ViewModel() {

    private val _verificationState =
        MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState

    fun verifyEmailOtp(email: String, otpCode: String) {
        verifyOtpInternal(email, otpCode, OtpType.EMAIL)
    }

    fun verifyRecoveryOtp(email: String, otpCode: String) {
        verifyOtpInternal(email, otpCode, OtpType.RECOVERY)
    }

    private fun verifyOtpInternal(email: String, otpCode: String, otpType: OtpType) {
        viewModelScope.launch {
            try {
                _verificationState.value = VerificationState.Loading

                val response = RetrofitInstance.userManagementService.verifyOtp(
                    VerifyOtpRequest(
                        email = email,
                        token = otpCode,
                        type = "email"   // или как у тебя
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Log.d("OTP", "verifyRecoveryOtp success, email=$email")
                        Log.d("OTP", "verifyRecoveryOtp access_token=${body.access_token.take(40)}...")
                        Log.d("OTP", "verifyRecoveryOtp userId=${body.user.id}, userEmail=${body.user.email}")

                        _verificationState.value = VerificationState.Success(
                            type = otpType,
                            data = body
                        )
                    } ?: run {
                        Log.e("OTP", "verifyRecoveryOtp empty body")
                        _verificationState.value = VerificationState.Error("Empty response")
                    }
                } else {
                    Log.e(
                        "OTP",
                        "verifyRecoveryOtp error code=${response.code()} msg=${response.message()} body=${response.errorBody()?.string()}"
                    )
                    val errorMessage = parseVerificationError(response.code(), response.message(), otpType)
                    _verificationState.value = VerificationState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("OTP", "verifyRecoveryOtp exception=${e.message}", e)
                val errorMessage = when (e) {
                    is ConnectException -> "No internet connection"
                    is SocketTimeoutException -> "Connection timeout"
                    else -> "Verification failed: ${e.message}"
                }
                _verificationState.value = VerificationState.Error(errorMessage)
            }
        }
    }

    private fun parseVerificationError(
        code: Int,
        message: String,
        otpType: OtpType
    ): String {
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
    }
}

enum class OtpType { EMAIL, RECOVERY }

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    data class Success(val type: OtpType, val data: VerifyOtpResponse? = null) : VerificationState()
    data class Error(val message: String) : VerificationState()
}
