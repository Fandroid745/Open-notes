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

package com.opennotes.notes.data.datasource

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.opennotes.notes.domain.model.Note

@Entity(tableName = "Note")
data class NoteEntity(
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    val isPinned: Boolean = false,
    val reminderTime: Long? = null,
    val repeatInterval: Long? = null,
    val repeatUnit: String? = null,
    @PrimaryKey val id: Int? = null,
) {
    fun toNote(): Note =
        Note(
            title = title,
            content = content,
            timestamp = timestamp,
            color = color,
            isPinned = isPinned,
            reminderTime = reminderTime,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            id = id,
        )
}

fun Note.toNoteEntity(): NoteEntity =
    NoteEntity(
        title = title,
        content = content,
        timestamp = timestamp,
        color = color,
        isPinned = isPinned,
        reminderTime = reminderTime,
        repeatInterval = repeatInterval,
        repeatUnit = repeatUnit,
        id = id,
    )
