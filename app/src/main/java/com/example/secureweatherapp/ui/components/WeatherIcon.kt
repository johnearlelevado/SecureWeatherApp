package com.example.secureweatherapp.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secureweatherapp.data.model.WeatherResponse
import java.util.Calendar

@Composable
fun WeatherIcon(weather: WeatherResponse) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val weatherMain = weather.weather.firstOrNull()?.main?.lowercase() ?: ""

    val icon = when {
        currentHour >= 18 -> Icons.Default.CheckCircle // Moon icon after 6 PM
        weatherMain.contains("rain") -> Icons.Default.Build
        weatherMain.contains("cloud") -> Icons.Default.Call
        else -> Icons.Default.Create
    }

    Icon(
        imageVector = icon,
        contentDescription = weather.weather.firstOrNull()?.description,
        modifier = Modifier.size(48.dp)
    )
}