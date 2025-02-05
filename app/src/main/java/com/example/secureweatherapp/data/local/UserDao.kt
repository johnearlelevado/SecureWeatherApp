package com.example.secureweatherapp.data.local

import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET failed_attempts = failed_attempts + 1, last_attempt = :timestamp WHERE email = :email")
    suspend fun incrementFailedAttempts(email: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET failed_attempts = 0, last_attempt = :timestamp WHERE email = :email")
    suspend fun resetFailedAttempts(email: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun isEmailTaken(email: String): Boolean

    @Query("UPDATE users SET password_hash = :newHash, password_salt = :newSalt, last_password_change = :timestamp WHERE email = :email")
    suspend fun updatePassword(email: String, newHash: String, newSalt: String, timestamp: Long = System.currentTimeMillis())
}