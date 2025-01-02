
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.maxid.linklater.ListDataStore
import me.maxid.linklater.R

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Extract data from input, send notification and remove it from the dataStore
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: return Result.failure() // The message is the item name
        sendNotification(applicationContext, title, message)
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


        // Build the notification, Intents are basically the onClick of the notification
        val intent = Intent(ACTION_VIEW, Uri.parse(message)).apply {
            addCategory(CATEGORY_BROWSABLE)
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context,1,intent,FLAG_IMMUTABLE)
        context.startActivity(intent)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notifications_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // may as well be referred to as "onClick"
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }

    private fun removeItemFromDataStore(context: Context, item: String) {
        val listDataStore = ListDataStore(context)

        runBlocking {
            val currentList = listDataStore.getList().firstOrNull() ?: emptyList()
            val updatedList = currentList.filterNot { it.first == item }
            listDataStore.saveList(updatedList)
        }
    }
}