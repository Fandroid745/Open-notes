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

package com.opennotes.notes.presentation.addEditNote

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.opennotes.notes.data.repository.FileHandler
import com.opennotes.notes.domain.model.Note
import com.opennotes.notes.domain.usecase.NoteUseCases
import com.opennotes.notes.presentation.reminder.ReminderWorker
import com.opennotes.ui.theme.NoteColorPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel
    @Inject
    constructor(
        private val noteUseCases: NoteUseCases,
        private val fileHandler: FileHandler,
        private val savedStateHandle: SavedStateHandle,
        private val application: Application,
    ) : ViewModel() {
        private val _noteTitle =
            mutableStateOf(
                NoteTextFieldState(
                    text = savedStateHandle.get<String>("title") ?: "",
                    hint = "Enter title ....",
                ),
            )
        val noteTitle: State<NoteTextFieldState> = _noteTitle

        private val _noteContent =
            mutableStateOf(
                NoteTextFieldState(
                    text = savedStateHandle.get<String>("content") ?: "",
                    hint = "Enter content....",
                ),
            )
        val noteContent: State<NoteTextFieldState> = _noteContent

        private val _noteColor =
            mutableIntStateOf(
                savedStateHandle.get<Int>("color") ?: NoteColorPalette.Light.first().toArgb(),
            )
        val noteColor: State<Int> = _noteColor

        private val _noteTimestamp = mutableStateOf<Long?>(null)
        val noteTimestamp: State<Long?> = _noteTimestamp

        private val _noteReminderTime = mutableStateOf<Long?>(null)
        val noteReminderTime: State<Long?> = _noteReminderTime

        private val _noteRepeatInterval = mutableStateOf<Long?>(null)
        val noteRepeatInterval: State<Long?> = _noteRepeatInterval

        private val _noteRepeatUnit = mutableStateOf<String?>(null)
        val noteRepeatUnit: State<String?> = _noteRepeatUnit

        private val _eventFlow = MutableSharedFlow<UiEvent>()
        val eventFlow = _eventFlow.asSharedFlow()

        private var currentNoteId: Int? = null
        private var currentIsPinned: Boolean = false
        private var autoSaveJob: Job? = null

        private fun triggerAutoSave() {
            autoSaveJob?.cancel()
            autoSaveJob =
                viewModelScope.launch {
                    delay(1000L)
                    saveNoteInternal()
                }
        }

        private fun scheduleReminderWork(
            noteId: Int,
            triggerTime: Long,
        ) {
            val delay = triggerTime - System.currentTimeMillis()
            if (delay <= 0) return

            val data =
                Data
                    .Builder()
                    .putInt("NOTE_ID", noteId)
                    .putString("NOTE_TITLE", noteTitle.value.text.takeIf { it.isNotBlank() } ?: "Reminder")
                    .putString("NOTE_CONTENT", noteContent.value.text.takeIf { it.isNotBlank() } ?: "Open note to view details")
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()

            WorkManager.getInstance(application).enqueueUniqueWork(
                "reminder_work_$noteId",
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )
        }

        private fun cancelReminderWork(noteId: Int) {
            WorkManager.getInstance(application).cancelUniqueWork("reminder_work_$noteId")
        }

        private suspend fun saveNoteInternal(): Int? {
            val title = noteTitle.value.text
            val content = noteContent.value.text
            if (title.isBlank() && content.isBlank()) {
                return null
            }
            try {
                val note =
                    Note(
                        title = title,
                        content = content,
                        color = noteColor.value,
                        timestamp = System.currentTimeMillis(),
                        isPinned = currentIsPinned,
                        reminderTime = noteReminderTime.value,
                        repeatInterval = noteRepeatInterval.value,
                        repeatUnit = noteRepeatUnit.value,
                        id = currentNoteId,
                    )
                val insertedId = noteUseCases.addNote(note)
                if (currentNoteId == null) {
                    currentNoteId = insertedId
                }
                return insertedId
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        init {
            savedStateHandle.get<Int>("noteId")?.let { noteId ->
                if (noteId != -1) {
                    viewModelScope.launch {
                        noteUseCases.getNote(noteId)?.also { note ->
                            currentNoteId = note.id
                            currentIsPinned = note.isPinned
                            _noteTimestamp.value = note.timestamp
                            _noteReminderTime.value = note.reminderTime
                            _noteRepeatInterval.value = note.repeatInterval
                            _noteRepeatUnit.value = note.repeatUnit
                            if (savedStateHandle.get<String>("title") == null) {
                                _noteTitle.value =
                                    noteTitle.value.copy(
                                        text = note.title,
                                        isHintVisible = false,
                                    )
                                _noteContent.value =
                                    _noteContent.value.copy(
                                        text = note.content,
                                        isHintVisible = false,
                                    )

                                if (savedStateHandle.get<Int>("color") == null) {
                                    _noteColor.intValue = note.color
                                }
                            }
                        }
                    }
                }
            }
        }

        fun applyDefaultColor(isDarkTheme: Boolean) {
            if (_noteColor.intValue == NoteColorPalette.Light.first().toArgb()) {
                _noteColor.intValue =
                    if (isDarkTheme) {
                        NoteColorPalette.Dark.first().toArgb()
                    } else {
                        NoteColorPalette.Light.first().toArgb()
                    }
            }
        }

        fun onEvent(event: AddEditNoteEvent) {
            when (event) {
                is AddEditNoteEvent.EnteredTitle -> {
                    _noteTitle.value = noteTitle.value.copy(text = event.value)
                    savedStateHandle["title"] = event.value // persist to SavedStateHandle
                    triggerAutoSave()
                }
                is AddEditNoteEvent.ChangeTitleFocus -> {
                    _noteTitle.value =
                        noteTitle.value.copy(
                            isHintVisible = !event.focusState.isFocused && _noteTitle.value.text.isBlank(),
                        )
                }
                is AddEditNoteEvent.EnteredContent -> {
                    _noteContent.value = _noteContent.value.copy(text = event.value)
                    savedStateHandle["content"] = event.value // persist to SavedStateHandle
                    triggerAutoSave()
                }
                is AddEditNoteEvent.ChangeColor -> {
                    _noteColor.intValue = event.color
                    savedStateHandle["color"] = event.color // persist to SavedStateHandle
                    triggerAutoSave()
                }
                is AddEditNoteEvent.SaveNote -> {
                    autoSaveJob?.cancel()
                    viewModelScope.launch {
                        if (noteTitle.value.text.isBlank() && noteContent.value.text.isBlank()) {
                            _eventFlow.emit(UiEvent.SavedNote)
                            return@launch
                        }
                        val resultId = saveNoteInternal()
                        if (resultId != null) {
                            _eventFlow.emit(UiEvent.SavedNote)
                        } else {
                            _eventFlow.emit(
                                UiEvent.ShowSnackbar(
                                    message = "Couldn't save note",
                                ),
                            )
                        }
                    }
                }
                is AddEditNoteEvent.ChangeContentFocus -> {
                    _noteContent.value =
                        _noteContent.value.copy(
                            isHintVisible = !event.focusState.isFocused && _noteContent.value.text.isBlank(),
                        )
                }
                is AddEditNoteEvent.InsertImage -> {
                    viewModelScope.launch {
                        try {
                            val localPath = fileHandler.saveImageToInternalStorage(event.uriString)
                            if (localPath != null) {
                                val currentText = _noteContent.value.text
                                val newText =
                                    if (currentText.isBlank()) {
                                        "!($localPath)\n"
                                    } else {
                                        "$currentText\n\n!($localPath)\n"
                                    }
                                _noteContent.value = _noteContent.value.copy(text = newText)
                                savedStateHandle["content"] = newText
                            } else {
                                _eventFlow.emit(UiEvent.ShowSnackbar("Failed to insert image"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            _eventFlow.emit(UiEvent.ShowSnackbar("Failed to insert image: ${e.message}"))
                        }
                    }
                }
                is AddEditNoteEvent.SetReminder -> {
                    viewModelScope.launch {
                        _noteReminderTime.value = event.timestamp
                        _noteRepeatInterval.value = event.repeatInterval
                        _noteRepeatUnit.value = event.repeatUnit
                        val noteId = saveNoteInternal()
                        if (noteId != null) {
                            if (event.timestamp != null) {
                                scheduleReminderWork(noteId, event.timestamp)
                                _eventFlow.emit(UiEvent.ShowSnackbar("Reminder set successfully"))
                            } else {
                                cancelReminderWork(noteId)
                                _eventFlow.emit(UiEvent.ShowSnackbar("Reminder removed"))
                            }
                        } else {
                            _eventFlow.emit(UiEvent.ShowSnackbar("Cannot set reminder on empty note"))
                        }
                    }
                }
            }
        }

        sealed class UiEvent {
            data class ShowSnackbar(
                val message: String,
            ) : UiEvent()

            object SavedNote : UiEvent()
        }
    }
