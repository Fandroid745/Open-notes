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

package com.opennotes.notes.presentation.addEditNote

import com.opennotes.R
import androidx.compose.ui.res.stringResource
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.opennotes.notes.presentation.addEditNote.components.FormatToolbar
import com.opennotes.notes.presentation.addEditNote.components.ReminderDialog
import com.opennotes.notes.presentation.addEditNote.components.markdown.MarkdownField
import com.opennotes.notes.presentation.addEditNote.components.markdown.MarkdownFormatter
import com.opennotes.notes.presentation.util.formatToDateTime
import com.opennotes.settings.presentation.CustomColorDialog
import com.opennotes.ui.theme.NoteColorPalette
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditNoteScreen(
    navController: NavController,
    noteColor: Int?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    viewModel: AddEditNoteViewModel = hiltViewModel(),
) {
    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isPreviewMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    var contentTextFieldValue by remember {
        mutableStateOf(
            androidx.compose.ui.text.input
                .TextFieldValue(text = contentState.text),
        )
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                uri?.let {
                    viewModel.onEvent(AddEditNoteEvent.InsertImage(it.toString()))
                }
            },
        )

    val resolvedColorInt =
        remember(noteColor, viewModel.noteColor.value) {
            noteColor ?: viewModel.noteColor.value
        }

    val noteBackgroundAnimatable = remember { Animatable(Color(resolvedColorInt)) }

    val backgroundColor = noteBackgroundAnimatable.value

    val contentColor =
        if (backgroundColor.luminance() < 0.5f) {
            Color.White
        } else {
            Color.Black
        }

    val noteColors =
        if (isDarkTheme) {
            NoteColorPalette.Dark
        } else {
            NoteColorPalette.Light
        }

    val contentFocusRequester = remember { FocusRequester() }
    val titleFocusRequester = remember { FocusRequester() }

    val interactionSource = remember { MutableInteractionSource() }

    val scope = rememberCoroutineScope()

    var showColorPicker by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var showFormatToolbar by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        LaunchedEffect(contentState.text) {
            if (contentState.text != contentTextFieldValue.text) {
                contentTextFieldValue = contentTextFieldValue.copy(text = contentState.text)
            }
        }

        LaunchedEffect(resolvedColorInt) {
            noteBackgroundAnimatable.animateTo(Color(resolvedColorInt))
        }

        LaunchedEffect(isDarkTheme) {
            viewModel.applyDefaultColor(isDarkTheme)
        }

        LaunchedEffect(key1 = true) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is AddEditNoteViewModel.UiEvent.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(message = event.message)
                    }

                    is AddEditNoteViewModel.UiEvent.SavedNote -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigateUp()
                    }
                }
            }
        }

        BackHandler {
            viewModel.onEvent(AddEditNoteEvent.SaveNote)
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onEvent(AddEditNoteEvent.SaveNote) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back",
                                tint = contentColor,
                            )
                        }
                    },
                    actions = {
                        FilledIconButton(
                            onClick = { isPreviewMode = !isPreviewMode },
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = contentColor.copy(alpha = 0.15f),
                                    contentColor = contentColor,
                                ),
                        ) {
                            Icon(
                                imageVector = if (isPreviewMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPreviewMode) "Edit mode" else "Preview mode",
                                tint = contentColor,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = { showReminderDialog = true },
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (viewModel.noteReminderTime.value != null) contentColor.copy(alpha = 0.3f) else contentColor.copy(alpha = 0.15f),
                                    contentColor = contentColor,
                                ),
                        ) {
                            Icon(
                                imageVector = if (viewModel.noteReminderTime.value != null) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Set reminder",
                                tint = contentColor,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = backgroundColor,
                        ),
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier.background(backgroundColor),
                ) {
                    if (showFormatToolbar) {
                        FormatToolbar(
                            contentColor = contentColor,
                            onFormatClick = { format ->
                                contentTextFieldValue = MarkdownFormatter.injectMarkdown(format, contentTextFieldValue)
                                viewModel.onEvent(AddEditNoteEvent.EnteredContent(contentTextFieldValue.text))
                            },
                        )
                    }
                    BottomAppBar(
                        containerColor = backgroundColor,
                        contentColor = contentColor,
                    ) {
                        FilledIconButton(
                            onClick = { showColorPicker = true },
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = contentColor.copy(alpha = 0.15f),
                                    contentColor = contentColor,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Change color",
                                tint = contentColor,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        FilledIconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = contentColor.copy(alpha = 0.15f),
                                    contentColor = contentColor,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Add image",
                                tint = contentColor,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        FilledIconButton(
                            onClick = { showFormatToolbar = !showFormatToolbar },
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (showFormatToolbar) contentColor.copy(alpha = 0.3f) else contentColor.copy(alpha = 0.15f),
                                    contentColor = contentColor,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = "Format text",
                                tint = contentColor,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box {
                            FilledIconButton(
                                onClick = { showMenu = true },
                                colors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = contentColor.copy(alpha = 0.15f),
                                        contentColor = contentColor,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = contentColor,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.note_info)) },
                                    onClick = {
                                        showMenu = false
                                        showInfoDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share_note)) },
                                    onClick = {
                                        showMenu = false
                                        val sendIntent =
                                            Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TITLE, titleState.text)
                                                putExtra(Intent.EXTRA_TEXT, "${titleState.text}\n\n${contentState.text}")
                                                type = "text/plain"
                                            }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share note via")
                                        context.startActivity(shareIntent)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            },
        ) { paddingValues ->
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextSelectionColors provides
                    TextSelectionColors(
                        handleColor = contentColor,
                        backgroundColor = contentColor.copy(alpha = 0.4f),
                    ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (!isPreviewMode) {
                                    contentFocusRequester.requestFocus()
                                }
                            }.padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                            .imePadding()
                            .padding(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    MarkdownField(
                        titleText = titleState.text,
                        contentTextFieldValue = contentTextFieldValue,
                        contentColor = contentColor,
                        isPreviewMode = isPreviewMode,
                        interactionSource = interactionSource,
                        contentFocusRequester = contentFocusRequester,
                        titleFocusRequester = titleFocusRequester,
                        onTitleChange = { viewModel.onEvent(AddEditNoteEvent.EnteredTitle(it)) },
                        onTitleFocusChange = { viewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it)) },
                        onContentChange = {
                            contentTextFieldValue = it
                            viewModel.onEvent(AddEditNoteEvent.EnteredContent(it.text))
                        },
                        onContentFocusChange = {
                            viewModel.onEvent(
                                AddEditNoteEvent.ChangeContentFocus(
                                    it,
                                ),
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                    )
                }
            }
        }

        if (showColorPicker) {
            ModalBottomSheet(
                onDismissRequest = { showColorPicker = false },
                containerColor = backgroundColor,
            ) {
                Text(
                    text = "Color",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                LazyRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(contentColor.copy(alpha = 0.1f))
                                .clickable {
                                    showColorPicker = false
                                    showCustomColorDialog = true
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.custom_color),
                                tint = contentColor
                            )
                        }
                    }

                    items(noteColors) { color ->
                        val colorInt = remember(color) { color.toArgb() }
                        val isSelected = viewModel.noteColor.value == colorInt
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1f,
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            label = "scale",
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }.shadow(if (isSelected) 8.dp else 4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) contentColor else Color.Transparent,
                                        shape = CircleShape,
                                    ).clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            noteBackgroundAnimatable.animateTo(
                                                targetValue = Color(colorInt),
                                                animationSpec = tween(durationMillis = 500),
                                            )
                                        }
                                        viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                                    },
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = contentColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showInfoDialog) {
            val timestamp = viewModel.noteTimestamp.value
            val dateString =
                remember(timestamp) {
                    timestamp?.formatToDateTime() ?: "Not saved yet"
                }
            val wordCount =
                remember(contentState.text) {
                    contentState.text
                        .split("\\s+".toRegex())
                        .filter { it.isNotBlank() }
                        .size
                }
            val charCount =
                remember(contentState.text) {
                    contentState.text.length
                }
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text(text = stringResource(R.string.note_info)) },
                text = {
                    Column {
                        Text(text = "Created: $dateString", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Words: $wordCount", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Characters: $charCount", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text(stringResource(R.string.ok), color = contentColor)
                    }
                },
                containerColor = backgroundColor,
                titleContentColor = contentColor,
                textContentColor = contentColor.copy(alpha = 0.8f),
            )
        }
        if (showReminderDialog) {
            ReminderDialog(
                reminderTime = viewModel.noteReminderTime.value,
                onReminderSet = { time -> viewModel.onEvent(AddEditNoteEvent.SetReminder(time)) },
                onDismiss = { showReminderDialog = false },
                backgroundColor = backgroundColor,
                contentColor = contentColor,
            )
        }

        if (showCustomColorDialog) {
            CustomColorDialog(
                initialColor = backgroundColor,
                onConfirm = { color ->
                    val colorInt = color.toArgb()
                    scope.launch {
                        noteBackgroundAnimatable.animateTo(
                            targetValue = Color(colorInt),
                            animationSpec = tween(durationMillis = 500),
                        )
                    }
                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                    showCustomColorDialog = false
                    showColorPicker = false
                },
                onDismiss = { showCustomColorDialog = false }
            )
        }
    }
}
