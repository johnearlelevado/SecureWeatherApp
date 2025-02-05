package com.example.secureweatherapp.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.room.TypeConverter
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

class CryptoConverter {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "WeatherDataKey"

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    @TypeConverter
    fun encryptString(value: String): String {
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val encryptedBytes = cipher.doFinal(value.toByteArray())
        val combined = cipher.iv + encryptedBytes
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    @TypeConverter
    fun decryptString(encrypted: String): String {
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        val decoded = Base64.decode(encrypted, Base64.DEFAULT)
        
        val iv = decoded.sliceArray(0..11)
        val encryptedBytes = decoded.sliceArray(12..decoded.lastIndex)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return String(cipher.doFinal(encryptedBytes))
    }
}