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

package com.opennotes.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.opennotes.R
import com.opennotes.notes.domain.model.AppIcon

@Composable
fun AppIconPicker(
    currentIcon: AppIcon,
    onIconChange: (AppIcon) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var pendingIcon by remember { mutableStateOf<AppIcon?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingItem(
            title = stringResource(R.string.app_icon_color_title),
            subtitle = currentIcon.title,
            icon = Icons.Default.AppShortcut,
            onClick = { isExpanded = !isExpanded },
            trailing = {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            isFirst = false,
            isLast = !isExpanded, // If expanded, the row below will be the 'last' visual element
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300)),
        ) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(modifier = Modifier.width(4.dp)) }
                items(AppIcon.values()) { icon ->
                    val color = Color(icon.colorHex)
                    val isSelected = currentIcon == icon

                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                ).background(color)
                                .clickable {
                                    if (currentIcon != icon) {
                                        pendingIcon = icon
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (color.luminance() > 0.5) Color.Black else Color.White,
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.width(4.dp)) }
            }
        }
    }

    if (pendingIcon != null) {
        AlertDialog(
            onDismissRequest = { pendingIcon = null },
            title = { Text(stringResource(R.string.app_icon_change_title)) },
            text = {
                Text(stringResource(R.string.app_icon_change_message))
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingIcon?.let { onIconChange(it) }
                    pendingIcon = null
                }) {
                    Text(stringResource(R.string.app_icon_restart))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingIcon = null }) {
                    Text(stringResource(R.string.app_icon_cancel))
                }
            },
        )
    }
}
