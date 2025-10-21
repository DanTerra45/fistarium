package wiki.tk.fistarium.features.notification.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import wiki.tk.fistarium.MainActivity
import wiki.tk.fistarium.R
import kotlin.random.Random

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "fistarium_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"] ?: getString(R.string.notification_default_title)
            val body = remoteMessage.data["body"] ?: ""
            val characterId = remoteMessage.data["characterId"]
            val type = remoteMessage.data["type"] // e.g., "new_character", "character_updated", "new_translation"
            
            showNotification(title, body, characterId, type)
        }

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: getString(R.string.notification_app_name),
                body = notification.body ?: "",
                characterId = null,
                type = null
            )
        }
    }

    override fun onNewToken(token: String) {
        // Send token to server if needed
        // For now, we'll just log it
        android.util.Log.d("FCM", "New token: $token")
        
        // You can send this to your backend server or save it locally
        // to send targeted notifications
    }

    private fun showNotification(title: String, body: String, characterId: String?, type: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add character ID if present to navigate to detail screen
            characterId?.let { putExtra("characterId", it) }
            type?.let { putExtra("notificationType", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
