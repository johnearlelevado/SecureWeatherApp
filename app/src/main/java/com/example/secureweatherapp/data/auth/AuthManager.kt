package com.example.secureweatherapp.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.secureweatherapp.data.local.User
import com.example.secureweatherapp.data.local.UserDao
import com.example.secureweatherapp.security.SecurityUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class AuthManager @Inject constructor(
    private val encryptedPrefs: SharedPreferences,
    private val userDao: UserDao
) {
    companion object {
        private const val TOKEN_VALIDITY_DURATION = 2 * 60 * 60 * 1000L // 2 hours
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (SecurityUtils.isDeviceRooted()) {
            _authState.value = AuthState.Error("Device security check failed")
        } else {
            checkAuthState()
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            if (userDao.isEmailTaken(email)) {
                return Result.failure(Exception("Email is already registered"))
            }

            if (!isPasswordStrong(password)) {
                return Result.failure(Exception("Password must be at least 12 characters long and contain numbers, letters, and symbols"))
            }

            val (hash, salt) = SecurityUtils.hashPassword(password)
            val user = User(
                email = email,
                passwordHash = hash,
                passwordSalt = salt
            )
            userDao.insertUser(user)

            val token = generateSecureToken()
            saveAuthData(token)
            _authState.value = AuthState.LoggedIn
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val user = userDao.getUserByEmail(email)
                ?: return Result.failure(Exception("Invalid credentials"))

            if (!SecurityUtils.verifyPassword(password, user.passwordHash, user.passwordSalt)) {
                return Result.failure(Exception("Invalid credentials"))
            }

            val token = generateSecureToken()
            saveAuthData(token)
            _authState.value = AuthState.LoggedIn
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        return password.length >= 12 &&
                password.any { it.isDigit() } &&
                password.any { it.isLetter() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun saveAuthData(token: String) {
        encryptedPrefs.edit()
            .putString("auth_token", token)
            .putLong("token_expiry", System.currentTimeMillis() + TOKEN_VALIDITY_DURATION)
            .apply()
    }

    private fun generateSecureToken(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun logout() {
        clearAuthData()
        _authState.value = AuthState.LoggedOut
    }

    private fun clearAuthData() {
        encryptedPrefs.edit()
            .remove("auth_token")
            .remove("token_expiry")
            .apply()
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    object SessionExpired : AuthState()
    data class Error(val message: String) : AuthState()
}