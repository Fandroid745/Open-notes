package com.opennotes.notes.presentation.addEditNote.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.opennotes.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    reminderTime: Long?,
    repeatInterval: Long?,
    repeatUnit: String?,
    onReminderSet: (Long?, Long?, String?) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
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

    LaunchedEffect(hasNotificationPermission) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        !hasNotificationPermission
    ) {
        return
    }

    var showM3ReminderDialog by remember {
        mutableStateOf(reminderTime == null)
    }

    if (!showM3ReminderDialog && reminderTime != null) {
        val reminderString =
            remember(reminderTime) {
                val sdf =
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault(),
                    )
                sdf.format(Date(reminderTime))
            }

        val repeatText =
            when {
                repeatInterval == null || repeatUnit == null ->
                    stringResource(R.string.repeat_does_not_repeat)

                repeatInterval == 1L && repeatUnit == "DAYS" ->
                    stringResource(R.string.repeat_daily)

                repeatInterval == 1L && repeatUnit == "WEEKS" ->
                    stringResource(R.string.repeat_weekly)

                repeatInterval == 1L && repeatUnit == "MONTHS" ->
                    stringResource(R.string.repeat_monthly)

                else ->
                    "${stringResource(R.string.every)} " +
                        "$repeatInterval ${repeatUnit.lowercase()}"
            }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Note Reminder",
                    color = contentColor,
                )
            },
            text = {
                Column {
                    Text(
                        text = "Reminder set for:\n$reminderString",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Repeat: $repeatText",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.6f),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onReminderSet(null, null, null)
                        onDismiss()
                    },
                ) {
                    Text(
                        stringResource(R.string.remove),
                        color = contentColor,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showM3ReminderDialog = true
                    },
                ) {
                    Text(
                        stringResource(R.string.change),
                        color = contentColor,
                    )
                }
            },
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            textContentColor = contentColor.copy(alpha = 0.8f),
        )
    } else {
        val initialCal =
            remember(reminderTime) {
                Calendar.getInstance().apply {
                    timeInMillis =
                        reminderTime ?: System.currentTimeMillis()
                }
            }

        var selectedDateMillis by remember {
            mutableStateOf(
                reminderTime ?: System.currentTimeMillis(),
            )
        }

        var selectedHour by remember {
            mutableIntStateOf(
                initialCal.get(Calendar.HOUR_OF_DAY),
            )
        }

        var selectedMinute by remember {
            mutableIntStateOf(
                initialCal.get(Calendar.MINUTE),
            )
        }

        var currentRepeatInterval by remember {
            mutableStateOf(repeatInterval)
        }

        var currentRepeatUnit by remember {
            mutableStateOf(repeatUnit)
        }

        var showSubDatePicker by remember {
            mutableStateOf(false)
        }

        var showSubTimePicker by remember {
            mutableStateOf(false)
        }

        var showRepeatMenu by remember {
            mutableStateOf(false)
        }

        var showCustomRepeatDialog by remember {
            mutableStateOf(false)
        }

        val sdfDate =
            remember {
                SimpleDateFormat(
                    "MMMM d",
                    Locale.getDefault(),
                )
            }

        val dateText =
            remember(selectedDateMillis) {
                sdfDate.format(Date(selectedDateMillis))
            }

        val timeText =
            remember(selectedHour, selectedMinute) {
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    selectedHour,
                    selectedMinute,
                )
            }

        // Only one declaration of repeatOptionText.
        val localRepeatUnit = currentRepeatUnit

        val repeatOptionText =
            when {
                currentRepeatInterval == null ||
                    localRepeatUnit == null ->
                    stringResource(R.string.repeat_does_not_repeat)

                currentRepeatInterval == 1L &&
                    localRepeatUnit == "DAYS" ->
                    stringResource(R.string.repeat_daily)

                currentRepeatInterval == 1L &&
                    localRepeatUnit == "WEEKS" ->
                    stringResource(R.string.repeat_weekly)

                currentRepeatInterval == 1L &&
                    localRepeatUnit == "MONTHS" ->
                    stringResource(R.string.repeat_monthly)

                else ->
                    "${stringResource(R.string.every)} " +
                        "$currentRepeatInterval ${localRepeatUnit.lowercase()}"
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
                    ReminderDropdownField(
                        value = dateText,
                        onClick = {
                            showSubDatePicker = true
                        },
                        contentColor = contentColor,
                    )

                    ReminderDropdownField(
                        value = timeText,
                        onClick = {
                            showSubTimePicker = true
                        },
                        contentColor = contentColor,
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ReminderDropdownField(
                            value = repeatOptionText,
                            onClick = {
                                showRepeatMenu = true
                            },
                            contentColor = contentColor,
                        )

                        DropdownMenu(
                            expanded = showRepeatMenu,
                            onDismissRequest = {
                                showRepeatMenu = false
                            },
                            modifier =
                                Modifier.background(
                                    backgroundColor,
                                ),
                        ) {
                            val options =
                                listOf(
                                    (
                                        stringResource(
                                            R.string.repeat_does_not_repeat,
                                        ) to null as Long?
                                    ) to null as String?,
                                    (
                                        stringResource(
                                            R.string.repeat_daily,
                                        ) to 1L
                                    ) to "DAYS",
                                    (
                                        stringResource(
                                            R.string.repeat_weekly,
                                        ) to 1L
                                    ) to "WEEKS",
                                    (
                                        stringResource(
                                            R.string.repeat_monthly,
                                        ) to 1L
                                    ) to "MONTHS",
                                    (
                                        stringResource(
                                            R.string.repeat_custom,
                                        ) to -1L
                                    ) to "CUSTOM",
                                )

                            options.forEach { (labelInfo, unit) ->
                                val (label, interval) = labelInfo

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = contentColor,
                                        )
                                    },
                                    onClick = {
                                        if (interval == -1L) {
                                            showCustomRepeatDialog = true
                                        } else {
                                            currentRepeatInterval = interval
                                            currentRepeatUnit = unit
                                        }

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

                                set(
                                    Calendar.HOUR_OF_DAY,
                                    selectedHour,
                                )

                                set(
                                    Calendar.MINUTE,
                                    selectedMinute,
                                )

                                set(
                                    Calendar.SECOND,
                                    0,
                                )

                                set(
                                    Calendar.MILLISECOND,
                                    0,
                                )
                            }

                        onReminderSet(
                            calendar.timeInMillis,
                            currentRepeatInterval,
                            currentRepeatUnit,
                        )

                        onDismiss()
                    },
                ) {
                    Text(
                        stringResource(R.string.save),
                        color = contentColor,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        color = contentColor,
                    )
                }
            },
            containerColor = backgroundColor,
            titleContentColor = contentColor,
        )

        if (showCustomRepeatDialog) {
            CustomRepeatDialog(
                initialInterval = currentRepeatInterval ?: 1L,
                initialUnit = currentRepeatUnit ?: "DAYS",
                onConfirm = { interval, unit ->
                    currentRepeatInterval = interval
                    currentRepeatUnit = unit
                    showCustomRepeatDialog = false
                },
                onDismiss = {
                    showCustomRepeatDialog = false
                },
                backgroundColor = backgroundColor,
                contentColor = contentColor,
            )
        }

        if (showSubDatePicker) {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis,
                )

            DatePickerDialog(
                onDismissRequest = {
                    showSubDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { utcMillis ->
                                // DatePicker returns UTC midnight;
                                // extract Y/M/D and reapply in local TZ.
                                val utcCal =
                                    Calendar
                                        .getInstance(
                                            TimeZone.getTimeZone("UTC"),
                                        ).apply {
                                            timeInMillis = utcMillis
                                        }

                                val localCal =
                                    Calendar.getInstance().apply {
                                        set(
                                            utcCal.get(Calendar.YEAR),
                                            utcCal.get(Calendar.MONTH),
                                            utcCal.get(Calendar.DAY_OF_MONTH),
                                            initialCal.get(Calendar.HOUR_OF_DAY),
                                            initialCal.get(Calendar.MINUTE),
                                        )
                                    }

                                selectedDateMillis =
                                    localCal.timeInMillis
                            }

                            showSubDatePicker = false
                        },
                    ) {
                        Text(
                            stringResource(R.string.ok),
                            color = contentColor,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSubDatePicker = false
                        },
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = contentColor,
                        )
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

        if (showSubTimePicker) {
            // Keep only one timePickerState declaration.
            val timePickerState =
                rememberTimePickerState(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    is24Hour = true,
                )

            var isClockMode by remember {
                mutableStateOf(false)
            }

            val timePickerColors =
                TimePickerDefaults.colors(
                    clockDialColor =
                        contentColor.copy(alpha = 0.05f),
                    clockDialSelectedContentColor =
                    backgroundColor,
                    clockDialUnselectedContentColor =
                    contentColor,
                    selectorColor = contentColor,
                    periodSelectorBorderColor = contentColor,
                    periodSelectorSelectedContainerColor =
                        contentColor.copy(alpha = 0.15f),
                    periodSelectorUnselectedContainerColor =
                        Color.Transparent,
                    periodSelectorSelectedContentColor =
                    contentColor,
                    periodSelectorUnselectedContentColor =
                    contentColor,
                    timeSelectorSelectedContainerColor =
                        contentColor.copy(alpha = 0.15f),
                    timeSelectorUnselectedContainerColor =
                        contentColor.copy(alpha = 0.05f),
                    timeSelectorSelectedContentColor =
                    contentColor,
                    timeSelectorUnselectedContentColor =
                    contentColor,
                )

            AlertDialog(
                onDismissRequest = {
                    showSubTimePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                            showSubTimePicker = false
                        },
                    ) {
                        Text(
                            stringResource(R.string.ok),
                            color = contentColor,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSubTimePicker = false
                        },
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = contentColor,
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.select_time),
                        color = contentColor,
                    )
                },
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            IconButton(
                                onClick = {
                                    isClockMode = !isClockMode
                                },
                            ) {
                                Icon(
                                    imageVector =
                                        if (isClockMode) {
                                            Icons.Default.Edit
                                        } else {
                                            Icons.Default.AccessTime
                                        },
                                    contentDescription =
                                        if (isClockMode) {
                                            "Switch to keyboard"
                                        } else {
                                            "Switch to clock"
                                        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRepeatDialog(
    initialInterval: Long,
    initialUnit: String,
    onConfirm: (Long, String) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
) {
    var intervalText by remember {
        mutableStateOf(
            if (initialInterval <= 0) {
                "1"
            } else {
                initialInterval.toString()
            },
        )
    }

    var selectedUnit by remember {
        mutableStateOf(
            if (initialUnit == "CUSTOM") {
                "DAYS"
            } else {
                initialUnit
            },
        )
    }

    var showUnitMenu by remember {
        mutableStateOf(false)
    }

    val units =
        listOf(
            "MINUTES",
            "HOURS",
            "DAYS",
            "WEEKS",
            "MONTHS",
            "YEARS",
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Custom Repeat",
                color = contentColor,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            intervalText = it
                        }
                    },
                    label = {
                        Text("Interval")
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor = contentColor,
                            unfocusedTextColor = contentColor,
                            focusedLabelColor = contentColor,
                            unfocusedLabelColor =
                                contentColor.copy(alpha = 0.7f),
                        ),
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val unitLabel =
                        when (selectedUnit) {
                            "MINUTES" ->
                                stringResource(R.string.unit_minutes)

                            "HOURS" ->
                                stringResource(R.string.unit_hours)

                            "DAYS" ->
                                stringResource(R.string.unit_days)

                            "WEEKS" ->
                                stringResource(R.string.unit_weeks)

                            "MONTHS" ->
                                stringResource(R.string.unit_months)

                            "YEARS" ->
                                stringResource(R.string.unit_years)

                            else -> selectedUnit
                        }

                    ReminderDropdownField(
                        value = unitLabel,
                        onClick = {
                            showUnitMenu = true
                        },
                        contentColor = contentColor,
                    )

                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = {
                            showUnitMenu = false
                        },
                        modifier =
                            Modifier.background(
                                backgroundColor,
                            ),
                    ) {
                        units.forEach { unit ->
                            val label =
                                when (unit) {
                                    "MINUTES" ->
                                        stringResource(
                                            R.string.unit_minutes,
                                        )

                                    "HOURS" ->
                                        stringResource(
                                            R.string.unit_hours,
                                        )

                                    "DAYS" ->
                                        stringResource(
                                            R.string.unit_days,
                                        )

                                    "WEEKS" ->
                                        stringResource(
                                            R.string.unit_weeks,
                                        )

                                    "MONTHS" ->
                                        stringResource(
                                            R.string.unit_months,
                                        )

                                    "YEARS" ->
                                        stringResource(
                                            R.string.unit_years,
                                        )

                                    else -> unit
                                }

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        label,
                                        color = contentColor,
                                    )
                                },
                                onClick = {
                                    selectedUnit = unit
                                    showUnitMenu = false
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
                    val interval =
                        intervalText.toLongOrNull() ?: 1L

                    onConfirm(
                        if (interval <= 0) 1L else interval,
                        selectedUnit,
                    )
                },
            ) {
                Text(
                    stringResource(R.string.ok),
                    color = contentColor,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    stringResource(R.string.cancel),
                    color = contentColor,
                )
            }
        },
        containerColor = backgroundColor,
    )
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
                contentDescription = null,
                tint = contentColor,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        contentColor.copy(alpha = 0.2f),
                    ),
        )
    }
}
