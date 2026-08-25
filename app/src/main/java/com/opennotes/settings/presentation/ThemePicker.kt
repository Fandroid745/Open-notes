package com.opennotes.settings.presentation
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.opennotes.R
import com.opennotes.settings.domain.model.ThemeMode

@Composable
fun ThemePicker(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
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
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.theme_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, theme ->
                        SegmentedButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            label = {
                                Text(
                                    when (theme) {
                                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
