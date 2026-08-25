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

package com.opennotes.settings.presentation

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.opennotes.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingsViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = event.message)
                    }
                }

                is SettingsViewModel.UiEvent.RequestBiometricAuth -> {
                    val enable = event.enable
                    if (activity == null) {
                        viewModel.onBiometricAuthFailed()
                        return@collectLatest
                    }

                    val biometricManager = androidx.biometric.BiometricManager.from(context)
                    val canAuthenticate =
                        biometricManager.canAuthenticate(
                            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                        )

                    if (canAuthenticate != androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.error_biometric_unavailable))
                        }
                        viewModel.onBiometricAuthFailed()
                        return@collectLatest
                    }

                    val executor = ContextCompat.getMainExecutor(context)
                    val prompt =
                        BiometricPrompt(
                            activity,
                            executor,
                            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    viewModel.onBiometricAuthSuccess(enable)
                                }

                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence,
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    viewModel.onBiometricAuthFailed()
                                }
                            },
                        )

                    val promptInfo =
                        androidx.biometric.BiometricPrompt.PromptInfo
                            .Builder()
                            .setTitle(if (enable) context.getString(R.string.biometric_enable_title) else context.getString(R.string.biometric_disable_title))
                            .setSubtitle(
                                if (enable) context.getString(R.string.biometric_enable_subtitle) else context.getString(R.string.biometric_disable_subtitle),
                            ).setAllowedAuthenticators(
                                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                            ).build()

                    prompt.authenticate(promptInfo)
                }

                else -> {}
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.privacy_title),
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.15.sp,
                            ),
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item {
                SettingItem(
                    title = stringResource(R.string.biometric_lock_title),
                    subtitle =
                        if (settings.biometricLock) {
                            stringResource(R.string.biometric_lock_subtitle_enabled)
                        } else {
                            stringResource(R.string.biometric_lock_subtitle_disabled)
                        },
                    icon = Icons.Default.Fingerprint,
                    trailing = {
                        SettingsSwitch(
                            isChecked = settings.biometricLock,
                            onCheckedChange = { enabled ->
                                viewModel.onBiometricLockToggleRequest(enabled)
                            },
                        )
                    },
                    isFirst = true,
                    isLast = false,
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.secure_screen_title),
                    subtitle = stringResource(R.string.secure_screen_subtitle),
                    icon = Icons.Default.Security,
                    trailing = {
                        SettingsSwitch(
                            isChecked = settings.secureScreen,
                            onCheckedChange = { enabled ->
                                viewModel.onSecureScreenToggleRequest(enabled)
                            },
                        )
                    },
                    isFirst = false,
                    isLast = true,
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
