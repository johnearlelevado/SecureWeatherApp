package com.example.secureweatherapp.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secureweatherapp.auth.AuthState
import com.example.secureweatherapp.ui.viewmodel.AuthUiState
import com.example.secureweatherapp.ui.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as ComponentActivity

    val authState by viewModel.authState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onNavigateToHome()
            is AuthUiState.Error -> {
                showError = true
                errorMessage = (uiState as AuthUiState.Error).message
            }
            else -> {
                showError = false
                errorMessage = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                showError = false
            },
            label = { Text("Email") },
            isError = showError && email.isBlank(),
            supportingText = {
                if (showError && email.isBlank()) {
                    Text("Email cannot be empty")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                showError = false
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = showError && password.isBlank(),
            supportingText = {
                if (showError && password.isBlank()) {
                    Text("Password cannot be empty")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                showError = false
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = showError && (confirmPassword.isBlank() || confirmPassword != password),
            supportingText = {
                if (showError) {
                    when {
                        confirmPassword.isBlank() -> Text("Please confirm your password")
                        confirmPassword != password -> Text("Passwords do not match")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        showError = true
                        errorMessage = "Please fill in all fields"
                    }
                    password != confirmPassword -> {
                        showError = true
                        errorMessage = "Passwords do not match"
                    }
                    password.length < 6 -> {
                        showError = true
                        errorMessage = "Password must be at least 6 characters long"
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        showError = true
                        errorMessage = "Please enter a valid email address"
                    }
                    else -> {
                        viewModel.register(activity, email, password)
                    }
                }
            },
            enabled = uiState !is AuthUiState.Loading
        ) {
            if (uiState is AuthUiState.Loading) {
                Text("Loading...")
            } else {
                Text("Register")
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}