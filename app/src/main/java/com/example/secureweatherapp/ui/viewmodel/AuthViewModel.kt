package com.example.secureweatherapp.ui.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureweatherapp.data.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    val authState = authManager.authState

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()


    fun login(activity: ComponentActivity, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                authManager.login(activity, email, password).onSuccess {
                    _uiState.value = AuthUiState.Success
                }.onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Authentication failed")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun register(activity: ComponentActivity, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                authManager.register(activity, email, password).onSuccess {
                    _uiState.value = AuthUiState.Success
                }.onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        authManager.logout()
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}