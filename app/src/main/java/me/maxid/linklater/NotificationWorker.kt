import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.maxid.linklater.ListDataStore

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Extract data from input
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: return Result.failure() // The message is the item name

        // Send the notification
        sendNotification(applicationContext, title, message)

        // After sending the notification, remove the item from the DataStore
        removeItemFromDataStore(applicationContext, message)

        return Result.success()
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "reminders_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android 8.0 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message) // Message contains the item name
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }

    private fun removeItemFromDataStore(context: Context, item: String) {
        val listDataStore = ListDataStore(context) // Create an instance of the ListDataStore

        // Run blocking to ensure suspension is handled synchronously
        runBlocking {
            // Get the current list from DataStore
            val currentList = listDataStore.getList().firstOrNull() ?: emptyList()

            // Filter out the item to remove
            val updatedList = currentList.filterNot { it.first == item }

            // Save the updated list back to DataStore
            listDataStore.saveList(updatedList)
        }
    }
}