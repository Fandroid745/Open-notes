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

package com.opennotes.notes.presentation.notes.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opennotes.notes.domain.model.Note
import com.opennotes.notes.presentation.addEditNote.components.markdown.MarkdownText
import com.opennotes.util.contentColorForBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: Note,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    cornerRadius: Dp = 12.dp,
    onNoteClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val backgroundColor = Color(note.color)

    val textColor =
        remember(backgroundColor) {
            backgroundColor.contentColorForBackground()
        }

    val borderColor =
        remember(backgroundColor, isSelected) {
            if (isSelected) {
                textColor
            } else {
                if (textColor == Color.White) {
                    Color.White.copy(alpha = 0.2f)
                } else {
                    Color.Black.copy(alpha = 0.15f)
                }
            }
        }

    val borderWidth = if (isSelected) 3.dp else 1.dp

    Card(
        modifier =
            modifier
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(cornerRadius),
                ).clip(RoundedCornerShape(cornerRadius))
                .combinedClickable(
                    onClick = onNoteClick,
                    onLongClick = onLongClick,
                ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                hoveredElevation = 4.dp,
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .padding(bottom = 36.dp),
            ) {
                if (note.title.isNotBlank()) {
                    MarkdownText(
                        radius = cornerRadius.value.toInt(),
                        markdown = note.title,
                        isPreview = true,
                        isEnabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 16.sp,
                        spacing = 1.dp,
                        textColor = textColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (note.content.isNotBlank()) {
                    MarkdownText(
                        radius = cornerRadius.value.toInt(),
                        markdown = note.content,
                        isPreview = true,
                        isEnabled = true,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp),
                        fontSize = 14.sp,
                        spacing = 1.dp,
                        textColor = textColor,
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = textColor,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(24.dp)
                            .background(backgroundColor, CircleShape),
                )
            }
        }
    }
}
