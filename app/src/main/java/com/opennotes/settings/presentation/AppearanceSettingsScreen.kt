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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.opennotes.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appearance_title),
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                ThemePicker(
                    currentTheme = settings.themeMode,
                    onThemeSelected = { viewModel.updateThemeMode(it) },
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            item {
                SettingItem(
                    title = stringResource(R.string.dynamic_color_title),
                    subtitle = stringResource(R.string.dynamic_color_subtitle),
                    icon = Icons.Default.Wallpaper,
                    trailing = {
                        SettingsSwitch(
                            isChecked = settings.dynamicColor,
                            onCheckedChange = { viewModel.updateDynamicColor(it) },
                        )
                    },
                    isFirst = false,
                    isLast = false,
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            item {
                ColorSchemePicker(
                    currentColor = settings.colorScheme,
                    isDynamicColor = settings.dynamicColor,
                    onColorChange = { viewModel.updateColorScheme(it) },
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            item {
                SettingItem(
                    title = stringResource(R.string.black_theme_title),
                    subtitle = if (settings.blackTheme) stringResource(R.string.black_theme_subtitle_enabled) else stringResource(R.string.black_theme_subtitle_disabled),
                    icon = Icons.Default.DarkMode,
                    trailing = {
                        SettingsSwitch(
                            isChecked = settings.blackTheme,
                            onCheckedChange = { viewModel.updateBlackTheme(it) },
                        )
                    },
                    isFirst = false,
                    isLast = false,
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            item {
                AppIconPicker(
                    currentIcon = settings.appIcon,
                    onIconChange = { selectedIcon ->
                        viewModel.setAppIcon(selectedIcon)
                        AppIconManager.setAppIcon(context, selectedIcon)
                    },
                )
            }
        }
    }
}
