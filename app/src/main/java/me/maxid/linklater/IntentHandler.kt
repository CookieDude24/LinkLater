package me.maxid.linklater

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.maxid.linklater.ui.theme.LinkLaterTheme

class IntentHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            LinkLaterTheme {
                AddItemDialog(
                    onDismiss = { notificationText, time ->
                    if (!notificationText.isNullOrEmpty()) {
                        Toast.makeText(this, "Text: $notificationText", Toast.LENGTH_SHORT).show()
                        if (time != null) {
                            Toast.makeText(this, "Time: ${time.time}, Text: $notificationText", Toast.LENGTH_SHORT).show()
                        }
                    }
                    finish()
                },
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

                )
            }
        }
    }
}