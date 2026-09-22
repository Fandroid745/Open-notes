/*
 *
 *  *  Copyright (c) 2026 Dhanush Sugganahalli <dhanush41230@gmail.com>
 *  *
 *  *  This program is free software; you can redistribute it and/or modify it under
 *  *  the terms of the GNU General Public License as published by the Free Software
 *  *  Foundation; either version 3 of the License, or (at your option) any later
 *  *  version.
 *  *
 *  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with
 *  *  this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.opennotes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamicColorScheme
import com.opennotes.settings.domain.model.Settings
import com.opennotes.settings.domain.model.ThemeMode

// AMOLED Color Scheme - preserves Material Design colors with black background
private val AmoledColorScheme =
    darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF3700B3),
        onPrimaryContainer = Color(0xFFE1BEFF),
        secondary = Color(0xFF03DAC6),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF005047),
        onSecondaryContainer = Color(0xFF70F2E8),
        tertiary = Color(0xFFCF6679),
        onTertiary = Color.Black,
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        surfaceVariant = Color(0xFF111111),
        onSurfaceVariant = Color(0xFFCAC4D0),
        surfaceContainer = Color(0xFF080808),
        surfaceContainerHigh = Color(0xFF0A0A0A),
        surfaceContainerHighest = Color(0xFF1A1A1A),
        surfaceContainerLow = Color.Black,
        surfaceContainerLowest = Color.Black,
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        error = Color(0xFFCF6679),
        onError = Color.Black,
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
    )

// Regular Dark Color Scheme
private val DarkColorScheme =
    darkColorScheme(
        primary = Color.White,
        background = Color(0xFF121212),
        onBackground = Color.White,
        surface = Color(0xFF1E1E1E),
        onSurface = Color.White,
        surfaceContainer = Color(0xFF2A2A2A),
        surfaceContainerHigh = Color(0xFF333333),
        surfaceContainerHighest = Color(0xFF3A3A3A),
    )

// Light theme
private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF333333),
        background = Color.White,
        onBackground = Color(0xFF333333),
        surface = Color(0xFFF5F5F5),
        onSurface = Color.Black,
        surfaceContainer = Color(0xFFEEEEEE),
        surfaceContainerHigh = Color(0xFFE8E8E8),
        surfaceContainerHighest = Color(0xFFE0E0E0),
    )

private val AppTypography = Apptypography

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OpenNotesTheme(
    settings: Settings,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()

    val isDarkTheme =
        when (settings.themeMode) {
            ThemeMode.SYSTEM -> systemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

    val initialColorScheme =
        when {
            // Dynamic colors on Android 12+
            settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val scheme =
                    if (isDarkTheme) {
                        dynamicDarkColorScheme(context)
                    } else {
                        dynamicLightColorScheme(context)
                    }

                // Override secondaryContainer to prevent unexpected OEM Monet colors (e.g., pink on green wallpaper)
                scheme.copy(
                    secondaryContainer = scheme.primaryContainer,
                    onSecondaryContainer = scheme.onPrimaryContainer,
                )
            }
            // Custom color scheme selected
            settings.colorScheme != 0L -> {
                val seedColor = Color(settings.colorScheme)
                dynamicColorScheme(seedColor, isDark = isDarkTheme, isAmoled = isDarkTheme && settings.blackTheme)
            }
            // Fallback
            else -> {
                when {
                    isDarkTheme && settings.blackTheme -> AmoledColorScheme
                    isDarkTheme -> DarkColorScheme
                    else -> LightColorScheme
                }
            }
        }

    val colorScheme =
        if (isDarkTheme && settings.blackTheme) {
            initialColorScheme.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF111111),
                surfaceContainer = Color(0xFF080808),
                surfaceContainerLow = Color.Black,
                surfaceContainerLowest = Color.Black,
                surfaceContainerHigh = Color(0xFF0A0A0A),
                surfaceContainerHighest = Color(0xFF1A1A1A),
                outlineVariant = Color(0xFF49454F),
            )
        } else {
            initialColorScheme
        }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
