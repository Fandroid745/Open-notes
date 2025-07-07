package com.opennotes.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your color palette
private val DarkColorPalette = darkColors(
    primary = Color.White,
    background = DarkGray,
    onBackground = Color.White,
    surface = LightBlue,
    onSurface = DarkGray
)

// Optional: define your own Typography and Shapes if not using defaults
private val AppTypography = Typography()
private val AppShapes = Shapes()

@Composable
fun OpenNotesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = AppTypography, // ✅ Correct usage
        shapes = AppShapes,         // ✅ Correct usage
        content = content
    )
}
