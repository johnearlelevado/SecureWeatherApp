import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

android {
    namespace = "com.example.secureweatherapp"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    // Read local.properties file
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    val debugApiKey = localProperties.getProperty("DEBUG_API_KEY")
    val releaseApiKey = localProperties.getProperty("RELEASE_API_KEY")
    val keystorePassword = localProperties.getProperty("KEYSTORE_PASSWORD")
    val keyalias = localProperties.getProperty("KEY_ALIAS")
    val keypassword = localProperties.getProperty("KEY_PASSWORD")


    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = "$keystorePassword"
            keyAlias = "$keyalias"
            keyPassword = "$keypassword"
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("String", "API_URL", "\"https://api.openweathermap.org/data/2.5/\"")
            buildConfigField("String", "API_KEY", "\"$debugApiKey\"")
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_URL", "\"https://api.openweathermap.org/data/2.5/\"")
            buildConfigField("String", "API_KEY", "\"$releaseApiKey\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    hilt {
        enableAggregatingTask = false
    }

    // Add kapt configuration
    kapt {
        correctErrorTypes = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {
    val roomVersion = "2.6.1"

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    // Replace these Activity dependencies with specific versions
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.activity:activity:1.8.2")

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.hilt.android)
    implementation(libs.appcompat)
    implementation(libs.material)
    // Removed the conflicting activity dependency
    implementation(libs.constraintlayout)
    kapt(libs.hilt.compiler)
    implementation(libs.security.crypto)
    implementation(libs.navigation.compose)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")

    // Room dependencies
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.credentials:credentials:1.2.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation ("io.mockk:mockk:1.13.5")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")

    // Android Testing
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation ("io.mockk:mockk-android:1.13.5")

    // Debug Testing
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.5.4")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.5.4")
    // Add SLF4J implementation
    testImplementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Security
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("androidx.biometric:biometric:1.2.0-alpha05")

    // Encryption
    implementation ("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation ("org.mindrot:jbcrypt:0.4")
}