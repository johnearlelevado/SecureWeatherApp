package com.example.secureweatherapp.ui.screens

import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secureweatherapp.data.auth.AuthState
import com.example.secureweatherapp.data.model.WeatherResponse
import com.example.secureweatherapp.domain.util.Resource
import com.example.secureweatherapp.ui.components.WeatherHistoryTab
import com.example.secureweatherapp.ui.components.WeatherIcon
import com.example.secureweatherapp.ui.util.Utils.formatTimeShort
import com.example.secureweatherapp.ui.viewmodel.AuthViewModel
import com.example.secureweatherapp.ui.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onSessionExpired: () -> Unit,
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentWeather by weatherViewModel.currentWeather.collectAsState()
    val savedWeather by weatherViewModel.savedWeather.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.SessionExpired, AuthState.LoggedOut -> {
                onSessionExpired()
            }
            else -> {}
        }
    }

    // Initial weather fetch when permissions are granted
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            Log.d("HomeScreen", "Initial weather fetch")
            weatherViewModel.fetchWeather()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App") },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex,
                modifier = Modifier.background(Color(0xFFE3F2FD))) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                ) {
                    Text(
                        text = "Current Weather",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                ) {
                    Text(
                        text = "History",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    if (!permissionsState.allPermissionsGranted) {
                        LocationPermissionRequest(
                            onRequestPermission = {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        )
                    } else {
                        CurrentWeatherTab(weatherState = currentWeather)
                    }
                }
                1 -> WeatherHistoryTab(weatherList = savedWeather)
            }
        }
    }
}

@Composable
private fun LocationPermissionRequest(
    onRequestPermission: () -> Unit
) {
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
            onClick = onRequestPermission
        ) {
            Text("Grant Permissions")
        }
    }
}

@Composable
private fun CurrentWeatherTab(
    weatherState: Resource<WeatherResponse>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (weatherState) {
            is Resource.Success -> {
                val weather = weatherState.data ?: return
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeatherIcon(
                            weather = weather,
                            modifier = Modifier.size(106.dp) // Increased size
                        )

                        Column {
                            Text(
                                text = "${weather.name}, ${weather.sys.country}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "${weather.main.temp}°C",
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = weather.weather.firstOrNull()?.description?.capitalize() ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Sunrise",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = formatTimeShort(weather.sys.sunrise),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column {
                                Text(
                                    text = "Sunset",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = formatTimeShort(weather.sys.sunset),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            is Resource.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Loading weather data...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = weatherState.message ?: "An error occurred",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

private fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}