package com.example.secureweatherapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secureweatherapp.auth.AuthState

@Composable
fun SplashScreen(
    authState: AuthState,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.LoggedIn -> onNavigateToHome()
            AuthState.LoggedOut, AuthState.SessionExpired -> onNavigateToLogin()
            AuthState.Loading -> { }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Weather App",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (authState) {
                    AuthState.Loading -> "Loading..."
                    AuthState.SessionExpired -> "Session expired"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}