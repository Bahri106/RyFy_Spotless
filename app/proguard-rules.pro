# ProGuard / R8 Rules for RyFy Spotless

# Keep Data Models
-keep class com.bahri.ryfy.model.** { *; }
-keep class com.bahri.ryfy.update.** { *; }

# Media3 & ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# OkHttp & Okio
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Compose rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
