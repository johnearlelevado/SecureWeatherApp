package com.example.secureweatherapp.data.model

data class WeatherResponse(
    val name: String,
    val sys: Sys,
    val main: Main,
    val weather: List<Weather>,
    val dt: Long
) {
    data class Sys(
        val country: String,
        val sunrise: Long,
        val sunset: Long
    )
    
    data class Main(
        val temp: Double
    )
    
    data class Weather(
        val id: Int,
        val main: String,
        val description: String,
        val icon: String
    )
}