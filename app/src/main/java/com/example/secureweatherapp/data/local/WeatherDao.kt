package com.example.secureweatherapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentWeather(): Flow<List<WeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    @Query("DELETE FROM weather_history WHERE timestamp NOT IN (SELECT timestamp FROM weather_history ORDER BY timestamp DESC LIMIT 5)")
    suspend fun deleteOldEntries(): Int
}