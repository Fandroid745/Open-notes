package com.opennotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// AMOLED Color Scheme
private val AmoledColorScheme = darkColorScheme(
    primary = Color.White,
    background = PureBlack,
    onBackground = Color.White,
    surface = PureBlack,
    onSurface = Color.White
)

// Light theme
private val LightColorScheme = lightColorScheme(
    primary = DarkGray,
    background = Color.White,
    onBackground = DarkGray,
    surface = Color.LightGray,
    onSurface = Color.Black
)

private val AppTypography = Apptypography
private val AppShapes = Shapes()

@Composable
fun OpenNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use AMOLED for dark mode instead of DarkGray
    val colorScheme = if (darkTheme) AmoledColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
