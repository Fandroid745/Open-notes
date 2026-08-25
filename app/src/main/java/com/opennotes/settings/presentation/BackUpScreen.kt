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

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.opennotes.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri: Uri? ->
                uri?.let { viewModel.onImportClick(it) }
            },
        )
    val exportFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/zip"),
            onResult = { uri: Uri? ->
                uri?.let { viewModel.onExportUriSelected(it) }
            },
        )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(key1 = true) {
        // fix deprecated key
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingsViewModel.UiEvent.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                is SettingsViewModel.UiEvent.OpenExportPicker ->
                    exportFileLauncher.launch(event.suggestedFileName)
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_backup_restore_title),
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    title = stringResource(R.string.export_notes_title),
                    subtitle = stringResource(R.string.export_notes_subtitle),
                    icon = Icons.Default.CloudUpload,
                    onClick = { viewModel.onExportClick() },
                    isFirst = true,
                    trailing = {},
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.import_notes_title),
                    subtitle = stringResource(R.string.import_notes_subtitle),
                    icon = Icons.Default.CloudDownload,
                    onClick = { filePickerLauncher.launch(arrayOf("application/json", "application/zip", "application/x-zip-compressed")) },
                    isLast = true,
                    trailing = {},
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
