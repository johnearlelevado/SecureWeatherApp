package com.example.secureweatherapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.secureweatherapp.ui.screens.HomeScreen
import com.example.secureweatherapp.ui.screens.LoginScreen
import com.example.secureweatherapp.ui.screens.RegistrationScreen
import com.example.secureweatherapp.ui.viewmodel.AuthState
import com.example.secureweatherapp.ui.viewmodel.AuthViewModel

@Composable
public fun WeatherAppNavigation() {
    val navController = rememberNavController()
    val authState by viewModel<AuthViewModel>().authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = { navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }}
            )
        }
        
        composable("register") {
            RegistrationScreen(
                onNavigateToLogin = { navController.navigateUp() },
                onNavigateToHome = { navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }}
            )
        }
        
        composable("home") {
            HomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            else -> {}
        }
    }
}