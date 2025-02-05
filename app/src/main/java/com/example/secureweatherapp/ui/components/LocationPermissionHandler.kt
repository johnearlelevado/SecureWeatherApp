package com.example.secureweatherapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    permissionState: PermissionState,
    onPermissionResult: (Boolean) -> Unit
) {
    val TAG = "LocationPermissionHandler"

    // Monitor permission status changes
    LaunchedEffect(permissionState.status.isGranted) {
        Log.d(TAG, "Permission status changed. Granted: ${permissionState.status.isGranted}")
        onPermissionResult(permissionState.status.isGranted)
    }

    if (!permissionState.status.isGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                "Location access is required to show weather for your current location. " +
                        "Please grant the permission."
            } else {
                "Location permission is required for this app to work. " +
                        "Please grant the permission."
            }

            Text(
                text = textToShow,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    Log.d(TAG, "Request permission button clicked")
                    permissionState.launchPermissionRequest()
                }
            ) {
                Text("Grant Permission")
            }
        }
    }
}