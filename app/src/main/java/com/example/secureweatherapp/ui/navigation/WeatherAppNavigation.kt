package com.example.secureweatherapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.secureweatherapp.data.auth.AuthState
import com.example.secureweatherapp.ui.components.SecurityErrorDialog
import com.example.secureweatherapp.ui.screens.HomeScreen
import com.example.secureweatherapp.ui.screens.LoginScreen
import com.example.secureweatherapp.ui.screens.RegistrationScreen
import com.example.secureweatherapp.ui.screens.SplashScreen
import com.example.secureweatherapp.ui.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherAppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    var showSecurityDialog by remember { mutableStateOf(false) }
    var securityMessage by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.SecurityError) {
            showSecurityDialog = true
            securityMessage = (authState as AuthState.SecurityError).message
        }
    }

    if (showSecurityDialog) {
        SecurityErrorDialog(
            message = securityMessage,
            onDismiss = { android.os.Process.killProcess(android.os.Process.myPid()) }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                authState = authState,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegistrationScreen(
                onNavigateToLogin = { navController.navigateUp() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onSessionExpired = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}