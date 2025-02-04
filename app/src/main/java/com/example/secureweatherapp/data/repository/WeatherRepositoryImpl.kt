package com.example.secureweatherapp.data.repository

import android.content.SharedPreferences
import com.example.secureweatherapp.data.api.WeatherApi
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.WeatherRepository
import com.example.secureweatherapp.domain.util.Resource
import com.example.weatherapp.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val encryptedPrefs: SharedPreferences
) : WeatherRepository {
    override suspend fun getCurrentWeather(lat: Double, lon: Double): Flow<Resource<WeatherResponse>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
            emit(Resource.Success(response))
            saveWeather(response)
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    private fun saveWeather(weather: WeatherResponse) {
        encryptedPrefs.edit().putString(
            "weather_${System.currentTimeMillis()}",
            Gson().toJson(weather)
        ).apply()
    }

    override fun getSavedWeather(): Flow<List<WeatherResponse>> = flow {
        val weatherList = encryptedPrefs.all.values
            .mapNotNull { it as? String }
            .mapNotNull { 
                try {
                    Gson().fromJson(it, WeatherResponse::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        emit(weatherList)
    }
}