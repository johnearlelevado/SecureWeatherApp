package com.example.secureweatherapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.ui.util.formatTime

@Composable
fun WeatherHistoryTab(weatherList: List<WeatherResponse>) {
    LazyColumn {
        items(weatherList) { weather ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${weather.name}, ${weather.sys.country}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${weather.main.temp}°C",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatTime(weather.dt),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}