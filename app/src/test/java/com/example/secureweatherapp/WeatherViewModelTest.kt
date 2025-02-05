package com.example.secureweatherapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.WeatherRepository
import com.example.secureweatherapp.domain.util.Resource
import com.example.secureweatherapp.ui.viewmodel.AuthUiState
import com.example.secureweatherapp.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WeatherViewModel
    private lateinit var repository: WeatherRepository
    private lateinit var locationManager: LocationManager
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        locationManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock location permission check
        every {
            context.checkSelfPermission(any())
        } returns PackageManager.PERMISSION_GRANTED

        // Mock location
        val mockLocation = mockk<Location> {
            every { latitude } returns 40.0
            every { longitude } returns -74.0
        }
        every {
            locationManager.getLastKnownLocation(any())
        } returns mockLocation

        // Setup default mock for getSavedWeather
        coEvery {
            repository.getSavedWeather()
        } returns flowOf(emptyList())

        viewModel = WeatherViewModel(repository, locationManager, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchWeather success emits loading then success`() = runTest {
        // Given
        val mockWeather = createMockWeatherResponse()
        coEvery {
            repository.getCurrentWeather(any(), any())
        } returns flowOf(
            Resource.Loading(),
            Resource.Success(mockWeather)
        )

        // When
        viewModel.fetchWeather()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val currentValue = viewModel.currentWeather.value
        assertTrue(currentValue is Resource.Success)
        currentValue as Resource.Success  // Smart cast
        assertEquals(mockWeather.name, currentValue.data?.name)
        assertEquals(mockWeather.main.temp, currentValue.data?.main?.temp)
        assertEquals(mockWeather.weather, currentValue.data?.weather)
        assertEquals(mockWeather.sys, currentValue.data?.sys)
    }

    @Test
    fun `saved weather flow emits correct data`() = runTest {
        // Given
        val mockWeather = listOf(createMockWeatherResponse())
        coEvery {
            repository.getSavedWeather()
        } returns flowOf(mockWeather)

        // When creating a new viewModel, it will start collecting the flow
        viewModel = WeatherViewModel(repository, locationManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(mockWeather, viewModel.savedWeather.value)
    }

    @Test
    fun `saved weather flow starts empty`() = runTest {
        // When - ViewModel is created with default empty flow mock
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.savedWeather.value.isEmpty())
    }

    private fun createMockWeatherResponse() = WeatherResponse(
        name = "New York",
        sys = WeatherResponse.Sys(
            country = "US",
            sunrise = 1234567890L,
            sunset = 1234599999L
        ),
        main = WeatherResponse.Main(
            temp = 20.0
        ),
        weather = listOf(
            WeatherResponse.Weather(
                id = 800,
                main = "Clear",
                description = "clear sky",
                icon = "01d"
            )
        ),
        dt = 1234567890L
    )
}