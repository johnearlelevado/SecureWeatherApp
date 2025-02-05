package com.example.secureweatherapp.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import com.example.secureweatherapp.data.local.User
import com.example.secureweatherapp.data.local.UserDao
import com.example.secureweatherapp.security.DeviceSecurity
import com.example.secureweatherapp.security.SecurityUtils
import com.example.secureweatherapp.ui.util.DeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class AuthManager @Inject constructor(
    private val encryptedPrefs: SharedPreferences,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TOKEN_VALIDITY_DURATION = 2 * 60 * 60 * 1000L // 2 hours
        private const val MAX_LOGIN_ATTEMPTS = 5
        private const val LOCKOUT_DURATION = 15 * 60 * 1000L // 15 minutes
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (SecurityUtils.isDeviceRooted()) { //|| DeviceSecurity.isDeveloperOptionsEnabled(context)
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
    suspend fun register(activity: ComponentActivity, email: String, password: String): Result<Unit> {
        return try {
            if (DeviceUtils.isEmulator()) {
                if (userDao.isEmailTaken(email)) {
                    return Result.failure(Exception("Email is already registered"))
                }

                val (hash, salt) = SecurityUtils.hashPassword(password)
                val user = User(
                    email = email,
                    passwordHash = hash,
                    passwordSalt = salt
                )
                userDao.insertUser(user)
            } else {
                val credentialManager = CredentialManager.create(activity)
                val request = CreatePasswordRequest(
                    id = email,
                    password = password.toByteArray().toString()
                )
                credentialManager.createCredential(
                    request = request,
                    context = activity
                )
            }

            val token = generateSecureToken()
            saveAuthData(token)
            _authState.value = AuthState.LoggedIn
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun login(activity: ComponentActivity, email: String, password: String): Result<Unit> {
        return try {
            if (DeviceUtils.isEmulator()) {
                val user = userDao.getUserByEmail(email) ?:
                return Result.failure(Exception("Invalid credentials"))

                if (isUserLockedOut(user.lastAttempt, user.failedAttempts)) {
                    return Result.failure(Exception("Account is temporarily locked"))
                }

                if (!SecurityUtils.verifyPassword(password, user.passwordHash, user.passwordSalt)) {
                    handleFailedLogin(email)
                    return Result.failure(Exception("Invalid credentials"))
                }

                userDao.resetFailedAttempts(email)
            } else {
                val credentialManager = CredentialManager.create(activity)
                val getPasswordOption = GetPasswordOption()
                val request = GetCredentialRequest(listOf(getPasswordOption))
                credentialManager.getCredential(
                    request = request,
                    context = activity
                )
            }

            val token = generateSecureToken()
            saveAuthData(token)
            _authState.value = AuthState.LoggedIn
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isUserLockedOut(lastAttempt: Long, failedAttempts: Int): Boolean {
        if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttempt
            return timeSinceLastAttempt < LOCKOUT_DURATION
        }
        return false
    }

    private suspend fun handleFailedLogin(email: String) {
        userDao.incrementFailedAttempts(email)
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