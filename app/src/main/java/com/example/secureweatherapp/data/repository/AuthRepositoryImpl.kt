package com.example.secureweatherapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.secureweatherapp.data.local.User
import com.example.secureweatherapp.data.local.UserDao
import com.example.secureweatherapp.domain.AuthRepository
import com.example.secureweatherapp.domain.util.Resource
import com.example.secureweatherapp.security.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            val user = userDao.getUserByEmail(email)
            if (user == null) {
                emit(Resource.Error("Invalid credentials"))
                return@flow
            }

            if (SecurityUtils.verifyPassword(password, user.passwordHash, user.passwordSalt)) {
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Invalid credentials"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Authentication failed"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun register(email: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            if (userDao.isEmailTaken(email)) {
                emit(Resource.Error("Email is already registered"))
                return@flow
            }

            if (!isPasswordStrong(password)) {
                emit(Resource.Error("Password must be at least 12 characters long and contain numbers, symbols"))
                return@flow
            }

            val (hash, salt) = SecurityUtils.hashPassword(password)
            val user = User(
                email = email,
                passwordHash = hash,
                passwordSalt = salt
            )

            userDao.insertUser(user)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Registration failed"))
        }
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        return userDao.isEmailTaken(email)
    }

    private fun isPasswordStrong(password: String): Boolean {
        return password.length >= 12 &&
                password.any { it.isDigit() } &&
                password.any { it.isLetter() } &&
                password.any { !it.isLetterOrDigit() }
    }
}