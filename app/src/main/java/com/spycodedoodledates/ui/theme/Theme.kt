package com.spycodedoodledates.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.spycodedoodledates.ui.theme.settings.CustomTheme
import com.spycodedoodledates.ui.theme.settings.DefaultTheme
import com.spycodedoodledates.ui.theme.settings.ThemeSettings

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

val LocalCustomTheme = staticCompositionLocalOf { DefaultTheme }

@Composable
fun DoodleDatesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeSettings: ThemeSettings? = null,
    content: @Composable () -> Unit
) {
    val customThemeState = if (themeSettings != null) {
        themeSettings.themeFlow.collectAsState(initial = DefaultTheme).value
    } else {
        DefaultTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }.copy(
        background = customThemeState.background,
        primary = customThemeState.accent,
        surface = customThemeState.paper
    )

    CompositionLocalProvider(LocalCustomTheme provides customThemeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
