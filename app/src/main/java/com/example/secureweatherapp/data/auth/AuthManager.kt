package com.example.secureweatherapp.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class AuthManager @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {
    private val TAG = "AuthManager"

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val token = encryptedPrefs.getString("auth_token", null)
        val expiryTime = encryptedPrefs.getLong("token_expiry", 0)

        _authState.value = when {
            token == null -> AuthState.LoggedOut
            System.currentTimeMillis() > expiryTime -> {
                clearAuthData()
                AuthState.SessionExpired
            }
            else -> AuthState.LoggedIn
        }
    }

    suspend fun register(activity: ComponentActivity, email: String, password: String): Result<Unit> {
        return try {
            val credentialManager = CredentialManager.create(activity)

            val request = CreatePasswordRequest(
                id = email,
                password = password.toByteArray().toString()
            )

            credentialManager.createCredential(
                request = request,
                context = activity
            )

            // For demo purposes, create a simple token
            val token = "demo_token_${System.currentTimeMillis()}"
            saveAuthData(token)

            _authState.value = AuthState.LoggedIn
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during registration", e)
            Result.failure(e)
        }
    }

    suspend fun login(activity: ComponentActivity, email: String): Result<Unit> {
        return try {
            val credentialManager = CredentialManager.create(activity)

            val getPasswordOption = GetPasswordOption()
            val request = GetCredentialRequest(listOf(getPasswordOption))

            credentialManager.getCredential(
                request = request,
                context = activity
            )

            // For demo purposes, create a simple token
            val token = "demo_token_${System.currentTimeMillis()}"
            saveAuthData(token)

            _authState.value = AuthState.LoggedIn
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during login", e)
            Result.failure(e)
        }
    }

    fun logout() {
        clearAuthData()
        _authState.value = AuthState.LoggedOut
    }

    private fun saveAuthData(token: String) {
        encryptedPrefs.edit()
            .putString("auth_token", token)
            .putLong("token_expiry", System.currentTimeMillis() + TOKEN_VALIDITY_DURATION)
            .apply()
    }

    private fun clearAuthData() {
        encryptedPrefs.edit()
            .remove("auth_token")
            .remove("token_expiry")
            .apply()
    }

    companion object {
        private const val TOKEN_VALIDITY_DURATION = 5000L //7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    object SessionExpired : AuthState()
}