package com.example.secureweatherapp.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Email and password cannot be empty")
                return@launch
            }
            
            // Simulate authentication
            delay(1000)
            encryptedPrefs.edit()
                .putString("user_email", email)
                .putString("user_password", password.hashCode().toString())
                .apply()
            
            _authState.value = AuthState.Success
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Email and password cannot be empty")
                return@launch
            }
            
            // Simulate registration
            delay(1000)
            encryptedPrefs.edit()
                .putString("user_email", email)
                .putString("user_password", password.hashCode().toString())
                .apply()
            
            _authState.value = AuthState.Success
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}