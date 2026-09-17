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

package com.opennotes.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.hilt.navigation.compose.hiltViewModel
import com.opennotes.R
import com.opennotes.notes.domain.usecase.NoteUseCases
import com.opennotes.settings.presentation.SettingsViewModel
import com.opennotes.ui.theme.OpenNotesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotesWidgetConfigActivity : ComponentActivity() {
    @Inject
    lateinit var noteUseCases: NoteUseCases

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent {
            val settingsViewModel = hiltViewModel<SettingsViewModel>()
            val settings by settingsViewModel.settings.collectAsState()
            val notes by noteUseCases.getNotes().collectAsState(initial = null)
            val scope = rememberCoroutineScope()

            OpenNotesTheme(settings = settings) {
                Scaffold(
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.widget_select_a_note),
                                    style =
                                        MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            },
                            colors =
                                TopAppBarDefaults.largeTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                                ),
                        )
                    },
                ) { paddingValues ->
                    when {
                        notes == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(paddingValues),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        notes!!.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(paddingValues),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NoteAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = stringResource(R.string.widget_no_notes_found),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 16.dp),
                            ) {
                                items(notes!!) { note ->
                                    Card(
                                        onClick = {
                                            scope.launch {
                                                val glanceId =
                                                    GlanceAppWidgetManager(applicationContext)
                                                        .getGlanceIds(NotesWidget::class.java)
                                                        .firstOrNull {
                                                            GlanceAppWidgetManager(applicationContext)
                                                                .getAppWidgetId(it) == appWidgetId
                                                        }
                                                glanceId?.let {
                                                    updateAppWidgetState(
                                                        applicationContext,
                                                        PreferencesGlanceStateDefinition,
                                                        it,
                                                    ) { prefs ->
                                                        prefs.toMutablePreferences().apply {
                                                            this[intPreferencesKey("note_$appWidgetId")] = note.id!!
                                                        }
                                                    }
                                                    NotesWidget().update(applicationContext, it)
                                                }
                                                val resultValue =
                                                    Intent().putExtra(
                                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                        appWidgetId,
                                                    )
                                                setResult(RESULT_OK, resultValue)
                                                finish()
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor = Color(note.color),
                                            ),
                                    ) {
                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                        ) {
                                            if (note.title.isNotBlank()) {
                                                Text(
                                                    text = note.title,
                                                    style =
                                                        MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                        ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            if (note.content.isNotBlank()) {
                                                Text(
                                                    text = note.content,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
