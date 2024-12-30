@file:OptIn(ExperimentalMaterial3Api::class)

package me.maxid.linklater

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.maxid.linklater.ui.theme.LinkLaterTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var listDataStore: ListDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        listDataStore = ListDataStore(this)

        setContent {
            LinkLaterTheme {
                MainActivityScreen(
                    listDataStore = listDataStore,
                    onAddItem = { newItem, scheduledTime ->
                        lifecycleScope.launch {
                            listDataStore.appendToList(
                                newItem,
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(scheduledTime.time)
                                    ?: "Unknown time"
                            )
                        }
                        NotificationUtils.scheduleNotification(
                            context = this@MainActivity,
                            delayInSeconds = scheduledTime.timeInMillis / 1000 - System.currentTimeMillis() / 1000,
                            title = "Reminder",
                            message = newItem,
                        )

                    }
                )
            }
        }
    }
}

@Composable
fun MainActivityScreen(
    listDataStore: ListDataStore,
    onAddItem: (String, Calendar) -> Unit,
) {
    // State for managing dialog visibility
    val isDialogOpen = remember { mutableStateOf(false) }
    val savedList = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // Collecting ListDataStore data
    LaunchedEffect(Unit) {
        listDataStore.getList().collect { list ->
            savedList.value = list
        }
    }

    // Request permissions for notifications (for devices >= Android Tiramisu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestNotificationPermission(context = LocalContext.current as ComponentActivity)
    }

    // UI Scaffold
    Scaffold(
        topBar = {
            CommonTopBar(title = stringResource(R.string.app_name))
        },
        floatingActionButton = {
            CommonFloatingButtons(
                onAddClick = { isDialogOpen.value = true },
            )
        }
    ) { padding ->
        ReminderList(
            items = savedList.value,
            modifier = Modifier.padding(padding)
        )
    }

    // Conditionally show the AddItemDialog
    if (isDialogOpen.value) {
        AddItemDialog(
            onDismiss = { enteredText, enteredTime ->
                if (!enteredText.isNullOrEmpty()) {
                    if (enteredTime != null) {
                        onAddItem(enteredText, enteredTime)
                    }
                }
                isDialogOpen.value = false
            },
            modifier = Modifier.padding(4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
fun CommonFloatingButtons(
    onAddClick: () -> Unit,
) {
    Column {
        ExtendedFloatingActionButton(
            onClick = onAddClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Item") },
            text = { Text("Add Reminder") },
            modifier = Modifier.padding(end = 16.dp, bottom = 64.dp).scale(1.2f)
        )
    }
}

@Composable
fun ReminderList(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    if (items.size == 0) {
        Text(
            text = "No items added yet",
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize()
                .fillMaxWidth()
                .wrapContentHeight(Alignment.CenterVertically)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items.size) { i ->
                ReminderListItem(
                    itemName = items[i].first,
                    itemNumber = i + 1,
                    itemReminderTime = items[i].second,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ReminderListItem(
    modifier: Modifier = Modifier,
    itemNumber: Int = 0,
    itemName: String = "Item name",
    itemReminderTime: String = Calendar.getInstance(TimeZone.getDefault()).toString(),
) {
    Column {
        ListItem(
            headlineContent = { Text(itemName) },
            supportingContent = {
                Text(
                    "Reminder scheduled for ${
                        itemReminderTime
                    }"
                )
            },
            trailingContent = { Text("Reminder #$itemNumber") },
        )
        HorizontalDivider()
    }
}

@Composable
fun AddItemDialog(
    onDismiss: (String?, Calendar?) -> Unit,
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
                        state = textFieldState,
                        modifier = modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { showTimePickerSate = true },
                        modifier = modifier
                            .align(Alignment.Start)
                            .padding(top = 16.dp, bottom = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Select time (Default: ${
                                SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault()
                                ).format(selectedTime.time)
                            })"
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
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
fun AddItemDialogTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState()
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
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
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

fun requestNotificationPermission(context: ComponentActivity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        1001
    )
}

