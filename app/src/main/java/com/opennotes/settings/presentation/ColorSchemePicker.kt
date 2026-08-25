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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.opennotes.R

@Composable
fun ColorSchemePicker(
    currentColor: Long,
    isDynamicColor: Boolean,
    onColorChange: (Long) -> Unit,
) {
    val colorSchemes =
        listOf(
            0xfffeb4a7L,
            0xffffb3c0L,
            0xfffcaaffL,
            0xffb9c3ffL,
            0xff62d3ffL,
            0xff44d9f1L,
            0xff52dbc9L,
            0xff78dd77L,
            0xff9fd75cL,
            0xffc1d02dL,
            0xfffabd00L,
            0xffffb86eL,
            0L,
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.ColorLens,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.color_scheme_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isDynamicColor) stringResource(R.string.dynamic_label) else stringResource(R.string.color_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(colorSchemes) { colorLong ->
                        ColorPickerButton(
                            color = if (colorLong == 0L) MaterialTheme.colorScheme.primary else Color(colorLong),
                            isSelected = !isDynamicColor && colorLong == currentColor,
                            onClick = { onColorChange(colorLong) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = color,
                contentColor = if (color == Color.White) Color.Black else Color.White,
            ),
        modifier = Modifier.size(48.dp),
        onClick = onClick,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color == Color.White) Color.Black else Color.White,
            )
        }
    }
}
