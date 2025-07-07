package com.opennotes.feature_node.presentation.add_edit_note

import androidx.compose.ui.focus.FocusState

sealed class AddEditNoteEvent {
    data class EnteredTitle(val value:String) : AddEditNoteEvent()
    data class EnteredContent(val value:String) : AddEditNoteEvent()
    data class changeTitleFocus(val focusState:FocusState) : AddEditNoteEvent()
    data class changeContentFocus(val focusState:FocusState) : AddEditNoteEvent()
    data class changeColor(val color:Int) : AddEditNoteEvent()
    object SaveNote : AddEditNoteEvent()
}


