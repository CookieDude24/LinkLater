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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.maxid.linklater.AddItemDialogComposables.AddItemDialog
import me.maxid.linklater.ui.theme.LinkLaterTheme
import java.text.DateFormat
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
                    listDataStore = listDataStore, onAddItem = { newItem, scheduledTime ->
                        lifecycleScope.launch {
                            listDataStore.appendToList(
                                newItem,
                                time = timeFormatter(time = scheduledTime, withWeekday = true),
                            )
                        }
                        NotificationUtils.scheduleNotification(
                            context = this@MainActivity,
                            delayInSeconds = scheduledTime.timeInMillis / 1000 - System.currentTimeMillis() / 1000,
                            title = getString(R.string.notification_title),
                            message = newItem,
                        )

                    })
            }
        }
    }
}

@Composable
fun MainActivityScreen(
    listDataStore: ListDataStore,
    onAddItem: (String, Calendar) -> Unit,
) {
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

    Scaffold(topBar = {
        CommonTopBar(title = stringResource(R.string.app_name))
    }, floatingActionButton = {
        CommonFloatingButtons(
            onAddClick = { isDialogOpen.value = true },
        )
    }) { padding ->
        ReminderList(
            items = savedList.value, modifier = Modifier.padding(padding)
        )
    }

    if (isDialogOpen.value) {
        AddItemDialog(
            onDismiss = { enteredText, enteredTime ->
                if (!enteredText.isNullOrEmpty()) {
                    if (enteredTime != null) {
                        onAddItem(enteredText, enteredTime)
                    }
                }
                isDialogOpen.value = false
            }, modifier = Modifier.padding(4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    )
}

@Composable
fun CommonFloatingButtons(
    onAddClick: () -> Unit,
) {
    Column {
        ExtendedFloatingActionButton(
            onClick = onAddClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_reminder_fab)) },
            text = { Text(stringResource(R.string.add_reminder_fab)) },
            modifier = Modifier
                .padding(end = 16.dp, bottom = 64.dp)
                .scale(1.2f)
        )
    }
}

@Composable
fun ReminderList(
    items: List<Pair<String, String>>, modifier: Modifier = Modifier
) {
    if (items.size == 0) {
        Text(
            text = stringResource(R.string.no_items_added_yet),
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
            modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)
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
    itemName: String = stringResource(R.string.default_list_item_name),
    itemReminderTime: String = Calendar.getInstance(TimeZone.getDefault()).toString(),
) {
    Column {
        ListItem(
            headlineContent = { Text(itemName) },
            supportingContent = {
                Text(
                    stringResource(R.string.reminder_scheduled_for, itemReminderTime)
                )
            },
            trailingContent = { Text(stringResource(R.string.reminder_number, itemNumber)) },
        )
        HorizontalDivider()
    }
}


fun requestNotificationPermission(context: ComponentActivity) {
    ActivityCompat.requestPermissions(
        context, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
    )
}

fun timeFormatter(time: Calendar, withWeekday: Boolean = false): String {
    if (withWeekday) {
        val dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        return dateTimeFormatter.format(time.time)
    } else {
        val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
        return timeFormatter.format(time.time)
    }
}