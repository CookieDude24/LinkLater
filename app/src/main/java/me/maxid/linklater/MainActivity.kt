@file:OptIn(ExperimentalMaterial3Api::class)

package me.maxid.linklater

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.maxid.linklater.ui.theme.LinkLaterTheme
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                    onAddItem = { newItem ->
                        lifecycleScope.launch {
                            listDataStore.appendToList(newItem)
                        }
                    },
                    onScheduleNotification = { delayInSeconds ->
                        NotificationUtils.scheduleNotification(
                            context = this,
                            delayInSeconds = delayInSeconds,
                            title = "Reminder Notification",
                            message = "Scheduled reminder triggered."
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
    onAddItem: (String) -> Unit,
    onScheduleNotification: (Long) -> Unit
) {
    // State for managing dialog visibility
    val isDialogOpen = remember { mutableStateOf(false) }
    val savedList = remember { mutableStateOf<List<String>>(emptyList()) }

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
                onNotifyClick = { onScheduleNotification(10) }
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
            onDismiss = { enteredText ->
                if (!enteredText.isNullOrEmpty()) {
                    onAddItem(enteredText)
                }
                isDialogOpen.value = false
            }
        )
    }
}

@Composable
fun CommonTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
fun CommonFloatingButtons(
    onAddClick: () -> Unit,
    onNotifyClick: () -> Unit
) {
    Column {
        ExtendedFloatingActionButton(
            onClick = onAddClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Item") },
            text = { Text("Add Reminder") }
        )
        ExtendedFloatingActionButton(
            onClick = onNotifyClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Send Notification") },
            text = { Text("Send Notification") }
        )
    }
}

@Composable
fun ReminderList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { i ->
            ReminderListItem(
                itemName = items[i],
                itemNumber = i + 1,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ReminderListItem(
    modifier: Modifier = Modifier,
    itemNumber: Int = 0,
    itemName: String = "Item name",
    itemReminderTime: LocalTime = LocalTime.now(ZoneId.systemDefault())
) {
    Column {
        ListItem(
            headlineContent = { Text(itemName) },
            supportingContent = {
                Text(
                    "Reminder scheduled for ${
                        itemReminderTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
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
    onDismiss: (String?) -> Unit
) {
    val textFieldState = rememberTextFieldState() // Using Compose input states

    BasicAlertDialog(
        onDismissRequest = { onDismiss(null) }
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AddItemDialogTextField(
                    state = textFieldState,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    enabled = textFieldState.text.isNotEmpty(),
                    onClick = { onDismiss(textFieldState.text.toString()) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Confirm")
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
    Column {
        TextField(
            state = state,
            isError = state.text.isEmpty(),
            lineLimits = TextFieldLineLimits.SingleLine,
            label = {
                Text("Reminder name")
            },
            placeholder = { Text("Enter reminder name") },
            modifier = modifier.fillMaxWidth()
        )
        if (state.text.isEmpty()) {
            Text(
                text = "Reminder name required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp)
            )
        }
    }
}

fun requestNotificationPermission(context: ComponentActivity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
        1001
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivityScreen() {
    LinkLaterTheme {
        MainActivityScreen(
            listDataStore = ListDataStore(LocalContext.current),
            onAddItem = {},
            onScheduleNotification = {}
        )
    }
}
