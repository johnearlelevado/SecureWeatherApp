package com.example.secureweatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.secureweatherapp.data.model.WeatherResponse

@Entity(tableName = "weather_history")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val country: String,
    val temperature: Double,
    val timestamp: Long,
    val description: String,
    val sunrise: Long,
    val sunset: Long,
    val icon: String  // Added icon field
) {
    fun toWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            name = cityName,
            sys = WeatherResponse.Sys(
                country = country,
                sunrise = sunrise,
                sunset = sunset
            ),
            main = WeatherResponse.Main(
                temp = temperature
            ),
            weather = listOf(
                WeatherResponse.Weather(
                    id = 0,
                    main = "",
                    description = description,
                    icon = icon  // Include icon in conversion
                )
            ),
            dt = timestamp
        )
    }

    companion object {
        fun fromWeatherResponse(weatherResponse: WeatherResponse): WeatherEntity {
            return WeatherEntity(
                cityName = weatherResponse.name,
                country = weatherResponse.sys.country,
                temperature = weatherResponse.main.temp,
                timestamp = weatherResponse.dt,
                description = weatherResponse.weather.firstOrNull()?.description ?: "",
                sunrise = weatherResponse.sys.sunrise,
                sunset = weatherResponse.sys.sunset,
                icon = weatherResponse.weather.firstOrNull()?.icon ?: "01d"  // Default to clear sky day if no icon
            )
        }
    }
}