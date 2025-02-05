package com.example.secureweatherapp.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.secureweatherapp.R
import com.example.secureweatherapp.data.model.WeatherResponse
import java.util.Calendar

@Composable
fun WeatherIcon(
    weather: WeatherResponse,
    modifier: Modifier = Modifier.size(48.dp)  // Default size as before
    ) {
    val iconCode = if (weather.weather.firstOrNull()?.icon.isNullOrEmpty()) "01d" else weather.weather.firstOrNull()?.icon
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    // If it's 6 PM or later and the icon is for day time (ends with 'd'),
    // convert it to night version (ends with 'n')
    val finalIconCode = if (currentHour >= 18 && iconCode?.endsWith("d") == true) {
        iconCode.dropLast(1) + "n"
    } else {
        iconCode
    }

    val imageUrl = "https://openweathermap.org/img/wn/$finalIconCode@4x.png"

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .error(R.drawable.ic_launcher_background)
            .crossfade(true)
            .build(),
        contentDescription = weather.weather.firstOrNull()?.description,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}