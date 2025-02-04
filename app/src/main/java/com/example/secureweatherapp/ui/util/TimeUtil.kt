package com.example.secureweatherapp.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp * 1000))
}