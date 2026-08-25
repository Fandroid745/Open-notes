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

package com.opennotes.notes.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.opennotes.R
import com.opennotes.notes.presentation.addEditNote.AddEditNoteScreen
import com.opennotes.notes.presentation.notes.NotesScreen
import com.opennotes.notes.presentation.util.Screen
import com.opennotes.settings.domain.model.ThemeMode
import com.opennotes.settings.presentation.AboutScreen
import com.opennotes.settings.presentation.AppearanceSettingsScreen
import com.opennotes.settings.presentation.BackupScreen
import com.opennotes.settings.presentation.PrivacySettingsScreen
import com.opennotes.settings.presentation.SettingsScreen
import com.opennotes.settings.presentation.SettingsViewModel
import com.opennotes.ui.theme.NoteColorPalette
import com.opennotes.ui.theme.OpenNotesTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !settingsViewModel.isLoaded.value }

        biometricPrompt =
            BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        settingsViewModel.setAppUnlocked(true)
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        handleBiometricError(errorCode) {
                            settingsViewModel.setAppUnlocked(true)
                        }
                    }

                    override fun onAuthenticationFailed() = Unit
                },
            )

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentSettings by settingsViewModel.settings.collectAsState()
            val isAppUnlocked by settingsViewModel.isAppUnlocked.collectAsState()
            val isLoaded by settingsViewModel.isLoaded.collectAsState()

            val showContent = isLoaded && (!currentSettings.biometricLock || isAppUnlocked)

            var hasPrompted by androidx.compose.runtime.saveable
                .rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(currentSettings.secureScreen) {
                if (currentSettings.secureScreen) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            LaunchedEffect(isAppUnlocked, currentSettings.biometricLock, isLoaded) {
                if (!isLoaded) return@LaunchedEffect // wait for real settings to load
                if (currentSettings.biometricLock && !isAppUnlocked) {
                    if (!hasPrompted) {
                        val promptInfo =
                            BiometricPrompt.PromptInfo
                                .Builder()
                                .setTitle(getString(R.string.unlock_title))
                                .setSubtitle(getString(R.string.unlock_subtitle))
                                .setAllowedAuthenticators(
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                        BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                                ).build()
                        biometricPrompt.authenticate(promptInfo)
                    }
                } else {
                }
            }

            OpenNotesTheme(settings = currentSettings) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (showContent) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Screen.NotesScreen.route,
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 3 },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                ) + fadeOut(animationSpec = tween(300))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it / 3 },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                ) + fadeOut(animationSpec = tween(300))
                            },
                        ) {
                            composable(route = Screen.NotesScreen.route) {
                                NotesScreen(navController = navController)
                            }
                            composable(
                                route =
                                    Screen.AddEditNoteScreen.route +
                                        "?noteId={noteId}&noteColor={noteColor}",
                                arguments =
                                    listOf(
                                        navArgument("noteId") {
                                            type = NavType.IntType
                                            defaultValue = -1
                                        },
                                        navArgument("noteColor") {
                                            type = NavType.IntType
                                            defaultValue = -1
                                        },
                                    ),
                                deepLinks =
                                    listOf(
                                        navDeepLink { uriPattern = "opennotes://note/{noteId}?noteColor={noteColor}" },
                                    ),
                            ) { backStackEntry ->
                                val color =
                                    backStackEntry.arguments
                                        ?.getInt("noteColor")
                                        ?.takeIf { it != -1 }
                                val isDarkTheme =
                                    when (currentSettings.themeMode) {
                                        ThemeMode.SYSTEM -> isSystemInDarkTheme()
                                        ThemeMode.LIGHT -> false
                                        ThemeMode.DARK -> true
                                    }
                                val resolvedColor =
                                    color ?: if (isDarkTheme) {
                                        NoteColorPalette.Dark.first().toArgb()
                                    } else {
                                        NoteColorPalette.Light.first().toArgb()
                                    }
                                AddEditNoteScreen(
                                    navController = navController,
                                    noteColor = resolvedColor,
                                    isDarkTheme = isDarkTheme,
                                )
                            }
                            composable(route = Screen.SettingsScreen.route) {
                                SettingsScreen(
                                    navController = navController,
                                    viewModel = settingsViewModel,
                                )
                            }
                            composable(route = Screen.BackupScreen.route) {
                                BackupScreen(
                                    navController = navController,
                                    viewModel = settingsViewModel,
                                )
                            }
                            composable(route = Screen.AppearanceSettingsScreen.route) {
                                AppearanceSettingsScreen(
                                    navController = navController,
                                    viewModel = settingsViewModel,
                                )
                            }
                            composable(route = Screen.PrivacySettingsScreen.route) {
                                PrivacySettingsScreen(
                                    navController = navController,
                                    viewModel = settingsViewModel,
                                )
                            }
                            composable(route = Screen.AboutScreen.route) {
                                AboutScreen(navController = navController)
                            }
                        }
                    } else {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                LoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleBiometricError(
        errorCode: Int,
        onComplete: () -> Unit,
    ) {
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED,
            -> finish()

            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_HW_UNAVAILABLE,
            -> {
                settingsViewModel.setAppUnlocked(true)
                settingsViewModel.onBiometricLockToggleRequest(false)
                onComplete()
            }

            else -> finish()
        }
    }
}
