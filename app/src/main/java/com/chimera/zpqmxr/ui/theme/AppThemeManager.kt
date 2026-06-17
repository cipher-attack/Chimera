package com.chimera.zpqmxr.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HapticProfile {
    STEALTH,
    TACTICAL,
    ASSAULT
}

enum class HapticEvent {
    CLICK, SUCCESS, ERROR, PAYLOAD_INJECT, ROOT_GRANTED
}

class HapticEngine private constructor(private val context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    var currentProfile: HapticProfile = HapticProfile.TACTICAL

    fun execute(event: HapticEvent) {
        if (currentProfile == HapticProfile.STEALTH || !vibrator.hasVibrator()) return

        val effect = when (event) {
            HapticEvent.CLICK -> {
                if (currentProfile == HapticProfile.TACTICAL) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                } else {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                }
            }
            HapticEvent.SUCCESS -> {
                val timings = longArrayOf(0, 20, 50, 40)
                val amplitudes = intArrayOf(0, 100, 0, 150)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticEvent.ERROR -> {
                val timings = longArrayOf(0, 50, 50, 50, 50, 80)
                val amplitudes = intArrayOf(0, 150, 0, 150, 0, 200)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
            HapticEvent.PAYLOAD_INJECT -> {
                if (currentProfile == HapticProfile.TACTICAL) {
                    val timings = longArrayOf(0, 10, 100, 10)
                    val amplitudes = intArrayOf(0, 80, 0, 120)
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    val timings = longArrayOf(0, 20, 80, 40)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                }
            }
            HapticEvent.ROOT_GRANTED -> {
                val timings = longArrayOf(0, 20, 40, 20, 60, 20, 100)
                val amplitudes = intArrayOf(0, 50, 0, 100, 0, 150, 0, 255)
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }
        }
        vibrator.vibrate(effect)
    }

    companion object {
        @Volatile
        private var INSTANCE: HapticEngine? = null

        fun getInstance(context: Context): HapticEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HapticEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

enum class AppThemeMode {
    CYBERPUNK_DARK,
    LIGHT_MODE,
    FIELD_OPS,
    TERMINAL_MONOCHROME,
    SLATE_MONOCHROME
}

class ThemeController(private val prefs: android.content.SharedPreferences) {
    private val _currentTheme = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme", AppThemeMode.CYBERPUNK_DARK.name) ?: AppThemeMode.CYBERPUNK_DARK.name)
    )
    val currentTheme: StateFlow<AppThemeMode> = _currentTheme.asStateFlow()

    fun switchTheme(mode: AppThemeMode) {
        _currentTheme.value = mode
        prefs.edit().putString("theme", mode.name).apply()
    }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> { error("No ThemeController provided") }
val LocalHapticEngine = staticCompositionLocalOf<HapticEngine?> { null }

val StealthBlack = Color(0xFF000000)
val StealthSurface = Color(0xFF0A0000)
val StealthSurfaceVariant = Color(0xFF140000)
val StealthRedMuted = Color(0xFF8B0000)
val StealthRedPrimary = Color(0xFFFF0000)
val StealthRedGlow = Color(0x33FF0000)
val StealthRedText = Color(0xFFFF6666)
val StealthTextMuted = Color(0xFF550000)

val FieldOpsColorScheme = androidx.compose.material3.darkColorScheme(
    primary = StealthRedPrimary,
    onPrimary = StealthBlack,
    primaryContainer = StealthSurfaceVariant,
    onPrimaryContainer = StealthRedPrimary,
    secondary = StealthRedMuted,
    onSecondary = StealthBlack,
    secondaryContainer = StealthSurfaceVariant,
    onSecondaryContainer = StealthRedText,
    tertiaryContainer = StealthSurfaceVariant,
    onTertiaryContainer = StealthRedPrimary,
    background = StealthBlack,
    onBackground = StealthRedPrimary,
    surface = StealthSurface,
    onSurface = StealthRedText,
    surfaceVariant = StealthSurfaceVariant,
    onSurfaceVariant = StealthRedMuted,
    error = StealthRedPrimary,
    errorContainer = StealthSurfaceVariant,
    onErrorContainer = StealthRedPrimary
)

val MatrixBlack = Color(0xFF020902)
val MatrixSurface = Color(0xFF041204)
val MatrixSurfaceVariant = Color(0xFF061A06)
val MatrixPrimary = Color(0xFF00FF00)
val MatrixMuted = Color(0xFF004400)
val MatrixTextDim = Color(0xFF008800)
val MatrixTextVeryDim = Color(0xFF005500)

val MatrixColorScheme = androidx.compose.material3.darkColorScheme(
    primary = MatrixPrimary,
    onPrimary = MatrixBlack,
    primaryContainer = MatrixSurfaceVariant,
    onPrimaryContainer = MatrixPrimary,
    secondary = MatrixTextDim,
    onSecondary = MatrixBlack,
    secondaryContainer = MatrixSurfaceVariant,
    onSecondaryContainer = MatrixTextDim,
    tertiaryContainer = MatrixSurfaceVariant,
    onTertiaryContainer = MatrixPrimary,
    background = MatrixBlack,
    onBackground = MatrixTextDim,
    surface = MatrixSurface,
    onSurface = MatrixTextDim,
    surfaceVariant = MatrixSurfaceVariant,
    onSurfaceVariant = MatrixTextVeryDim,
    error = Color(0xFFFF3333),
    errorContainer = MatrixSurfaceVariant,
    onErrorContainer = Color(0xFFFF3333)
)

val GhostBlack = Color(0xFF000000)
val GhostSurface = Color(0xFF050505)
val GhostSurfaceVariant = Color(0xFF0A0A0A)
val GhostWhite = Color(0xFF555555)
val GhostText = Color(0xFF333333)
val GhostTextDim = Color(0xFF1A1A1A)

val GhostColorScheme = androidx.compose.material3.darkColorScheme(
    primary = GhostWhite,
    onPrimary = GhostBlack,
    primaryContainer = GhostSurfaceVariant,
    onPrimaryContainer = GhostWhite,
    secondary = GhostText,
    onSecondary = GhostBlack,
    secondaryContainer = GhostSurfaceVariant,
    onSecondaryContainer = GhostText,
    tertiaryContainer = GhostSurfaceVariant,
    onTertiaryContainer = GhostWhite,
    background = GhostBlack,
    onBackground = GhostText,
    surface = GhostSurface,
    onSurface = GhostText,
    surfaceVariant = GhostSurfaceVariant,
    onSurfaceVariant = GhostTextDim,
    error = GhostWhite,
    errorContainer = GhostSurfaceVariant,
    onErrorContainer = GhostWhite
)
