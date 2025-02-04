package com.example.secureweatherapp.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secureweatherapp.data.model.WeatherResponse
import java.util.Calendar

@Composable
fun WeatherIcon(weather: WeatherResponse) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val icon = when {
        currentHour >= 18 -> Icons.Default.Build
        weather.weather.firstOrNull()?.main?.lowercase() == "rain" -> Icons.Default.Build
        else -> Icons.Default.Build
    }
    
    Icon(
        imageVector = icon,
        contentDescription = weather.weather.firstOrNull()?.description,
        modifier = Modifier.size(48.dp)
    )
}