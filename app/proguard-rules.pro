# ProGuard / R8 Rules for RyFy Spotless

# Keep Data Models
-keep class com.bahri.ryfy.model.** { *; }
-keep class com.bahri.ryfy.update.** { *; }

# Keep Media3 Cache & Data Source
-keep class androidx.media3.datasource.cache.** { *; }
-keep class androidx.media3.database.** { *; }
-dontwarn androidx.media3.**
