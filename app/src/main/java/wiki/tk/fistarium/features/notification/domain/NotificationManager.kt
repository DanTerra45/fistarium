package wiki.tk.fistarium.features.notification.domain

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Manager for handling FCM topic subscriptions
 */
class NotificationManager {

    private val messaging = FirebaseMessaging.getInstance()

    companion object {
        const val TOPIC_ALL_UPDATES = "all_updates"
        const val TOPIC_NEW_CHARACTERS = "new_characters"
        const val TOPIC_CHARACTER_UPDATES = "character_updates"
        const val TOPIC_NEW_TRANSLATIONS = "new_translations"
        const val TOPIC_NEW_COMBOS = "new_combos"
    }

    /**
     * Subscribe to a topic
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            messaging.subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            messaging.unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Subscribe to all default topics
     */
    suspend fun subscribeToAllTopics(): Result<Unit> {
        return try {
            subscribeToTopic(TOPIC_ALL_UPDATES)
            subscribeToTopic(TOPIC_NEW_CHARACTERS)
            subscribeToTopic(TOPIC_CHARACTER_UPDATES)
            subscribeToTopic(TOPIC_NEW_TRANSLATIONS)
            subscribeToTopic(TOPIC_NEW_COMBOS)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Subscribe to character-specific updates
     */
    suspend fun subscribeToCharacter(characterId: String): Result<Unit> {
        return subscribeToTopic("character_$characterId")
    }

    /**
     * Unsubscribe from character-specific updates
     */
    suspend fun unsubscribeFromCharacter(characterId: String): Result<Unit> {
        return unsubscribeFromTopic("character_$characterId")
    }

    /**
     * Get FCM token
     */
    suspend fun getToken(): Result<String> {
        return try {
            val token = messaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
