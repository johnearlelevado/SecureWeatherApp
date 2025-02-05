# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

# Keep Room entities
-keep class com.example.secureweatherapp.data.local.** { *; }

# Keep API models
-keep class com.example.secureweatherapp.data.model.** { *; }

# Keep security related classes
-keepclassmembers class com.example.secureweatherapp.security.** { *; }

# Keep Retrofit services
-keep,allowobfuscation interface com.example.secureweatherapp.data.api.WeatherApi

# AndroidX Security
-keep class androidx.security.crypto.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Encryption
-keep class javax.crypto.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Remove kotlin metadata
-keepattributes *Annotation*, Signature, Exception
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata { *; }

# Keep source file names for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile