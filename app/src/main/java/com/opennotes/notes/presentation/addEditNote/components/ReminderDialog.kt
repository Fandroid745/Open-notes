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

package com.opennotes.notes.presentation.addEditNote.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.opennotes.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    reminderTime: Long?,
    onReminderSet: (Long?) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasNotificationPermission = isGranted
            if (!isGranted) {
                onDismiss()
            }
        }

    // Request permission immediately if not granted
    LaunchedEffect(hasNotificationPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
        // Wait for permission check
        return
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    var showM3ReminderDialog by remember { mutableStateOf(reminderTime == null) }

    if (!showM3ReminderDialog && reminderTime != null) {
        // Show Options Dialog
        val reminderString =
            remember(reminderTime) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(reminderTime))
            }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Note Reminder") },
            text = {
                Column {
                    Text(text = "Reminder set for:\n$reminderString", style = MaterialTheme.typography.bodyMedium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onReminderSet(null)
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.remove), color = contentColor)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showM3ReminderDialog = true
                    },
                ) {
                    Text(stringResource(R.string.change), color = contentColor)
                }
            },
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            textContentColor = contentColor.copy(alpha = 0.8f),
        )
    } else {
        // Show Date and Time selection dialog
        val initialCal =
            remember(reminderTime) {
                Calendar.getInstance().apply { timeInMillis = reminderTime ?: System.currentTimeMillis() }
            }

        var selectedDateMillis by remember { mutableStateOf(reminderTime ?: System.currentTimeMillis()) }
        var selectedHour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
        var selectedMinute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }
        var repeatOption by remember { mutableStateOf("Does not repeat") }

        var showSubDatePicker by remember { mutableStateOf(false) }
        var showSubTimePicker by remember { mutableStateOf(false) }
        var showRepeatMenu by remember { mutableStateOf(false) }

        val sdfDate = remember { SimpleDateFormat("MMMM d", Locale.getDefault()) }
        val dateText = remember(selectedDateMillis) { sdfDate.format(Date(selectedDateMillis)) }
        val timeText =
            remember(selectedHour, selectedMinute) {
                String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Pick a date & time",
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Date field
                    ReminderDropdownField(
                        value = dateText,
                        onClick = { showSubDatePicker = true },
                        contentColor = contentColor,
                    )

                    // Time field
                    ReminderDropdownField(
                        value = timeText,
                        onClick = { showSubTimePicker = true },
                        contentColor = contentColor,
                    )

                    // Repeat option
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ReminderDropdownField(
                            value = repeatOption,
                            onClick = { showRepeatMenu = true },
                            contentColor = contentColor,
                        )
                        DropdownMenu(
                            expanded = showRepeatMenu,
                            onDismissRequest = { showRepeatMenu = false },
                            modifier = Modifier.background(backgroundColor),
                        ) {
                            val options = listOf("Does not repeat", "Daily", "Weekly", "Monthly")
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = contentColor) },
                                    onClick = {
                                        repeatOption = option
                                        showRepeatMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar =
                            Calendar.getInstance().apply {
                                timeInMillis = selectedDateMillis
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMinute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        onReminderSet(calendar.timeInMillis)
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.save), color = contentColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(stringResource(R.string.cancel), color = contentColor)
                }
            },
            containerColor = backgroundColor,
            titleContentColor = contentColor,
        )

        // Nested Date Picker Dialog
        if (showSubDatePicker) {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis,
                )
            DatePickerDialog(
                onDismissRequest = { showSubDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                            showSubDatePicker = false
                        },
                    ) {
                        Text(stringResource(R.string.ok), color = contentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubDatePicker = false }) {
                        Text(stringResource(R.string.cancel), color = contentColor)
                    }
                },
                colors =
                    DatePickerDefaults.colors(
                        containerColor = backgroundColor,
                    ),
            ) {
                DatePicker(
                    state = datePickerState,
                    colors =
                        DatePickerDefaults.colors(
                            containerColor = backgroundColor,
                            titleContentColor = contentColor,
                            headlineContentColor = contentColor,
                        ),
                )
            }
        }

        // Nested Time Picker Dialog with clock/keyboard toggle
        if (showSubTimePicker) {
            val timePickerState =
                rememberTimePickerState(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    is24Hour = false,
                )
            var isClockMode by remember { mutableStateOf(false) }

            val timePickerColors =
                TimePickerDefaults.colors(
                    clockDialColor = contentColor.copy(alpha = 0.05f),
                    clockDialSelectedContentColor = backgroundColor,
                    clockDialUnselectedContentColor = contentColor,
                    selectorColor = contentColor,
                    periodSelectorBorderColor = contentColor,
                    periodSelectorSelectedContainerColor = contentColor.copy(alpha = 0.15f),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = contentColor,
                    periodSelectorUnselectedContentColor = contentColor,
                    timeSelectorSelectedContainerColor = contentColor.copy(alpha = 0.15f),
                    timeSelectorUnselectedContainerColor = contentColor.copy(alpha = 0.05f),
                    timeSelectorSelectedContentColor = contentColor,
                    timeSelectorUnselectedContentColor = contentColor,
                )

            AlertDialog(
                onDismissRequest = { showSubTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                            showSubTimePicker = false
                        },
                    ) {
                        Text(stringResource(R.string.ok), color = contentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubTimePicker = false }) {
                        Text(stringResource(R.string.cancel), color = contentColor)
                    }
                },
                title = { Text(stringResource(R.string.select_time)) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (isClockMode) {
                            TimePicker(
                                state = timePickerState,
                                colors = timePickerColors,
                            )
                        } else {
                            TimeInput(
                                state = timePickerState,
                                colors = timePickerColors,
                            )
                        }
                        // Clock / Keyboard toggle icon at the bottom left
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            IconButton(onClick = { isClockMode = !isClockMode }) {
                                Icon(
                                    imageVector =
                                        if (isClockMode) {
                                            Icons.Default.Edit
                                        } else {
                                            Icons.Default.AccessTime
                                        },
                                    contentDescription = if (isClockMode) "Switch to keyboard" else "Switch to clock",
                                    tint = contentColor,
                                )
                            }
                        }
                    }
                },
                containerColor = backgroundColor,
                titleContentColor = contentColor,
            )
        }
    }
}

@Composable
private fun ReminderDropdownField(
    value: String,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = contentColor,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(contentColor.copy(alpha = 0.2f)),
        )
    }
}
