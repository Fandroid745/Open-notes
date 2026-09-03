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

package com.opennotes.notes.presentation.addEditNote.components.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.opennotes.notes.presentation.addEditNote.components.TransParentHintTextField

@Composable
fun MarkdownField(
    titleText: String,
    contentTextFieldValue: TextFieldValue,
    contentColor: Color,
    isPreviewMode: Boolean,
    markdownEnabled: Boolean,
    interactionSource: MutableInteractionSource,
    contentFocusRequester: FocusRequester,
    titleFocusRequester: FocusRequester,
    onTitleChange: (String) -> Unit,
    onTitleFocusChange: (FocusState) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onContentFocusChange: (FocusState) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isPreviewMode) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(8.dp),
        ) {
            MarkdownText(
                radius = 8,
                markdown = titleText.ifBlank { "No title" },
                isPreview = false,
                isReadOnly = true,
                isEnabled = markdownEnabled,
                modifier = Modifier.fillMaxWidth(),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                onContentChange = {},
                settingsViewModel = null,
                textColor = contentColor,
            )
            Spacer(modifier = Modifier.height(16.dp))
            MarkdownText(
                radius = 8,
                markdown = contentTextFieldValue.text.ifBlank { "No content to preview" },
                isPreview = false,
                isReadOnly = true,
                isEnabled = markdownEnabled,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                onContentChange = { newText ->
                    onContentChange(TextFieldValue(text = newText))
                },
                settingsViewModel = null,
                textColor = contentColor,
            )
        }
    } else {
        Column(modifier = modifier) {
            TransParentHintTextField(
                text = titleText,
                hint = "Title",
                onValueChange = onTitleChange,
                onFocusChange = onTitleFocusChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = contentColor),
                focusRequester = titleFocusRequester,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { contentFocusRequester.requestFocus() },
            ) {
                TransParentHintTextField(
                    textFieldValue = contentTextFieldValue,
                    hint = "Content",
                    onValueChange = onContentChange,
                    onFocusChange = onContentFocusChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                    singleLine = false,
                    focusRequester = contentFocusRequester,
                    modifier =
                        Modifier
                            .fillMaxSize(),
                )
            }
        }
    }
}
