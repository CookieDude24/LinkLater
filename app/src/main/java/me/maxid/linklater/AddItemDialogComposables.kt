package me.maxid.linklater
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import java.util.*

object AddItemDialogComposables {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddItemDialog(
        onDismiss: (String?, Calendar?) -> Unit = { _, _ -> },
        modifier: Modifier = Modifier,
        sharedText: String = "",
    ) {
        val textFieldState = rememberTextFieldState(sharedText) // if no text is shared the input is still empty
        var showTimePickerSate: Boolean by remember { mutableStateOf(false) }
        var selectedTime: Calendar by remember {
            mutableStateOf(
                Calendar.getInstance(TimeZone.getDefault()).apply { add(Calendar.HOUR, 1) })
        }
        BasicAlertDialog(
            onDismissRequest = { onDismiss(null, null) },

            ) {
            Surface(
                modifier = modifier.wrapContentSize(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Box {
                    Column(modifier = modifier.padding(16.dp)) {
                        Text(
                            text = "Add new reminder",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = modifier
                                .padding(top = 24.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        AddItemDialogTextField(
                            state = textFieldState, modifier = modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { showTimePickerSate = true },
                            modifier = modifier
                                .align(Alignment.Start)
                                .padding(top = 16.dp, bottom = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Select time (Default: ${timeFormatter(selectedTime)})"
                            )
                        }
                        Button(
                            enabled = textFieldState.text.isNotEmpty(),
                            onClick = { onDismiss(textFieldState.text.toString(), selectedTime) },
                            modifier = modifier.align(Alignment.End)
                        ) {
                            Text("Confirm")
                        }
                    }
                    if (showTimePickerSate) {
                        TimepickerComposables.TimePickerDialog(
                            onCancel = { showTimePickerSate = false },
                            onConfirm = { time ->
                                selectedTime = time
                                showTimePickerSate = false
                            },
                            modifier = modifier,
                            time = selectedTime,
                        )
                    }
                }
            }
        }
    }




    @Composable
    fun AddItemDialogTextField(
        modifier: Modifier = Modifier, state: TextFieldState = rememberTextFieldState()
    ) {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        OutlinedTextField(
            state = state,
            isError = state.text.isEmpty(),
            lineLimits = TextFieldLineLimits.SingleLine,
            label = {
                Text("Reminder name")
            },
            placeholder = { Text("Enter reminder name") },
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            trailingIcon = {
                if (state.text.isEmpty()) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Reminder name required")
                }
            },
            supportingText = {
                if (state.text.isEmpty()) {
                    Text(
                        text = "required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                } else {
                    Text(
                        text = "Required",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            },
        )
    }

}