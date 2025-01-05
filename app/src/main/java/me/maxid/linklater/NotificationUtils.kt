package me.maxid.linklater

import NotificationWorker
import android.content.Context
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
        scheduleNotification(context,time.timeInMillis / 1000 - System.currentTimeMillis() / 1000,title= context.getString(R.string.notification_title),message)
    }
}