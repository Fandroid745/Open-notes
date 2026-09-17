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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.opennotes.R
import com.opennotes.settings.presentation.SettingsViewModel
import java.io.File

@Composable
fun MarkdownCodeBlock(
    color: Color,
    text: @Composable (() -> Unit),
) {
    Box(
        modifier = Modifier.padding(top = 6.dp),
        content = {
            Surface(
                color = color,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .fillMaxWidth(),
                content = {
                    text()
                },
            )
        },
    )
}

@Composable
fun MarkdownQuote(
    content: String,
    fontSize: TextUnit,
    textColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .height(22.dp)
                    .width(6.dp)
                    .background(
                        Color(0xFF1565C0),
                        RoundedCornerShape(16.dp),
                    ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = content,
            fontSize = fontSize,
            color = textColor,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
fun MarkdownCheck(
    content: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    textColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier =
                Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
            colors =
                CheckboxDefaults.colors(
                    checkmarkColor = Color.White,
                    checkedColor = Color(0xFF1565C0),
                    uncheckedColor = textColor,
                ),
        )
        content()
    }
}

@Composable
fun MarkdownText(
    radius: Int,
    markdown: String,
    isPreview: Boolean = false,
    isReadOnly: Boolean = false,
    isEnabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    weight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = 16.sp,
    spacing: Dp = 2.dp,
    onContentChange: (String) -> Unit = {},
    settingsViewModel: SettingsViewModel? = null,
    textColor: Color = Color.Unspecified,
) {
    if (markdown.isBlank()) {
        StaticMarkdownText(
            markdown = if (markdown.isBlank()) "content" else markdown,
            modifier = modifier,
            weight = weight,
            fontSize = fontSize,
            textColor = textColor,
        )
        return
    }

    if (!isEnabled) {
        if (isReadOnly) {
            SelectionContainer {
                Text(
                    text = markdown,
                    fontSize = fontSize,
                    fontWeight = weight,
                    color = textColor,
                    modifier = modifier,
                )
            }
        } else {
            StaticMarkdownText(
                markdown = markdown,
                modifier = modifier,
                weight = weight,
                fontSize = fontSize,
                textColor = textColor,
            )
        }
        return
    }

    // Safety check - use simple text rendering for extremely large content only
    val isTooLarge =
        remember(markdown) {
            markdown.length > 20000
        }

    if (isTooLarge) {
        SelectionContainer {
            LazyColumn(modifier = modifier) {
                item {
                    Text(
                        text = markdown,
                        fontSize = fontSize,
                        fontWeight = weight,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        return
    }

    // Cache expensive markdown parsing
    val parsedContent =
        remember(markdown) {
            try {
                val lines = markdown.lines()
                val lineProcessors =
                    listOf(
                        HeadingProcessor(),
                        ListItemProcessor(),
                        CodeBlockProcessor(),
                        QuoteProcessor(),
                        ImageInsertionProcessor(),
                        CheckboxProcessor(),
                        LinkProcessor(),
                        HorizontalRuleProcessor(),
                    )
                val markdownBuilder = MarkdownBuilder(lines, lineProcessors)
                markdownBuilder.parse()
                markdownBuilder.content
            } catch (e: Exception) {
                listOf(NormalText(markdown))
            }
        }

    MarkdownContent(
        radius = radius,
        isPreview = isPreview,
        isReadOnly = isReadOnly,
        content = parsedContent,
        modifier = modifier,
        spacing = spacing,
        weight = weight,
        fontSize = fontSize,
        lines = markdown.lines(),
        onContentChange = onContentChange,
        textColor = textColor,
    )
}

@Composable
fun StaticMarkdownText(
    markdown: String,
    modifier: Modifier,
    weight: FontWeight,
    fontSize: TextUnit,
    textColor: Color,
) {
    Text(
        text = markdown,
        fontSize = fontSize,
        fontWeight = weight,
        color =
            if (markdown == "content") {
                Color.Gray
            } else {
                textColor
            },
        modifier = modifier,
    )
}

@Composable
fun MarkdownContent(
    radius: Int,
    isPreview: Boolean,
    isReadOnly: Boolean,
    content: List<MarkdownElement>,
    modifier: Modifier,
    spacing: Dp,
    weight: FontWeight,
    fontSize: TextUnit,
    lines: List<String>,
    onContentChange: (String) -> Unit,
    textColor: Color,
) {
    if (content.isEmpty()) {
        Text(
            text = stringResource(R.string.content_label),
            fontSize = fontSize,
            fontWeight = weight,
            color = Color.Gray,
            modifier = modifier,
        )
        return
    }

    if (isPreview) {
        Column(modifier = modifier) {
            content.take(6).forEachIndexed { index, _ ->
                RenderMarkdownElement(
                    radius = radius,
                    index = index,
                    content = content,
                    weight = weight,
                    fontSize = fontSize,
                    lines = lines,
                    isPreview = true,
                    isReadOnly = isReadOnly,
                    onContentChange = onContentChange,
                    textColor = textColor,
                )
                if (index < content.size - 1 && index < 5) {
                    Spacer(modifier = Modifier.height(spacing))
                }
            }
        }
    } else {
        SelectionContainer {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing),
            ) {
                items(content.size) { index ->
                    RenderMarkdownElement(
                        radius = radius,
                        content = content,
                        index = index,
                        weight = weight,
                        fontSize = fontSize,
                        lines = lines,
                        isPreview = isPreview,
                        isReadOnly = isReadOnly,
                        onContentChange = onContentChange,
                        textColor = textColor,
                    )
                }
            }
        }
    }
}

@Composable
fun RenderMarkdownElement(
    radius: Int,
    content: List<MarkdownElement>,
    index: Int,
    weight: FontWeight,
    fontSize: TextUnit,
    lines: List<String>,
    isPreview: Boolean,
    isReadOnly: Boolean,
    onContentChange: (String) -> Unit,
    textColor: Color,
) {
    val element = content[index]

    when (element) {
        is Heading -> {
            Text(
                text = buildAnnotatedMarkdownString(element.text, weight),
                fontSize =
                    when (element.level) {
                        1 -> (fontSize.value + 8).sp
                        2 -> (fontSize.value + 6).sp
                        3 -> (fontSize.value + 4).sp
                        4 -> (fontSize.value + 2).sp
                        5 -> (fontSize.value + 1).sp
                        6 -> fontSize
                        else -> fontSize
                    },
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }

        is CheckboxItem -> {
            MarkdownCheck(
                content = {
                    Text(
                        text = buildAnnotatedMarkdownString(element.text, weight),
                        fontSize = fontSize,
                        fontWeight = weight,
                        color = textColor,
                    )
                },
                checked = element.checked,
                textColor = textColor,
                onCheckedChange =
                    if (isPreview || isReadOnly) {
                        null
                    } else {
                        { newChecked ->
                            val newMarkdown =
                                lines.toMutableList().apply {
                                    this[element.index] =
                                        if (newChecked) {
                                            "[X] ${element.text}"
                                        } else {
                                            "[ ] ${element.text}"
                                        }
                                }
                            onContentChange(newMarkdown.joinToString("\n"))
                        }
                    },
            )
        }

        is ListItem -> {
            val prefix =
                if (element.isNumbered && element.number != null) {
                    "${element.number}. "
                } else {
                    "• "
                }
            Text(
                text = buildAnnotatedMarkdownString("$prefix${element.text}", weight),
                fontSize = fontSize,
                fontWeight = weight,
                color = textColor,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        is Quote -> {
            MarkdownQuote(content = element.text, fontSize = fontSize, textColor = textColor)
        }

        is CodeBlock -> {
            if (element.isEnded) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .fillMaxWidth(),
                ) {
                    Column {
                        element.language?.let { lang ->
                            Text(
                                text = lang.uppercase(),
                                fontSize = (fontSize.value - 2).sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                        Text(
                            text = element.code.trimEnd(),
                            fontSize = (fontSize.value - 1).sp,
                            fontWeight = weight,
                            fontFamily = FontFamily.Monospace,
                            color = textColor,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = buildAnnotatedMarkdownString(element.firstLine, weight),
                    fontWeight = weight,
                    fontSize = fontSize,
                    color = textColor,
                )
            }
        }

        is Link -> {
            val context = LocalContext.current
            val linkColor = Color(0xFF0D47A1) // Dark blue for links

            val annotatedString =
                remember(element.fullText, element.urlRanges) {
                    buildAnnotatedString {
                        try {
                            val fullText = element.fullText
                            var lastIndex = 0

                            val safeRanges =
                                element.urlRanges
                                    .take(5)
                                    .filter { (url, range) ->
                                        url.isNotEmpty() &&
                                            range.first >= 0 &&
                                            range.last < fullText.length &&
                                            range.first <= range.last
                                    }.sortedBy { it.second.first }

                            for ((url, range) in safeRanges) {
                                val safeStart = maxOf(0, minOf(range.first, fullText.length))
                                val safeEnd = maxOf(safeStart, minOf(range.last + 1, fullText.length))

                                // Add text before the link
                                if (safeStart > lastIndex && lastIndex < fullText.length) {
                                    append(buildAnnotatedMarkdownString(fullText.substring(lastIndex, safeStart), weight))
                                }

                                // Add the link itself
                                if (safeStart < safeEnd) {
                                    pushStringAnnotation("URL", url)
                                    withStyle(
                                        SpanStyle(
                                            color = linkColor,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                        ),
                                    ) {
                                        append(url)
                                    }
                                    pop()
                                }

                                lastIndex = safeEnd
                            }

                            // Add remaining text after all links
                            if (lastIndex < fullText.length) {
                                append(buildAnnotatedMarkdownString(fullText.substring(lastIndex), weight))
                            }
                        } catch (e: Exception) {
                            append(element.fullText)
                        }
                    }
                }

            ClickableText(
                text = annotatedString,
                onClick = { offset: Int ->
                    try {
                        annotatedString
                            .getStringAnnotations("URL", offset, offset)
                            .firstOrNull()
                            ?.let { annotation ->
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                intent.data = android.net.Uri.parse(annotation.item)
                                context.startActivity(intent)
                            }
                    } catch (e: Exception) {
                        // Handle error silently
                    }
                },
                style =
                    androidx.compose.ui.text.TextStyle(
                        fontSize = fontSize,
                    ),
            )
        }

        is HorizontalRule -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(1.dp)
                        .background(Color(0xFF666666)),
            )
        }

        is NormalText -> {
            Text(
                text = buildAnnotatedMarkdownString(element.text, weight),
                fontSize = fontSize,
                fontWeight = weight,
                color = textColor,
            )
        }

        is ImageInsertion -> {
            val context = LocalContext.current
            val imageFile = File(context.filesDir, element.photoUri)
            AsyncImage(
                model = imageFile,
                contentDescription = "Note Image",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )
        }

        else -> {
            // Fallback for any other types
            Text(text = element.toString())
        }
    }
}
