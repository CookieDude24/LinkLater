
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

        val channel = NotificationChannel(
            channelId,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for reminders"
        }
        notificationManager.createNotificationChannel(channel)

        // Create an Intent to open the link in a browser or another app
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Wrap the intent in a PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.link_monochrome) // Replace with your drawable
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

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