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

package com.opennotes.notes.presentation.notes

import com.opennotes.R
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.opennotes.notes.domain.model.Note
import com.opennotes.notes.presentation.notes.components.NoteItem
import com.opennotes.notes.presentation.util.Screen
import com.opennotes.settings.domain.model.NotesLayout
import com.opennotes.settings.presentation.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.value
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notesPendingDeleteState = remember { mutableStateOf<Set<Note>?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = state.selectedNotes.isNotEmpty()) {
        viewModel.onEvent(NotesEvent.ClearSelection)
    }

    notesPendingDeleteState.value?.let { notesToDelete ->
        AlertDialog(
            onDismissRequest = { notesPendingDeleteState.value = null },
            title = {
                Text(
                    text = if (notesToDelete.size == 1) "Delete note" else "Delete ${notesToDelete.size} notes",
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text =
                        if (notesToDelete.size == 1) {
                            "Are you sure you want to delete this note?"
                        } else {
                            "Are you sure you want to delete these notes?"
                        },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notesPendingDeleteState.value = null
                        viewModel.onEvent(NotesEvent.DeleteSelectedNotes)
                        scope.launch {
                            val result =
                                snackbarHostState.showSnackbar(
                                    message = if (notesToDelete.size == 1) "Note deleted" else "${notesToDelete.size} notes deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short,
                                )
                            if (result == SnackbarResult.ActionPerformed) {
                                // Note: Undo currently only restores one note.
                                // Implementing multi-restore would require a new event.
                                viewModel.onEvent(NotesEvent.RestoreNote)
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { notesPendingDeleteState.value = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditNoteScreen.route)
                },
                shape = RoundedCornerShape(50.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New note",
                    )
                },
                text = {
                    Text(stringResource(R.string.new_note))
                },
            )
        },
    ) { paddingValues ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = 12.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                ) {
                    if (state.selectedNotes.isNotEmpty()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onEvent(NotesEvent.ClearSelection)
                            }) {
                                Icon(Icons.Default.Close, "Clear selection")
                            }
                            Text(
                                text = "${state.selectedNotes.size}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f).padding(start = 16.dp),
                            )
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onEvent(NotesEvent.SelectAllNotes)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = "Select all",
                                )
                            }
                            val allPinned = state.selectedNotes.all { it.isPinned }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onEvent(NotesEvent.TogglePinSelectedNotes)
                            }) {
                                Icon(
                                    imageVector = if (allPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                                    contentDescription = "Toggle Pin",
                                )
                            }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                notesPendingDeleteState.value = state.selectedNotes
                            }) {
                                Icon(Icons.Default.Delete, "Delete selected")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = {
                                viewModel.onEvent(NotesEvent.SearchNote(it))
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(48.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_notes),
                                    modifier = Modifier.size(25.dp),
                                )
                            },
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = {
                                        val newLayout =
                                            if (settings.notesLayout == NotesLayout.GRID) {
                                                NotesLayout.COLUMN
                                            } else {
                                                NotesLayout.GRID
                                            }
                                        settingsViewModel.updateNotesLayout(newLayout)
                                    }) {
                                        Icon(
                                            imageVector =
                                                if (settings.notesLayout == NotesLayout.GRID) {
                                                    Icons.Default.ViewAgenda
                                                } else {
                                                    Icons.Default.GridView
                                                },
                                            contentDescription = stringResource(R.string.notes_layout_toggle),
                                            modifier = Modifier.size(25.dp),
                                        )
                                    }
                                    IconButton(onClick = { showSortSheet = true }) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = "Sort notes",
                                            modifier = Modifier.size(25.dp),
                                        )
                                    }
                                    IconButton(onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            modifier = Modifier.size(25.dp),
                                        )
                                    }
                                }
                            },
                            placeholder = { Text(stringResource(R.string.search_notes)) },
                            singleLine = true,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface,
                                ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val pinnedNotes = state.notes.filter { it.isPinned }
                val otherNotes = state.notes.filter { !it.isPinned }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(if (settings.notesLayout == NotesLayout.GRID) 2 else 1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = "PINNED",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                            )
                        }
                        items(
                            items = pinnedNotes,
                            key = { note -> note.id ?: note.hashCode() },
                        ) { note ->
                            NoteItem(
                                note = note,
                                isSelected = state.selectedNotes.contains(note),
                                modifier = Modifier.fillMaxWidth(),
                                onNoteClick = {
                                    if (state.selectedNotes.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onEvent(NotesEvent.ToggleSelection(note))
                                    } else {
                                        navController.navigate(
                                            Screen.AddEditNoteScreen.route +
                                                "?noteId=${note.id}&noteColor=${note.color}",
                                        )
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onEvent(NotesEvent.ToggleSelection(note))
                                },
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text = "OTHERS",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp),
                                )
                            }
                        }
                        items(
                            items = otherNotes,
                            key = { note -> note.id ?: note.hashCode() },
                        ) { note ->
                            NoteItem(
                                note = note,
                                isSelected = state.selectedNotes.contains(note),
                                modifier = Modifier.fillMaxWidth(),
                                onNoteClick = {
                                    if (state.selectedNotes.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onEvent(NotesEvent.ToggleSelection(note))
                                    } else {
                                        navController.navigate(
                                            Screen.AddEditNoteScreen.route +
                                                "?noteId=${note.id}&noteColor=${note.color}",
                                        )
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onEvent(NotesEvent.ToggleSelection(note))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    if (showSortSheet) {
        SortBottomSheet(
            currentSortOrder = state.sortOrder,
            onSortSelected = { viewModel.onEvent(NotesEvent.SortNotes(it)) },
            onDismiss = { showSortSheet = false },
        )
    }
}
