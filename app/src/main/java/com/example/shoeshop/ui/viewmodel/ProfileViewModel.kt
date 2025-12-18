// ui/viewmodel/ProfileViewModel.kt
package com.example.shoeshop.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoeshop.data.RetrofitInstance
import com.example.shoeshop.data.SessionManager
import com.example.shoeshop.data.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadProfile() {
        val userId = SessionManager.userId
        if (userId == null) {
            Log.e("ProfileVM", "loadProfile: userId is null")
            return
        }

        Log.d("ProfileVM", "loadProfile: userId=$userId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // ✅ ПРАВИЛЬНЫЙ синтаксис Supabase: только eq.<id>
                val filter = "eq.$userId"
                val resp = RetrofitInstance.profileService.getProfile(filter)
                Log.d("ProfileVM", "loadProfile filter=$filter: code=${resp.code()} body=${resp.body()}")

                if (resp.isSuccessful) {
                    val list = resp.body().orEmpty()
                    Log.d("ProfileVM", "loadProfile: listSize=${list.size}")
                    _uiState.value = _uiState.value.copy(
                        profile = list.firstOrNull(),
                        isLoading = false
                    )
                } else {
                    Log.w("ProfileVM", "No profile found, code=${resp.code()}")
                    _uiState.value = _uiState.value.copy(profile = null, isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "loadProfile exception=${e.message}", e)
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun saveProfile(firstname: String, lastname: String, address: String, phone: String) {
        val userId = SessionManager.userId
        if (userId == null) {
            Log.e("ProfileVM", "saveProfile: userId is null")
            return
        }

        Log.d("ProfileVM", "saveProfile: userId=$userId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val body = mapOf(
                    "firstname" to (if (firstname.isBlank()) null else firstname),
                    "lastname" to (if (lastname.isBlank()) null else lastname),
                    "address" to (if (address.isBlank()) null else address),
                    "phone" to (if (phone.isBlank()) null else phone)
                )

                // ✅ ПРАВИЛЬНЫЙ синтаксис: только eq.<id>
                val filter = "eq.$userId"

                // 1. UPDATE
                Log.d("ProfileVM", "Trying UPDATE: filter=$filter, body=$body")
                val updateResp = RetrofitInstance.profileService.updateProfile(filter, body)
                Log.d("ProfileVM", "UPDATE: code=${updateResp.code()}, error=${updateResp.errorBody()?.string()}")

                if (updateResp.isSuccessful) {
                    Log.d("ProfileVM", "Profile UPDATED successfully")
                    _uiState.value = _uiState.value.copy(
                        profile = Profile(
                            id = "",
                            user_id = userId,
                            firstname = firstname.ifBlank { null },
                            lastname = lastname.ifBlank { null },
                            address = address.ifBlank { null },
                            phone = phone.ifBlank { null }
                        ),
                        isLoading = false
                    )
                    return@launch
                }

                // 2. CREATE
                Log.w("ProfileVM", "UPDATE failed, trying CREATE")
                val createResp = RetrofitInstance.profileService.createProfile(
                    body + mapOf("user_id" to userId)
                )
                Log.d("ProfileVM", "CREATE: code=${createResp.code()}, error=${createResp.errorBody()?.string()}")

                if (createResp.isSuccessful) {
                    Log.d("ProfileVM", "Profile CREATED successfully")
                    _uiState.value = _uiState.value.copy(
                        profile = Profile(
                            id = "",
                            user_id = userId,
                            firstname = firstname.ifBlank { null },
                            lastname = lastname.ifBlank { null },
                            address = address.ifBlank { null },
                            phone = phone.ifBlank { null }
                        ),
                        isLoading = false
                    )
                } else {
                    Log.e("ProfileVM", "Both failed: UPDATE=${updateResp.code()}, CREATE=${createResp.code()}")
                    _uiState.value = _uiState.value.copy(
                        error = "Не удалось сохранить профиль",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "saveProfile exception=${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}", isLoading = false)
            }
        }
    }
}
