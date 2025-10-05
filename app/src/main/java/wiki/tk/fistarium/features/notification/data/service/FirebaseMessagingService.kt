package wiki.tk.fistarium.features.notification.data.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming FCM messages
        remoteMessage.notification?.let { notification ->
            // Handle notification
        }
    }

    override fun onNewToken(token: String) {
        // Handle new FCM token
    }
}