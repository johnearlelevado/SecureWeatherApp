package com.example.secureweatherapp.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.WeatherRepository
import com.example.secureweatherapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "WeatherViewModel"

    private val _currentWeather = MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())
    val currentWeather = _currentWeather.asStateFlow()

    private val _savedWeather = MutableStateFlow<List<WeatherResponse>>(emptyList())
    val savedWeather = _savedWeather.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized")
        if (checkLocationPermission()) {
            Log.d(TAG, "Location permission already granted, fetching weather")
            fetchWeather()
        } else {
            Log.d(TAG, "Location permission not granted yet")
        }
    }

    fun refreshWeather() {
        Log.d(TAG, "refreshWeather called")
        if (!checkLocationPermission()) {
            Log.e(TAG, "Location permission not granted during refresh")
            _currentWeather.value = Resource.Error("Location permission required")
            return
        }
        fetchWeather()
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to fetch weather")
                _currentWeather.value = Resource.Loading()

                // Try GPS provider first
                var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (location == null) {
                    Log.d(TAG, "GPS location null, trying network provider")
                    // Try network provider as fallback
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (location == null) {
                    Log.e(TAG, "Both GPS and Network location are null")
                    _currentWeather.value = Resource.Error("Unable to get location. Please make sure location services are enabled.")
                    return@launch
                }

                Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")

                repository.getCurrentWeather(location.latitude, location.longitude)
                    .collect { resource ->
                        Log.d(TAG, "Weather resource received: $resource")
                        _currentWeather.value = resource
                    }

            } catch (e: SecurityException) {
                Log.e(TAG, "Security Exception during fetch", e)
                _currentWeather.value = Resource.Error("Location permission required")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                _currentWeather.value = Resource.Error("Error fetching weather: ${e.message}")
            }
        }

        viewModelScope.launch {
            repository.getSavedWeather().collect {
                _savedWeather.value = it
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Location permission check result: $hasPermission")
        return hasPermission
    }
}