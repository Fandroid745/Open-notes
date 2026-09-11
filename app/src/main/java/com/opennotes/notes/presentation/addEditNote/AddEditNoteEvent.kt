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

import androidx.compose.ui.focus.FocusState

sealed class AddEditNoteEvent {
    data class EnteredTitle(
        val value: String,
    ) : AddEditNoteEvent()

    data class EnteredContent(
        val value: String,
    ) : AddEditNoteEvent()

    data class ChangeTitleFocus(
        val focusState: FocusState,
    ) : AddEditNoteEvent()

    data class ChangeContentFocus(
        val focusState: FocusState,
    ) : AddEditNoteEvent()

    data class ChangeColor(
        val color: Int,
    ) : AddEditNoteEvent()

    data class InsertImage(
        val uriString: String,
    ) : AddEditNoteEvent()

    object SaveNote : AddEditNoteEvent()

    data class SetReminder(
        val timestamp: Long?,
        val repeatInterval: Long? = null,
        val repeatUnit: String? = null,
    ) : AddEditNoteEvent()
}
