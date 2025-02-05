package com.example.secureweatherapp.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    fun formatTimeShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}