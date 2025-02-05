package com.example.secureweatherapp.security

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    private const val ITERATIONS = 600000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    @RequiresApi(Build.VERSION_CODES.O)
    fun hashPassword(password: String): Pair<String, String> {
        val salt = generateSalt()
        val hash = pbkdf2(password, salt)
        return Pair(hash, Base64.getEncoder().encodeToString(salt))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.getDecoder().decode(storedSalt)
        val computedHash = pbkdf2(password, salt)
        return computedHash == storedHash
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pbkdf2(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    fun isDeviceRooted(): Boolean {
        return checkRootBinaries() || checkTestKeys() || checkSuExists()
    }

    private fun checkRootBinaries(): Boolean {
        val paths = arrayOf("/system/xbin/su", "/system/bin/su", "/sbin/su", "/system/app/Superuser.apk")
        return paths.any { java.io.File(it).exists() }
    }

    private fun checkTestKeys(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuExists(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val bufferedReader = process.inputStream.bufferedReader()
            bufferedReader.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}