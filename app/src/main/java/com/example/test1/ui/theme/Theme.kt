package com.example.test1.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ui/theme/Color.kt
/*private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF212C5D),
    secondary = Color(0xFFE8EAF6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color(0xFF1F1F1F),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB0C4FF),
    secondary = Color(0xFFE8EAF6),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)*/
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF212C5D),
    secondary = Color(0xFFE8EAF6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFE8EAF6), // Lekko szara powierzchnia
    onPrimary = Color.White,
    onSecondary = Color(0xFF212C5D), // Ciemniejszy tekst na jasnym tle
    onBackground = Color(0xFF1F1F1F),
    onSurface = Color(0xFF1F1F1F),
    secondaryContainer = Color(0xFFDCEEEB),
    tertiaryContainer = Color(0xFFE8EAF6),
    onSecondaryContainer = Color.Black, // Ciemniejszy tekst na jasnym tle
    onTertiaryContainer = Color(0xFF1F1F1F),
    error = Color(0xFFD32F2F),

)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF57C00), // Jaśniejszy, ale wciąż nasycony niebieski
    secondary = Color(0xFF2E3A66), // Ciemniejsze tło dla elementów
    background = Color(0xFF121212), // Głęboka czerń tła
    surface = Color(0xFF1E1E1E), // Nieco jaśniejsza powierzchnia kart
    onPrimary = Color(0xFFE8EAF6), // Ciemny tekst na jasnym przycisku
    onSecondary = Color.White,
    onBackground = Color(0xFFE0E0E0), // Lekko szary tekst
    onSurface = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFFEADDFF),
    tertiaryContainer = Color(0xFF523A3A),
    secondaryContainer = Color(0xFF40445C),
    onTertiaryContainer = Color(0xFFEADDFF),
    error = Color(0xFFD32F2F)

)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Ustawiamy pasek na przezroczysty, bo używasz enableEdgeToEdge
            window.statusBarColor = Color.Transparent.toArgb()
            // Ta linia kontroluje, czy ikony na pasku (zegar, bateria) są ciemne czy jasne
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}