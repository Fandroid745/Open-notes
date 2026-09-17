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

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.opennotes.R
import com.opennotes.notes.domain.model.Note

@Composable
fun ZeroState(widgetId: Int) {
    val context = LocalContext.current
    Column(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<NotesWidgetConfigActivity>()),
    ) {
        Text(
            text = context.getString(R.string.widget_tap_to_select),
            style =
                TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
        )
    }
}

@Composable
fun SelectedNote(
    note: Note,
    widgetId: Int,
) {
    val isDefaultColor = note.color == 0xFF000000.toInt() || note.color == 0xFFF5F5F5.toInt()

    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(
                    if (isDefaultColor) {
                        GlanceTheme.colors.surfaceVariant
                    } else {
                        ColorProvider(day = Color(note.color), night = Color(note.color))
                    },
                ).cornerRadius(16.dp)
                .padding(16.dp)
                .clickable(
                    actionStartActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "opennotes://note/${note.id}?noteColor=${note.color}".toUri(),
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    ),
                ),
    ) {
        val contentColor =
            if (isDefaultColor) {
                GlanceTheme.colors.primary
            } else if (Color(note.color).luminance() < 0.5f) {
                ColorProvider(day = Color.White, night = Color.White)
            } else {
                ColorProvider(day = Color.Black, night = Color.Black)
            }

        if (note.title.isNotBlank()) {
            WidgetText(
                markdown = note.title,
                weight = FontWeight.Bold,
                fontSize = 18.sp,
                color = contentColor,
                onContentChange = {},
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
        }
        WidgetText(
            markdown = note.content,
            color = contentColor,
            onContentChange = {},
        )
    }
}
