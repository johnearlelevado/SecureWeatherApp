package com.example.secureweatherapp.ui.screens

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.util.Resource
import com.example.secureweatherapp.ui.components.LocationPermissionHandler
import com.example.secureweatherapp.ui.components.WeatherHistoryTab
import com.example.secureweatherapp.ui.components.WeatherIcon
import com.example.secureweatherapp.ui.util.formatTime
import com.example.secureweatherapp.ui.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentWeather by viewModel.currentWeather.collectAsState()
    val savedWeather by viewModel.savedWeather.collectAsState()

    LaunchedEffect(selectedTabIndex, permissionsState.allPermissionsGranted) {
        if (selectedTabIndex == 0 && permissionsState.allPermissionsGranted) {
            Log.d("HomeScreen", "Permissions granted, refreshing weather")
            viewModel.refreshWeather()
        }
    }

    Column {
        TopAppBar(
            title = { Text("Weather App") },
            actions = {
                if (permissionsState.allPermissionsGranted && selectedTabIndex == 0) {
                    IconButton(onClick = { viewModel.refreshWeather() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather"
                        )
                    }
                }
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
            0 -> {
                if (!permissionsState.allPermissionsGranted) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Location permission is required to show weather information.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                Log.d("HomeScreen", "Requesting permissions")
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        ) {
                            Text("Grant Permissions")
                        }
                    }
                } else {
                    CurrentWeatherTab(
                        weatherState = currentWeather,
                        onRefresh = { viewModel.refreshWeather() }
                    )
                }
            }
            1 -> WeatherHistoryTab(savedWeather)
        }
    }
}

@Composable
fun CurrentWeatherTab(
    weatherState: Resource<WeatherResponse>,
    onRefresh: () -> Unit
) {
    when (weatherState) {
        is Resource.Success -> {
            val weather = weatherState.data ?: return
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                //CircularProgressIndicator()
                Text(
                    "Loading weather data...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        is Resource.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    weatherState.message ?: "An error occurred",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}