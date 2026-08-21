
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

package com.opennotes.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.opennotes.notes.domain.model.AppIcon
import com.opennotes.settings.domain.model.NotesLayout
import com.opennotes.settings.domain.model.Settings
import com.opennotes.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        companion object {
            // New theme settings
            private val THEME_MODE = stringPreferencesKey("theme_mode")
            private val APP_ICON = stringPreferencesKey("app_icon")
            private val BLACK_THEME = booleanPreferencesKey("black_theme")

            private val COLOR_SCHEME = stringPreferencesKey("color_scheme")
            private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
            private val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
            private val SECURE_SCREEN = booleanPreferencesKey("secure_screen")
            private val NOTES_LAYOUT = stringPreferencesKey("notes_layout")

            // Legacy settings for backward compatibility
            private val DARK_THEME = booleanPreferencesKey("dark_theme")
            private val AUTOMATIC_THEME = booleanPreferencesKey("automatic_theme")
            private val LIGHT_THEME = booleanPreferencesKey("LIGHT_THEME")
        }

        /**
         * Save the whole Settings object
         */
        suspend fun saveSettings(settings: Settings) {
            dataStore.edit { preferences ->
                preferences[THEME_MODE] = settings.themeMode.name
                preferences[APP_ICON] = settings.appIcon.name
                preferences[BLACK_THEME] = settings.blackTheme
                preferences[COLOR_SCHEME] = settings.colorScheme.toString()
                preferences[DYNAMIC_COLOR] = settings.dynamicColor
                preferences[BIOMETRIC_LOCK] = settings.biometricLock
                preferences[SECURE_SCREEN] = settings.secureScreen
                preferences[NOTES_LAYOUT] = settings.notesLayout.name

                // Also update legacy fields for compatibility
                when (settings.themeMode) {
                    ThemeMode.SYSTEM -> {
                        preferences[AUTOMATIC_THEME] = true
                        preferences[DARK_THEME] = false
                        preferences[LIGHT_THEME] = false
                    }

                    ThemeMode.LIGHT -> {
                        preferences[AUTOMATIC_THEME] = false
                        preferences[DARK_THEME] = false
                        preferences[LIGHT_THEME] = true
                    }

                    ThemeMode.DARK -> {
                        preferences[AUTOMATIC_THEME] = false
                        preferences[DARK_THEME] = true
                        preferences[LIGHT_THEME] = false
                    }
                }
            }
        }

        /**
         * Read Settings as Flow
         */
        fun getSettingsFlow(): Flow<Settings> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    val themeModeName = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
                    val themeMode =
                        try {
                            ThemeMode.valueOf(themeModeName)
                        } catch (e: IllegalArgumentException) {
                            ThemeMode.SYSTEM
                        }

                    val appIconName = preferences[APP_ICON] ?: AppIcon.DEFAULT.name
                    val appIcon =
                        try {
                            AppIcon.valueOf(appIconName)
                        } catch (e: IllegalArgumentException) {
                            AppIcon.DEFAULT
                        }

                    val blackTheme = preferences[BLACK_THEME] ?: false
                    val colorScheme = preferences[COLOR_SCHEME]?.toLongOrNull() ?: 0L
                    val dynamicColor = preferences[DYNAMIC_COLOR] ?: true
                    val biometricLock = preferences[BIOMETRIC_LOCK] ?: false
                    val secureScreen = preferences[SECURE_SCREEN] ?: false
                    val notesLayoutName = preferences[NOTES_LAYOUT] ?: NotesLayout.GRID.name
                    val notesLayout =
                        try {
                            NotesLayout.valueOf(notesLayoutName)
                        } catch (e: IllegalArgumentException) {
                            NotesLayout.GRID
                        }

                    Settings(
                        themeMode = themeMode,
                        appIcon = appIcon,
                        blackTheme = blackTheme,
                        colorScheme = colorScheme,
                        dynamicColor = dynamicColor,
                        biometricLock = biometricLock,
                        secureScreen = secureScreen,
                        notesLayout = notesLayout,
                    )
                }

        private fun migrateLegacyThemeMode(
            prefs: Preferences,
            defaultSettings: Settings,
        ): ThemeMode {
            val systemTheme = prefs[AUTOMATIC_THEME] ?: defaultSettings.systemTheme
            val darkTheme = prefs[DARK_THEME] ?: defaultSettings.darkTheme
            val lightTheme = prefs[LIGHT_THEME] ?: defaultSettings.lightTheme

            return when {
                systemTheme -> ThemeMode.SYSTEM
                lightTheme -> ThemeMode.LIGHT
                darkTheme -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

        /**
         * Read Settings once (non-Flow) - Alternative approach
         */
        suspend fun getSettings(): Settings {
            val prefs = dataStore.data.first()
            val defaultSettings = Settings()

            val themeModeString = prefs[THEME_MODE]
            val themeMode =
                if (themeModeString != null) {
                    try {
                        ThemeMode.valueOf(themeModeString)
                    } catch (e: IllegalArgumentException) {
                        migrateLegacyThemeMode(prefs, defaultSettings)
                    }
                } else {
                    migrateLegacyThemeMode(prefs, defaultSettings)
                }

            return Settings(
                themeMode = themeMode,
                blackTheme = prefs[BLACK_THEME] ?: defaultSettings.blackTheme,
                biometricLock = prefs[BIOMETRIC_LOCK] ?: defaultSettings.biometricLock,
                secureScreen = prefs[SECURE_SCREEN] ?: defaultSettings.secureScreen,
                colorScheme = prefs[COLOR_SCHEME]?.toLongOrNull() ?: 0L,
                dynamicColor = prefs[DYNAMIC_COLOR] ?: defaultSettings.dynamicColor,
                notesLayout =
                    prefs[NOTES_LAYOUT]?.let {
                        try {
                            NotesLayout.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            defaultSettings.notesLayout
                        }
                    } ?: defaultSettings.notesLayout,
                // Legacy fields for compatibility
                darkTheme = prefs[DARK_THEME] ?: defaultSettings.darkTheme,
                systemTheme = prefs[AUTOMATIC_THEME] ?: defaultSettings.systemTheme,
                lightTheme = prefs[LIGHT_THEME] ?: defaultSettings.lightTheme,
            )
        }

        /**
         * Clear all preferences
         */
        suspend fun clearAll() {
            dataStore.edit { prefs ->
                prefs.clear()
            }
        }
    }
