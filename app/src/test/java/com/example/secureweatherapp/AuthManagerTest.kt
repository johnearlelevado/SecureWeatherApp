package com.example.secureweatherapp

import android.app.Activity
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.secureweatherapp.data.auth.AuthManager
import com.example.secureweatherapp.data.auth.AuthState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthManagerTest {

    private lateinit var authManager: AuthManager
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var activity: ComponentActivity
    private lateinit var credentialManager: CredentialManager

    @Before
    fun setup() {
        sharedPrefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        credentialManager = mockk()

        every { sharedPrefs.edit() } returns editor
        every { editor.apply() } just Runs

        authManager = AuthManager(sharedPrefs)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Given
        every { sharedPrefs.getString("auth_token", null) } returns null
        
        // When
        val initialState = authManager.authState.value

        // Then
        assertEquals(AuthState.Loading, initialState)
    }

    @Test
    fun `checkAuthState sets LoggedOut when no token exists`() = runTest {
        // Given
        every { sharedPrefs.getString("auth_token", null) } returns null

        // When - init is called in constructor
        authManager = AuthManager(sharedPrefs)

        // Then
        assertEquals(AuthState.LoggedOut, authManager.authState.value)
    }

    @Test
    fun `checkAuthState sets LoggedIn when valid token exists`() = runTest {
        // Given
        val futureTime = System.currentTimeMillis() + 3600000 // 1 hour in future
        every { sharedPrefs.getString("auth_token", null) } returns "valid_token"
        every { sharedPrefs.getLong("token_expiry", 0) } returns futureTime

        // When
        authManager = AuthManager(sharedPrefs)

        // Then
        assertEquals(AuthState.LoggedIn, authManager.authState.value)
    }

    @Test
    fun `checkAuthState sets SessionExpired when token is expired`() = runTest {
        // Given
        val pastTime = System.currentTimeMillis() - 3600000 // 1 hour in past
        every { sharedPrefs.getString("auth_token", null) } returns "expired_token"
        every { sharedPrefs.getLong("token_expiry", 0) } returns pastTime

        // When
        authManager = AuthManager(sharedPrefs)

        // Then
        assertEquals(AuthState.SessionExpired, authManager.authState.value)
    }

    @Test
    fun `register success sets LoggedIn state`() = runTest {
        // Given
        val mockResponse = mockk<CreateCredentialResponse>()
        coEvery { 
            credentialManager.createCredential(any(), any()) 
        } returns mockResponse

        // When
        val result = authManager.register(activity, "test@test.com", "password")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(AuthState.LoggedIn, authManager.authState.value)
        verify { 
            editor.putString("auth_token", any()) 
            editor.putLong("token_expiry", any())
            editor.apply()
        }
    }

    @Test
    fun `login success sets LoggedIn state`() = runTest {
        // Given
        val mockResponse = mockk<GetCredentialResponse>()
        coEvery { 
            credentialManager.getCredential(any<Activity>(), any<GetCredentialRequest>())
        } returns mockResponse

        // When
        val result = authManager.login(activity, "test@test.com")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(AuthState.LoggedIn, authManager.authState.value)
        verify { 
            editor.putString("auth_token", any())
            editor.putLong("token_expiry", any())
            editor.apply()
        }
    }

    @Test
    fun `logout clears token and sets LoggedOut state`() = runTest {
        // When
        authManager.logout()

        // Then
        assertEquals(AuthState.LoggedOut, authManager.authState.value)
        verify { 
            editor.remove("auth_token")
            editor.remove("token_expiry")
            editor.apply()
        }
    }
}