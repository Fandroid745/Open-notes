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

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opennotes.notes.domain.model.Note
import com.opennotes.notes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel
    @Inject
    constructor(
        private val noteUseCases: NoteUseCases,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _state = mutableStateOf(NotesState())
        val state: State<NotesState> = _state
        private var recentlyDeletedNotes: List<Note> = emptyList()
        private var getNotesJob: Job? = null

        private val savedSortOrder =
            savedStateHandle
                .get<String>("sortOrder")
                ?.let { SortOrder.valueOf(it) }
                ?: SortOrder.DATE_CREATED_NEW

        init {
            getNotes(SortOrder.DATE_CREATED_NEW)
        }

        fun onEvent(event: NotesEvent) {
            when (event) {
                is NotesEvent.DeleteNote -> {
                    viewModelScope.launch {
                        noteUseCases.deleteNote(event.note)
                        recentlyDeletedNotes = listOf(event.note)
                    }
                }

                is NotesEvent.RestoreNote -> {
                    viewModelScope.launch {
                        if (recentlyDeletedNotes.isEmpty()) return@launch
                        recentlyDeletedNotes.forEach { note ->
                            noteUseCases.addNote(note)
                        }
                        recentlyDeletedNotes = emptyList()
                    }
                }

                is NotesEvent.TogglePinNote -> {
                    viewModelScope.launch {
                        noteUseCases.addNote(event.note.copy(isPinned = !event.note.isPinned))
                    }
                }

                is NotesEvent.SearchNote -> {
                    _state.value = state.value.copy(searchQuery = event.query)
                    searchNotes(event.query)
                }

                is NotesEvent.SortNotes -> {
                    savedStateHandle["sortOrder"] = event.sortOrder.name
                    _state.value = state.value.copy(sortOrder = event.sortOrder)
                    getNotes(event.sortOrder)
                }

                is NotesEvent.ToggleSelection -> {
                    val currentSelection = state.value.selectedNotes.toMutableSet()
                    // Use note ID for comparison if possible to avoid instance mismatch
                    val existingNote = currentSelection.find { it.id == event.note.id }
                    if (existingNote != null) {
                        currentSelection.remove(existingNote)
                        state.value.selectedNotes.minus(existingNote)
                    } else {
                        currentSelection.add(event.note)
                    }
                    _state.value = state.value.copy(selectedNotes = currentSelection)
                }

                is NotesEvent.ClearSelection -> {
                    _state.value = state.value.copy(selectedNotes = emptySet())
                }

                is NotesEvent.SelectAllNotes -> {
                    _state.value = state.value.copy(selectedNotes = state.value.notes.toSet())
                }

                is NotesEvent.TogglePinSelectedNotes -> {
                    viewModelScope.launch {
                        val notesToUpdate = state.value.selectedNotes
                        val allPinned = notesToUpdate.all { it.isPinned }
                        notesToUpdate.forEach { note ->
                            noteUseCases.addNote(note.copy(isPinned = !allPinned))
                        }
                        _state.value = state.value.copy(selectedNotes = emptySet())
                    }
                }

                is NotesEvent.DeleteSelectedNotes -> {
                    viewModelScope.launch {
                        val notesToDelete = state.value.selectedNotes.toList()
                        notesToDelete.forEach { note ->
                            noteUseCases.deleteNote(note)
                        }
                        recentlyDeletedNotes = notesToDelete
                        _state.value = state.value.copy(selectedNotes = emptySet())
                    }
                }

                else -> {}
            }
        }

        private fun getNotes(sortOrder: SortOrder) {
            getNotesJob?.cancel()
            getNotesJob =
                noteUseCases
                    .getNotes()
                    .onEach { notes ->
                        val sortedByOrder =
                            when (sortOrder) {
                                SortOrder.DATE_CREATED_NEW -> notes.sortedByDescending { it.createdAt }
                                SortOrder.DATE_CREATED_OLD -> notes.sortedBy { it.createdAt }
                                SortOrder.TITLE_A_Z -> notes.sortedBy { it.title.lowercase() }
                                SortOrder.TITLE_Z_A -> notes.sortedByDescending { it.title.lowercase() }
                            }
                        val sorted = sortedByOrder.sortedByDescending { it.isPinned }
                        _state.value = state.value.copy(notes = sorted, sortOrder = sortOrder)
                    }.launchIn(viewModelScope)
        }

        private fun searchNotes(query: String) {
            getNotesJob?.cancel()
            getNotesJob =
                noteUseCases
                    .searchNotes(query)
                    .onEach { notes ->
                        val sorted = notes.sortedByDescending { it.isPinned }
                        _state.value = state.value.copy(notes = sorted)
                    }.launchIn(viewModelScope)
        }
    }
