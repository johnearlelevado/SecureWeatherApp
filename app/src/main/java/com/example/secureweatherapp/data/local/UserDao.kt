package com.example.secureweatherapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.sql.SQLException

@Dao
interface UserDao {
    @Insert
    @Throws(SQLException::class)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun isEmailTaken(email: String): Boolean
}