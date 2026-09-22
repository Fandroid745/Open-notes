package com.opennotes.widget

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

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.opennotes.R
import com.opennotes.notes.presentation.addEditNote.components.markdown.*
import java.io.File

@Composable
fun WidgetText(
    modifier: GlanceModifier = GlanceModifier,
    markdown: String,
    weight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = 15.sp,
    color: ColorProvider,
    onContentChange: (String) -> Unit = {},
) {
    val lines = markdown.lines()
    val lineProcessors =
        listOf(
            HeadingProcessor(),
            ListItemProcessor(),
            CodeBlockProcessor(),
            QuoteProcessor(),
            ImageInsertionProcessor(),
            CheckboxProcessor(),
        )
    val markdownBuilder = MarkdownBuilder(lines, lineProcessors)
    markdownBuilder.parse()
    MarkdownWidgetContent(
        modifier = modifier,
        content = markdownBuilder.content.take(6), // Limit to 6 items to keep widget compact
        weight = weight,
        lines = lines,
        fontSize = fontSize,
        color = color,
        onContentChange = onContentChange,
    )
}

@Composable
fun MarkdownWidgetContent(
    modifier: GlanceModifier,
    content: List<MarkdownElement>,
    weight: FontWeight,
    lines: List<String>,
    color: ColorProvider,
    fontSize: TextUnit,
    onContentChange: (String) -> Unit,
) {
    val context = LocalContext.current
    if (content.isEmpty()) {
        Text(
            text = context.getString(R.string.widget_no_content),
            style = TextStyle(color = color, fontSize = fontSize),
        )
        return
    }

    LazyColumn(
        modifier = modifier,
    ) {
        items(content.size) { index ->
            WidgetMarkdownElement(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                index = index,
                color = color,
                content = content,
                weight = weight,
                fontSize = fontSize,
                lines = lines,
                onContentChange = onContentChange,
            )
        }
    }
}

@Composable
fun WidgetMarkdownElement(
    modifier: GlanceModifier,
    lines: List<String>,
    content: List<MarkdownElement>,
    index: Int,
    color: ColorProvider,
    weight: FontWeight,
    fontSize: TextUnit,
    onContentChange: (String) -> Unit,
) {
    val element = content[index]
    val context = LocalContext.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        when (element) {
            is Heading -> {
                Text(
                    text = element.text.stripMarkdown(),
                    style =
                        TextStyle(
                            fontSize =
                                when (element.level) {
                                    in 1..6 -> (24 - (2 * element.level)).sp
                                    else -> fontSize
                                },
                            fontWeight = FontWeight.Bold,
                            color = color,
                        ),
                )
            }
            is ImageInsertion -> {
                val file = File(context.filesDir, element.photoUri)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    val maxFileSize = 15552000
                    if (bitmap.byteCount < maxFileSize) {
                        Image(
                            provider = ImageProvider(bitmap),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier =
                                GlanceModifier
                                    .padding(6.dp)
                                    .wrapContentHeight(),
                        )
                    } else {
                        Text(
                            text = "Unsupported image size",
                            style =
                                TextStyle(
                                    fontSize = fontSize,
                                    fontWeight = weight,
                                    color = GlanceTheme.colors.error,
                                ),
                        )
                    }
                }
            }
            is NormalText -> {
                if (element.text.isNotBlank()) {
                    Text(
                        text = element.text.stripMarkdown(),
                        style =
                            TextStyle(
                                fontSize = fontSize,
                                fontWeight = weight,
                                color = color,
                            ),
                    )
                }
            }
            is CheckboxItem -> {
                MarkdownWidgetCheck(
                    content = {
                        Text(
                            text = element.text.stripMarkdown(),
                            style =
                                TextStyle(
                                    fontSize = fontSize,
                                    fontWeight = weight,
                                    color = color,
                                ),
                            maxLines = 1,
                            modifier = GlanceModifier.padding(start = 4.dp),
                        )
                    },
                    checked = element.checked,
                    onCheckedChange = null, // Read-only in widget for now
                )
            }
            is ListItem -> {
                val prefix = if (element.isNumbered && element.number != null) "${element.number}. " else "• "
                Text(
                    text = (prefix + element.text).stripMarkdown(),
                    style =
                        TextStyle(
                            fontSize = fontSize,
                            fontWeight = weight,
                            color = color,
                        ),
                )
            }
            is Quote -> {
                Row(
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Box(
                        modifier =
                            GlanceModifier
                                .height(22.dp)
                                .width(4.dp)
                                .background(color),
                    ) {}
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = element.text.stripMarkdown(),
                        style =
                            TextStyle(
                                fontSize = fontSize,
                                fontWeight = weight,
                                color = color,
                            ),
                    )
                }
            }
            is CodeBlock -> {
                if (element.isEnded) {
                    MarkdownWidgetCodeBlock(color = GlanceTheme.colors.surfaceVariant) {
                        Text(
                            text = element.code.dropLast(1),
                            style =
                                TextStyle(
                                    fontSize = fontSize,
                                    fontWeight = weight,
                                    fontFamily = FontFamily.Monospace,
                                    color = color,
                                ),
                        )
                    }
                } else {
                    Text(
                        text = element.firstLine,
                        style =
                            TextStyle(
                                fontSize = fontSize,
                                fontWeight = weight,
                                color = color,
                            ),
                    )
                }
            }
            is Link -> {
                Text(
                    text = element.fullText.stripMarkdown(),
                    style =
                        TextStyle(
                            fontSize = fontSize,
                            fontWeight = weight,
                            color = color,
                        ),
                )
            }
            is HorizontalRule -> {
                Box(
                    modifier =
                        GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color),
                ) {}
            }
        }
    }
}

@Composable
fun MarkdownWidgetCheck(
    content: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        CheckBox(
            checked = checked,
            onCheckedChange = null, // Ensure read-only UI behavior in glance checkbox
            modifier = GlanceModifier.padding(end = 4.dp),
        )
        content()
    }
}

@Composable
fun MarkdownWidgetCodeBlock(
    color: ColorProvider,
    text: @Composable () -> Unit,
) {
    Box(
        modifier = GlanceModifier.padding(top = 6.dp),
    ) {
        Box(
            modifier =
                GlanceModifier
                    .padding(6.dp)
                    .cornerRadius(6.dp)
                    .background(color)
                    .fillMaxWidth(),
        ) {
            text()
        }
    }
}
