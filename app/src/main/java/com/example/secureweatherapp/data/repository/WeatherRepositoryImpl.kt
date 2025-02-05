package com.example.secureweatherapp.data.repository

import android.util.Log
import com.example.secureweatherapp.BuildConfig
import com.example.secureweatherapp.data.api.WeatherApi
import com.example.secureweatherapp.data.local.WeatherDao
import com.example.secureweatherapp.data.local.WeatherEntity
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.WeatherRepository
import com.example.secureweatherapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class WeatherRepositoryImpl(
    private val api: WeatherApi,
    private val dao: WeatherDao
) : WeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Flow<Resource<WeatherResponse>> = flow {
        try {
            emit(Resource.Loading())
            val weather = api.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.API_KEY
            )
            emit(Resource.Success(weather))

            // Save to Room database
            try {
                dao.insertWeather(WeatherEntity.fromWeatherResponse(weather))
                val deletedRows = dao.deleteOldEntries()
                Log.d("WeatherRepository", "Deleted $deletedRows old entries")
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error saving to database", e)
            }

        } catch (e: HttpException) {
            emit(Resource.Error("An error occurred: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    override fun getSavedWeather(): Flow<List<WeatherResponse>> {
        return dao.getRecentWeather().map { entities ->
            entities.map { it.toWeatherResponse() }
        }
    }
}