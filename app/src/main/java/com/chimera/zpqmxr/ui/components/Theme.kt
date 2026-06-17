package com.chimera.zpqmxr.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = primaryCustom,
    onPrimary = JetBlack,
    primaryContainer = primaryContainerCustom,
    onPrimaryContainer = primaryCustom,
    secondaryContainer = secondaryContainerCustom,
    onSecondaryContainer = secondaryCustom,
    tertiaryContainer = SurfaceVariantDark,
    onTertiaryContainer = DarkText,
    background = JetBlack,
    onBackground = DarkText,
    surface = SurfaceDark,
    onSurface = DarkText,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = DarkTextMuted,
    error = errorCustom,
    errorContainer = errorContainerCustom,
    onErrorContainer = errorCustom
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF0D47A1),
    tertiaryContainer = Color(0xFFE5E5EA),
    onTertiaryContainer = Color.Black,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    error = errorCustom,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = errorCustom
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("chimera_settings", android.content.Context.MODE_PRIVATE)
    val themeController = androidx.compose.runtime.remember { ThemeController(sharedPrefs) }
    val currentThemeMode by themeController.currentTheme.collectAsState()
    
    val colorScheme = when (currentThemeMode) {
        AppThemeMode.CYBERPUNK_DARK -> DarkColorScheme
        AppThemeMode.LIGHT_MODE -> LightColorScheme
        AppThemeMode.FIELD_OPS -> FieldOpsColorScheme
        AppThemeMode.TERMINAL_MONOCHROME -> MatrixColorScheme
        AppThemeMode.SLATE_MONOCHROME -> GhostColorScheme
    }

    val hapticEngine = HapticEngine.getInstance(context)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val isLightStatusBar = currentThemeMode == AppThemeMode.LIGHT_MODE
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightStatusBar
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLightStatusBar
        }
    }

    CompositionLocalProvider(
        LocalThemeController provides themeController,
        LocalHapticEngine provides hapticEngine
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}

