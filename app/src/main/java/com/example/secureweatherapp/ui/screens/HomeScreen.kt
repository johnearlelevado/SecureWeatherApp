package com.example.secureweatherapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.util.Resource
import com.example.secureweatherapp.ui.components.WeatherHistoryTab
import com.example.secureweatherapp.ui.components.WeatherIcon
import com.example.secureweatherapp.ui.util.formatTime
import com.example.secureweatherapp.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentWeather by viewModel.currentWeather.collectAsState()
    val savedWeather by viewModel.savedWeather.collectAsState()

    Column {
        TopAppBar(
            title = { Text("Weather App") },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Logout"
                    )
                }
            }
        )

        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 }
            ) {
                Text("Current Weather")
            }
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 }
            ) {
                Text("History")
            }
        }

        when (selectedTabIndex) {
            0 -> CurrentWeatherTab(currentWeather)
            1 -> WeatherHistoryTab(savedWeather)
        }
    }
}

@Composable
fun CurrentWeatherTab(weatherState: Resource<WeatherResponse>) {
    when (weatherState) {
        is Resource.Success -> {
            val weather = weatherState.data ?: WeatherResponse(
                name = "",
                sys = WeatherResponse.Sys(country = "", sunrise = 0L, sunset = 0L),
                weather = emptyList(),
                dt = 0L,
                main = WeatherResponse.Main(temp = 0.0),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "${weather.name}, ${weather.sys.country}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${weather.main.temp}°C",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Sunrise: ${formatTime(weather.sys.sunrise)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Sunset: ${formatTime(weather.sys.sunset)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                WeatherIcon(weather)
            }
        }
        is Resource.Loading -> {
            //CircularProgressIndicator()
            Text("loading")
        }
        is Resource.Error -> {
            Text(weatherState.message ?: "An error occurred")
        }
    }
}