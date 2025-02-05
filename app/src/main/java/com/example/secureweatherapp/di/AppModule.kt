package com.example.secureweatherapp.di

import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.secureweatherapp.data.auth.AuthManager
import com.example.secureweatherapp.data.api.WeatherApi
import com.example.secureweatherapp.data.local.UserDao
import com.example.secureweatherapp.data.local.WeatherDao
import com.example.secureweatherapp.data.local.WeatherDatabase
import com.example.secureweatherapp.data.repository.WeatherRepositoryImpl
import com.example.secureweatherapp.domain.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "api.openweathermap.org"

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add(BASE_URL,
                // Primary certificate
                "sha256/CpmBztr3L/AZjANtR+K3vhridQoIsoyqTl5yU5zQQLQ=",
                // Backup certificate
                "sha256/47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=")
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(okHttpClient: OkHttpClient): WeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://$BASE_URL/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(database: WeatherDatabase) = database.weatherDao

    @Provides
    @Singleton
    fun provideUserDao(database: WeatherDatabase) = database.userDao

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "weather_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(weatherApi: WeatherApi, weatherDao: WeatherDao): WeatherRepository {
        return WeatherRepositoryImpl(weatherApi, weatherDao)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(LocationManager::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthManager(
        encryptedPrefs: SharedPreferences,
        userDao: UserDao,
        @ApplicationContext context: Context
    ): AuthManager {
        return AuthManager(encryptedPrefs, userDao, context)
    }
}