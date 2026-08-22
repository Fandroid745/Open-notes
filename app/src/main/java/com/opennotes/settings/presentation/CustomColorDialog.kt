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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opennotes.R

@Composable
fun CustomColorDialog(
    initialColor: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hexText by remember { 
        val argb = initialColor.toArgb()
        // Format as RRGGBB (ignoring alpha for now as per user request for simplicity/certainty)
        mutableStateOf(String.format("%06X", (0xFFFFFF and argb))) 
    }
    
    val parsedColor = remember(hexText) {
        try {
            val sanitized = hexText.removePrefix("#")
            if (sanitized.length == 6) {
                Color(android.graphics.Color.parseColor("#$sanitized"))
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.custom_color),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Preview
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(parsedColor ?: Color.Gray)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )

                OutlinedTextField(
                    value = hexText,
                    onValueChange = { newValue ->
                        // Filter to allow only hex chars and limit length
                        val filtered = newValue.removePrefix("#")
                            .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                            .take(6)
                        hexText = filtered
                    },
                    label = { Text(stringResource(R.string.enter_hex_code)) },
                    prefix = { Text("#") },
                    placeholder = { Text("RRGGBB") },
                    singleLine = true,
                    isError = hexText.isNotEmpty() && parsedColor == null,
                    supportingText = {
                        if (hexText.isNotEmpty() && parsedColor == null) {
                            Text(stringResource(R.string.invalid_hex_code))
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { parsedColor?.let { onConfirm(it) } },
                enabled = parsedColor != null
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
