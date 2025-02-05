import androidx.activity.ComponentActivity
import com.example.secureweatherapp.data.auth.AuthManager
import com.example.secureweatherapp.data.auth.AuthState
import com.example.secureweatherapp.ui.viewmodel.AuthUiState
import com.example.secureweatherapp.ui.viewmodel.AuthViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var viewModel: AuthViewModel
    private val authStateFlow = MutableStateFlow(AuthState.LoggedOut)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { authManager.authState } returns authStateFlow
        viewModel = AuthViewModel(authManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `login success updates UI state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery {
            authManager.login(email, password)
        } returns Result.success(Unit)

        // When
        viewModel.login(email, password)
        testScheduler.advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
        coVerify { authManager.login(email, password) }
    }

    @Test
    fun `login failure updates UI state with error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Invalid credentials"
        coEvery {
            authManager.login(email, password)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.login(email, password)
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(errorMessage, (viewModel.uiState.value as AuthUiState.Error).message)
    }

    @Test
    fun `register success updates UI state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery {
            authManager.register(email, password)
        } returns Result.success(Unit)

        // When
        viewModel.register(email, password)
        testScheduler.advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
        coVerify { authManager.register(email, password) }
    }

    @Test
    fun `register failure updates UI state with error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Email already exists"
        coEvery {
            authManager.register(email, password)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.register(email, password)
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(errorMessage, (viewModel.uiState.value as AuthUiState.Error).message)
    }
}