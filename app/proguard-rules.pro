# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers enum * { *; }

# Keep data classes and models
-keep class com.chimera.zpqmxr.data.** { *; }

# Keep Bluetooth reflection targets
-keepclassmembers class android.bluetooth.BluetoothDevice {
    boolean setPairingConfirmation(boolean);
    boolean setPin(byte[]);
    boolean createBond();
}

# Keep but allow obfuscation of utils
-keep,allowobfuscation class com.chimera.zpqmxr.utils.** { *; }

# CRITICAL: Do not obfuscate NativeBridge or JNI breaks
-keep class com.chimera.zpqmxr.utils.NativeBridge { *; }

# Keep native methods across all classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Room related
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }

# Keep Lifecycle and ViewModel
-keep class androidx.lifecycle.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
