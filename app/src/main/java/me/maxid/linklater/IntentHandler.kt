package me.maxid.linklater

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.maxid.linklater.AddItemDialogComposables.AddItemDialog
import me.maxid.linklater.ui.theme.LinkLaterTheme

class IntentHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinkLaterTheme {
                AddItemDialog(
                    onDismiss = { notificationText, time ->
                    if (!notificationText.isNullOrEmpty() && time != null) {
                        NotificationUtils.scheduleReminder(
                            this,
                            time,
                            notificationText
                        )

                        Toast.makeText(this, "Successfully created a Reminder!", Toast.LENGTH_SHORT).show()
                        }

                    finish()
                },
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

                )
            }
        }
    }
}