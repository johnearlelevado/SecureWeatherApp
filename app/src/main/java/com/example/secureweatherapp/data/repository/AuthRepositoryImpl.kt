package com.example.secureweatherapp.data.repository

import android.util.Log
import com.example.secureweatherapp.data.local.User
import com.example.secureweatherapp.data.local.UserDao
import com.example.secureweatherapp.domain.AuthRepository
import com.example.secureweatherapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    override suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            
            val user = userDao.getUserByEmail(email)
            if (user == null) {
                emit(Resource.Error("Invalid email or password"))
                return@flow
            }

            val hashedPassword = hashPassword(password)
            if (user.passwordHash != hashedPassword) {
                emit(Resource.Error("Invalid email or password"))
                return@flow
            }

            emit(Resource.Success(user))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during login", e)
            emit(Resource.Error("An error occurred during login"))
        }
    }

    override suspend fun register(email: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            if (userDao.isEmailTaken(email)) {
                emit(Resource.Error("Email is already registered"))
                return@flow
            }

            val hashedPassword = hashPassword(password)
            val user = User(
                email = email,
                passwordHash = hashedPassword
            )

            userDao.insertUser(user)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during registration", e)
            emit(Resource.Error("An error occurred during registration"))
        }
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        return userDao.isEmailTaken(email)
    }
}