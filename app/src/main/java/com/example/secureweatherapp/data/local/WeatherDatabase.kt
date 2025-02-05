package com.example.secureweatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WeatherEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}