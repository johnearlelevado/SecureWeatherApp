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

    private val _currentWeather = MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())
    val currentWeather = _currentWeather.asStateFlow()

    private val _savedWeather = MutableStateFlow<List<WeatherResponse>>(emptyList())
    val savedWeather = _savedWeather.asStateFlow()

    init {
        fetchWeather()
        viewModelScope.launch {
            repository.getSavedWeather().collect {
                _savedWeather.value = it
            }
        }
    }

    private fun fetchWeather() {
        if (checkLocationPermission()) {
            try {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let {
                    viewModelScope.launch {
                        repository.getCurrentWeather(it.latitude, it.longitude)
                            .collect { resource ->
                                _currentWeather.value = resource
                            }
                    }
                }
            } catch (e: SecurityException) {
                _currentWeather.value = Resource.Error("Location permission required")
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }
}