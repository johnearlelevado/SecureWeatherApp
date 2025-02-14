package com.example.secureweatherapp.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
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
        // Initialize saved weather observation
        viewModelScope.launch {
            repository.getSavedWeather().collect {
                _savedWeather.value = it
            }
        }
    }

    fun fetchWeather() {
        if (!checkLocationPermission()) {
            _currentWeather.value = Resource.Error("Location permission required")
            return
        }

        viewModelScope.launch {
            try {
                _currentWeather.value = Resource.Loading()

                // Try GPS provider first
                var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (location == null) {
                    // Try network provider as fallback
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (location == null) {
                    _currentWeather.value = Resource.Error("Unable to get location. Please make sure location services are enabled.")
                    return@launch
                }


                repository.getCurrentWeather(location.latitude, location.longitude)
                    .collect { resource ->
                        _currentWeather.value = resource
                    }

            } catch (e: SecurityException) {
                _currentWeather.value = Resource.Error("Location permission required")
            } catch (e: Exception) {
                _currentWeather.value = Resource.Error("Error fetching weather: ${e.message}")
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        return hasPermission
    }
}