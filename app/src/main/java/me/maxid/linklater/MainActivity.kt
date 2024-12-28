@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package me.maxid.linklater

import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.maxid.linklater.ui.theme.LinkLaterTheme
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    lateinit var listDataStore: ListDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        listDataStore = ListDataStore(this)

        setContent {
            LinkLaterTheme {
                val savedList = remember { mutableStateOf<List<String>>(emptyList()) }
                val isDialogOpen = remember { mutableStateOf(false) }
                val textFieldState = rememberTextFieldState()
                LaunchedEffect(Unit) {
                    listDataStore.getList().collectLatest { list ->
                        savedList.value = list
                    }
                }
                fun addItemToList(item: String) {
                    lifecycleScope.launch {
                        listDataStore.appendToList(item)
                    }
                }
                // UI
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {}
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = {
                                isDialogOpen.value = true // Open the dialog on button click
                            },
                            icon = { Icon(Icons.Filled.Add, "Localized Description") },
                            text = { Text(text = stringResource(R.string.add_item_fab)) },
                        )
                    }

                ) { padding ->
                    LazyColumn(
                        contentPadding = padding,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedList.value) { item ->
                            ReminderListItem(
                                itemName = item,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                itemNumber = savedList.value.indexOf(item) + 1


                            )
                        }
                    }
                }
                if (isDialogOpen.value) {
                    AddItemDialog(
                        onDismiss = {
                            isDialogOpen.value = false // Close dialog
                            addItemToList(textFieldState.text.toString())
                            textFieldState.clearText()
                        },
                        state = textFieldState
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: (String?) -> Unit = {}, state: TextFieldState = rememberTextFieldState()) {
    val openDialog = remember { mutableStateOf(true) }
    val enteredText = remember { mutableStateOf("") } // State to hold the entered text


    if (openDialog.value) {
        BasicAlertDialog(
            onDismissRequest = {
                onDismiss(null) // Pass null on outside tap or back button
                openDialog.value = false
            }

        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AddItemDialogTextField(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        state = state
                    )
                    TextButton(
                        enabled = state.text.isNotEmpty(),
                        onClick = {
                            onDismiss(enteredText.value) // Pass entered text to parent
                            openDialog.value = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AddItemDialogTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState()
) {
    Column{
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
                modifier = Modifier.padding(top=8.dp,start = 16.dp)
            )
        }
    }
}

@Preview
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
            supportingContent = { Text("Reminder scheduled for ${itemReminderTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}") },
            trailingContent = { Text("Reminder #$itemNumber") },
        )
        HorizontalDivider()
    }
}
fun setAlarm(context: Context) {
    // Get the AlarmManager service
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Create a pending intent to specify what to do when alarm triggers
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Schedule the alarm
    val triggerTime = SystemClock.elapsedRealtime() + (5 * 1000L) // Alarm triggers in 5 seconds
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.ELAPSED_REALTIME_WAKEUP, // Use elapsed real-time since boot
        triggerTime,
        pendingIntent
    )

    Toast.makeText(context, "Alarm set for 5 seconds from now!", Toast.LENGTH_SHORT).show()
}