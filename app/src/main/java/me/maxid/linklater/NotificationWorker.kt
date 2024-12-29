   import android.app.NotificationChannel
   import android.app.NotificationManager
   import android.content.Context
   import androidx.core.app.NotificationCompat
   import androidx.work.Worker
   import androidx.work.WorkerParameters

   class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
       Worker(appContext, workerParams) {

       override fun doWork(): Result {
           // Get input data if required
           val title = inputData.getString("title") ?: "Reminder"
           val message = inputData.getString("message") ?: "Don't forget to complete your tasks!"
           
           // Send the notification
           sendNotification(applicationContext, title, message)

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
               .setSmallIcon(android.R.drawable.ic_dialog_info) // Set your own icon
               .setContentTitle(title)
               .setContentText(message)
               .setPriority(NotificationCompat.PRIORITY_HIGH)
               .build()

           // Show the notification
           notificationManager.notify(1, notification)
       }
   }