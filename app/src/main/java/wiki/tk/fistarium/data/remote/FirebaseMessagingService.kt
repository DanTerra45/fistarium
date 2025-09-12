package wiki.tk.fistarium.data.remote

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming FCM messages here
        // For now, just log the message
        remoteMessage.notification?.let {
            // Handle notification payload
        }
    }

    override fun onNewToken(token: String) {
        // Handle new FCM token
        // You can send this token to your server if needed
    }
}