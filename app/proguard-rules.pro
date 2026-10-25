# ProGuard / R8 Rules for RyFy Spotless

# Keep Data Models & Core Handlers
-keep class com.bahri.ryfy.model.** { *; }
-keep class com.bahri.ryfy.update.** { *; }
-keep class com.bahri.ryfy.data.** { *; }
-keep class com.bahri.ryfy.core.** { *; }

# Media3 & ExoPlayer - Keep all classes, interfaces, AIDL stubs
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
