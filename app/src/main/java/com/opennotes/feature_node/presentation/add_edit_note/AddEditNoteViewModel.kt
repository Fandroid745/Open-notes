package com.opennotes.feature_node.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opennotes.feature_node.domain.model.InvalidNoteException
import com.opennotes.feature_node.domain.model.Note
import com.opennotes.feature_node.domain.use_case.NoteUseCases
import com.opennotes.feature_node.presentation.add_edit_note.AddEditNoteEvent
import com.opennotes.feature_node.presentation.add_edit_note.NoteTextFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    savedStateHandle : SavedStateHandle
) : ViewModel()
{

    private val _noteTitle= mutableStateOf(
        NoteTextFieldState(
    hint= "Enter title ...."
    )
    )

    val noteTitle: State<NoteTextFieldState> =_noteTitle


    private val _noteContent= mutableStateOf(
        NoteTextFieldState(
        hint="Enter content...."
    )
    )
    val noteContent:State<NoteTextFieldState> = _noteContent


    private val _noteColor= mutableStateOf(Note.noteColors.random().toArgb())
    val noteColor:State<Int> = _noteColor


    private val _eventFlow= MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()




private var currentNoteId:Int ? = null


    init{
        savedStateHandle.get<Int>("noteId")?.let{
            noteId->
            if(noteId!=-1){
                viewModelScope.launch{
                    noteUseCases.getNote(noteId)?.also{ note->
                        currentNoteId  = note.id
                        _noteTitle.value= noteTitle.value.copy(
                            text= note.title,
                            isHintVisible = false
                        )
                        _noteContent.value =_noteContent.value.copy(
                            text=note.content,
                            isHintVisible=false
                        )
                        _noteColor.value = note.color
                    }
                }
            }
        }
    }
    fun onEvent (event: AddEditNoteEvent) {
        when (event) {
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = noteTitle.value.copy(
                    text = event.value
                )
            }


            is AddEditNoteEvent.changeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                isHintVisible= !event.focusState.isFocused && _noteContent.value.text.isBlank()
                )
            }

            is AddEditNoteEvent.EnteredContent -> {
                _noteContent.value=_noteContent.value.copy(
                    text=event.value
                )
            }
             is AddEditNoteEvent.SaveNote ->{
                viewModelScope.launch{
                    try{
                        noteUseCases.addNote(
                            Note(
                                title = noteTitle.value.text,
                                content = noteContent.value.text,
                                color = noteColor.value,
                                timestamp = System.currentTimeMillis().toString(),
                                id = currentNoteId,
                            )
                        )

                        _eventFlow.emit(UiEvent.SavedNote)
                    } catch(e: InvalidNoteException){
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )
                    }
                }
            }
            is AddEditNoteEvent.changeColor -> {
                _noteColor.value= event.color
            }
            is AddEditNoteEvent.changeContentFocus -> {
                _noteContent.value= _noteContent.value.copy(
                    isHintVisible=!event.focusState.isFocused && _noteContent.value.text.isBlank()
                )
            }
        }
    }
    sealed class UiEvent{
        data class ShowSnackbar(val message:String): UiEvent()
        object SavedNote : UiEvent()
    }
}