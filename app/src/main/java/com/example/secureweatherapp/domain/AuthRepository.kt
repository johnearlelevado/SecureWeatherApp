package com.example.secureweatherapp.domain

import com.example.secureweatherapp.data.local.User
import com.example.secureweatherapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun register(email: String, password: String): Flow<Resource<User>>
    suspend fun isEmailTaken(email: String): Boolean
}