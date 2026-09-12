package com.spycodedoodledates.ui.theme.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_settings")

data class CustomTheme(
    val background: Color,
    val paper: Color,
    val accent: Color
)

val DefaultTheme = CustomTheme(
    background = Color(0xFF1B2430), // Dark ink navy
    paper = Color(0xFFF6F1E7),      // Warm paper
    accent = Color(0xFFC9A227)      // Brass accent
)

val CyberpunkTheme = CustomTheme(
    background = Color(0xFF0D0221),
    paper = Color(0xFF2DE2E6),
    accent = Color(0xFFD40078)
)

val VintageTheme = CustomTheme(
    background = Color(0xFF3E2723),
    paper = Color(0xFFD7CCC8),
    accent = Color(0xFF8D6E63)
)

val ForestTheme = CustomTheme(
    background = Color(0xFF1B5E20),
    paper = Color(0xFFE8F5E9),
    accent = Color(0xFF4CAF50)
)

val MinimalistTheme = CustomTheme(
    background = Color(0xFFFFFFFF),
    paper = Color(0xFFF5F5F5),
    accent = Color(0xFF212121)
)

val SakuraTheme = CustomTheme(
    background = Color(0xFFFFE0E9),
    paper = Color(0xFFFFF0F5),
    accent = Color(0xFFFF69B4)
)

val DeepSeaTheme = CustomTheme(
    background = Color(0xFF001219),
    paper = Color(0xFF005F73),
    accent = Color(0xFF94D2BD)
)

val SunsetTheme = CustomTheme(
    background = Color(0xFF2B2D42),
    paper = Color(0xFFEF233C),
    accent = Color(0xFFFFB703)
)

val MidnightTheme = CustomTheme(
    background = Color(0xFF000000),
    paper = Color(0xFF14213D),
    accent = Color(0xFFFCA311)
)

val LavenderTheme = CustomTheme(
    background = Color(0xFFE6E6FA),
    paper = Color(0xFFF3E5F5),
    accent = Color(0xFF9575CD)
)

data class AppSettingsState(
    val theme: CustomTheme,
    val vibrationEnabled: Boolean,
    val ringtoneUri: String?
)

class ThemeSettings(private val context: Context) {
    private val backgroundKey = stringPreferencesKey("background_color")
    private val paperKey = stringPreferencesKey("paper_color")
    private val accentKey = stringPreferencesKey("accent_color")
    private val vibrationKey = booleanPreferencesKey("vibration_enabled")
    private val ringtoneKey = stringPreferencesKey("ringtone_uri")

    val settingsFlow: Flow<AppSettingsState> = context.dataStore.data.map { preferences ->
        AppSettingsState(
            theme = CustomTheme(
                background = preferences[backgroundKey]?.let { Color(android.graphics.Color.parseColor(it)) } ?: DefaultTheme.background,
                paper = preferences[paperKey]?.let { Color(android.graphics.Color.parseColor(it)) } ?: DefaultTheme.paper,
                accent = preferences[accentKey]?.let { Color(android.graphics.Color.parseColor(it)) } ?: DefaultTheme.accent
            ),
            vibrationEnabled = preferences[vibrationKey] ?: true,
            ringtoneUri = preferences[ringtoneKey]
        )
    }

    val themeFlow: Flow<CustomTheme> = settingsFlow.map { it.theme }

    suspend fun updateTheme(newTheme: CustomTheme) {
        context.dataStore.edit { preferences ->
            preferences[backgroundKey] = String.format("#%06X", (0xFFFFFF and newTheme.background.toArgb()))
            preferences[paperKey] = String.format("#%06X", (0xFFFFFF and newTheme.paper.toArgb()))
            preferences[accentKey] = String.format("#%06X", (0xFFFFFF and newTheme.accent.toArgb()))
        }
    }

    suspend fun updateVibration(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[vibrationKey] = enabled
        }
    }

    suspend fun updateRingtone(uri: String?) {
        context.dataStore.edit { preferences ->
            preferences[ringtoneKey] = uri ?: ""
        }
    }

    suspend fun resetToDefault() {
        updateTheme(DefaultTheme)
        updateVibration(true)
        updateRingtone(null)
    }
}
