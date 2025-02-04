# Weather App

A weather application built with Modern Android Development practices.

## Setup

1. Register at OpenWeather API (https://openweathermap.org/api) to get your API key
2. Open `app/build.gradle.kts`
3. Replace `YOUR_API_KEY_HERE` with your actual API key:
   ```kotlin
   buildConfigField("String", "WEATHER_API_KEY", "\"YOUR_API_KEY_HERE\"")
   ```

## Features

- User authentication (Login/Registration)
- Current weather information
- Weather history
- Location-based weather updates
- Encrypted data storage