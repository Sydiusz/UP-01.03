package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstproject.data.model.SignInRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.shoeshop.data.RetrofitInstance

class SignInViewModel : ViewModel() {
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.userManagementService.signIn(
                    SignInRequest(email, password)
                )

                if (response.isSuccessful) {
                    response.body()?.let { signInResponse ->
                        // Сохраняем токен
                        saveAuthToken(signInResponse.access_token)
                        saveRefreshToken(signInResponse.refresh_token)
                        saveUserData(signInResponse.user)

                        Log.v("signIn", "User authenticated: ${signInResponse.user.email}")
                        _signInState.value = SignInState.Success
                    }
                } else {
                    val errorMessage = parseSignInError(response.code(), response.message())
                    _signInState.value = SignInState.Error(errorMessage)
                    Log.e("signIn", "Error code: ${response.code()}, message: ${response.message()}, body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.ConnectException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Connection timeout"
                    is javax.net.ssl.SSLHandshakeException -> "Security error"
                    else -> "Authentication failed: ${e.message}"
                }
                _signInState.value = SignInState.Error(errorMessage)
                Log.e("SignInViewModel", "Exception: ${e.message}", e)
            }
        }
    }

    private fun parseSignInError(code: Int, message: String): String {
        return when (code) {
            400 -> "Invalid email or password"
            401 -> "Invalid login credentials"
            422 -> "Invalid email format"
            429 -> "Too many login attempts. Please try again later."
            500 -> "Server error. Please try again later."
            else -> "Login failed: $message"
        }
    }

    private fun saveAuthToken(token: String) {
        // TODO: Сохранить токен в SecurePreferences
        Log.d("Auth", "Access token saved: ${token.take(10)}...")
    }

    private fun saveRefreshToken(token: String) {
        // TODO: Сохранить refresh токен
        Log.d("Auth", "Refresh token saved: ${token.take(10)}...")
    }

    private fun saveUserData(user: com.example.myfirstproject.data.model.User) {
        // TODO: Сохранить данные пользователя
        Log.d("Auth", "User data saved: ${user.email}")
    }

    fun resetState() {
        _signInState.value = SignInState.Idle
    }
}

sealed class SignInState {
    object Idle : SignInState()
    object Success : SignInState()
    data class Error(val message: String) : SignInState()
}