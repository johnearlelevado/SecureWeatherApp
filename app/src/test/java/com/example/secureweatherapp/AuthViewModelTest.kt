package com.example.secureweatherapp

import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.secureweatherapp.data.auth.AuthManager
import com.example.secureweatherapp.data.auth.AuthState
import com.example.secureweatherapp.ui.viewmodel.AuthUiState
import com.example.secureweatherapp.ui.viewmodel.AuthViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel
    private lateinit var authManager: AuthManager
    private lateinit var activity: ComponentActivity

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authManager = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        viewModel = AuthViewModel(authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates UI state`() = runTest {
        // Given
        coEvery { 
            authManager.login(any(), any()) 
        } returns Result.success(Unit)

        // When
        viewModel.login(activity, "test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `login failure updates UI state with error`() = runTest {
        // Given
        val errorMessage = "Invalid credentials"
        coEvery { 
            authManager.login(any(), any()) 
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.login(activity, "test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(
            errorMessage,
            (viewModel.uiState.value as AuthUiState.Error).message
        )
    }

    @Test
    fun `register success updates UI state`() = runTest {
        // Given
        coEvery { 
            authManager.register(any(), any(), any()) 
        } returns Result.success(Unit)

        // When
        viewModel.register(activity, "test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `register failure updates UI state with error`() = runTest {
        // Given
        val errorMessage = "Email already exists"
        coEvery { 
            authManager.register(any(), any(), any()) 
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.register(activity, "test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(
            errorMessage,
            (viewModel.uiState.value as AuthUiState.Error).message
        )
    }

    @Test
    fun `logout updates auth state`() = runTest {
        // Given
        val authStateFlow = MutableStateFlow<AuthState>(AuthState.LoggedIn)
        coEvery { authManager.authState } returns authStateFlow

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { authManager.logout() }
    }
}