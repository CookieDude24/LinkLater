package me.maxid.linklater

import NotificationWorker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationUtils {

    /**
     * Schedules a notification using WorkManager.
     *
     * @param context The application context.
     * @param delayInSeconds The delay before the notification is sent, in seconds.
     * @param title The title of the notification.
     * @param message The body content of the notification.
     */
    fun scheduleNotification(context: Context, delayInSeconds: Long, title: String, message: String) {
        val workData = workDataOf(
            "title" to title,
            "message" to message
        )

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
    /**
     * Schedules a reminder using the scheduleNotification function.
     *
     * @param context The application context.
     * @param time The point in time the reminder should be scheduled for.
     * @param message The body content of the notification.
     */
    fun scheduleReminder(context: Context, time: Calendar, message: String) {
        scheduleNotification(context,time.timeInMillis / 1000 - System.currentTimeMillis() / 1000,"Reminder",message)
    }

    /**
     * Sends a notification directly (without WorkManager).
     *
     * @param context The application context.
     * @param title The title of the notification.
     * @param message The body content of the notification.
     */
    fun sendNotificationDirectly(context: Context, title: String, message: String) {
        val channelId = "reminders_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 and above
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

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}