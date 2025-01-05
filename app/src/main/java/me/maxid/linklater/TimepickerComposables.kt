package me.maxid.linklater

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.util.*

object TimepickerComposables {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TimePickerDialog(
        onCancel: () -> Unit,
        onConfirm: (Calendar) -> Unit,
        modifier: Modifier = Modifier,
        time: Calendar = Calendar.getInstance()
    ) {
        time.timeInMillis = System.currentTimeMillis()

        var mode: DisplayMode by remember { mutableStateOf(DisplayMode.Picker) }
        val timeState: TimePickerState = rememberTimePickerState(
            initialHour = time[Calendar.HOUR_OF_DAY],
            initialMinute = time[Calendar.MINUTE],
        )

        fun onConfirmClicked() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
            cal.set(Calendar.MINUTE, timeState.minute)
            if (timeState.hour == time[Calendar.HOUR_OF_DAY] && timeState.minute <= time[Calendar.MINUTE] ||
                timeState.hour < time[Calendar.HOUR_OF_DAY]
            ) {
                cal.add(Calendar.DAY_OF_WEEK, 1)
            }
            cal.isLenient = false

            onConfirm(cal)
        }

        // TimePicker does not provide a default TimePickerDialog, so we use our own PickerDialog:
        // https://issuetracker.google.com/issues/288311426
        PickerDialog(
            modifier = modifier,
            onDismissRequest = onCancel,
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.timepicker_dialog_title),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (timeState.hour == time[Calendar.HOUR_OF_DAY] && timeState.minute <= time[Calendar.MINUTE] ||
                        timeState.hour < time[Calendar.HOUR_OF_DAY]
                    ) {
                        Surface(
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shadowElevation = 5.dp,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                stringResource(R.string.timepicker_dialog_reminder_going_off_tomorrow),
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

            },
            buttons = {
                DisplayModeToggleButton(
                    displayMode = mode,
                    onDisplayModeChange = { mode = it },
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel_textbutton))
                }
                Button(onClick = ::onConfirmClicked) {
                    Text(stringResource(R.string.confirm_textbutton))
                }
            },
        ) {
            val contentModifier = Modifier.padding(horizontal = 24.dp)
            when (mode) {
                DisplayMode.Picker -> TimePicker(modifier = contentModifier, state = timeState)
                DisplayMode.Input -> TimeInput(modifier = contentModifier, state = timeState)
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DisplayModeToggleButton(
        displayMode: DisplayMode,
        onDisplayModeChange: (DisplayMode) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        when (displayMode) {
            DisplayMode.Picker -> IconButton(
                modifier = modifier,
                onClick = { onDisplayModeChange(DisplayMode.Input) },
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = stringResource(R.string.timepicker_dialog_displaymode_toggle_content_description))
            }

            DisplayMode.Input -> IconButton(
                modifier = modifier,
                onClick = { onDisplayModeChange(DisplayMode.Picker) },
            ) {
                Icon(Icons.Default.Schedule, contentDescription = stringResource(R.string.timepicker_dialog_displaymode_toggle_content_description))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PickerDialog(
        onDismissRequest: () -> Unit,
        title: @Composable () -> Unit,
        buttons: @Composable RowScope.() -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        AlertDialog(
            modifier = modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Title
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 16.dp, bottom = 20.dp),
                            ) {
                                title()
                            }
                        }
                    }
                    // Content
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.textContentColor) {
                        content()
                    }
                    // Buttons
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                            // TODO This should wrap on small screens, but we can't use AlertDialogFlowRow as it is no public
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, end = 6.dp, start = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            ) {
                                buttons()
                            }
                        }
                    }
                }
            }
        }
    }
}