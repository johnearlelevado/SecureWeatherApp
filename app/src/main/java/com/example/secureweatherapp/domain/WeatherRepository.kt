package com.example.secureweatherapp.domain

import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Flow<Resource<WeatherResponse>>
    fun getSavedWeather(): Flow<List<WeatherResponse>>
}