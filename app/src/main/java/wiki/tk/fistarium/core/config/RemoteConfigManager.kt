package wiki.tk.fistarium.core.config

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import wiki.tk.fistarium.BuildConfig

/**
 * Manager for Firebase Remote Config
 * Handles feature flags, maintenance mode, and dynamic configurations
 */
class RemoteConfigManager {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        // Config Keys
        const val KEY_MAINTENANCE_MODE = "maintenance_mode"
        const val KEY_MIN_APP_VERSION = "min_app_version"
        const val KEY_FORCE_UPDATE = "force_update"
        const val KEY_ENABLE_CHARACTER_EDITING = "enable_character_editing"
        const val KEY_ENABLE_TRANSLATIONS = "enable_translations"
        const val KEY_ENABLE_COMMENTS = "enable_comments"
        const val KEY_MAX_IMAGE_SIZE_MB = "max_image_size_mb"
        const val KEY_FEATURED_CHARACTER_ID = "featured_character_id"

        // Default Values
        private val DEFAULTS = mapOf(
            KEY_MAINTENANCE_MODE to false,
            KEY_MIN_APP_VERSION to "0.0.1",
            KEY_FORCE_UPDATE to false,
            KEY_ENABLE_CHARACTER_EDITING to true,
            KEY_ENABLE_TRANSLATIONS to true,
            KEY_ENABLE_COMMENTS to true,
            KEY_MAX_IMAGE_SIZE_MB to 5L,
            KEY_FEATURED_CHARACTER_ID to ""
        )
    }

    init {
        // Set default values
        remoteConfig.setDefaultsAsync(DEFAULTS)

        // Configure fetch settings
        // CAUTION: Low intervals in production can cause Firebase Throttling (blocking requests)
        // We use 0s for Debug to test immediately, and 60s for Release.
        val fetchInterval = if (BuildConfig.DEBUG) 0L else 3600L
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(fetchInterval)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    /**
     * Fetch and activate remote config values
     * @return true if fetch and activation succeeded
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Listen for real-time config updates
     */
    fun observeUpdates(): Flow<Unit> = callbackFlow {
        val listener = object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                // Activate the new config
                remoteConfig.activate().addOnCompleteListener {
                    if (it.isSuccessful) {
                        trySend(Unit)
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                error.printStackTrace()
            }
        }
        
        val registration = remoteConfig.addOnConfigUpdateListener(listener)
        awaitClose { registration.remove() }
    }

    // Maintenance Mode
    fun isMaintenanceMode(): Boolean = remoteConfig.getBoolean(KEY_MAINTENANCE_MODE)

    // Version Control
    fun getMinAppVersion(): String = remoteConfig.getString(KEY_MIN_APP_VERSION)
    fun isForceUpdateRequired(): Boolean = remoteConfig.getBoolean(KEY_FORCE_UPDATE)

    // Feature Flags
    fun isCharacterEditingEnabled(): Boolean = remoteConfig.getBoolean(KEY_ENABLE_CHARACTER_EDITING)
    fun isTranslationsEnabled(): Boolean = remoteConfig.getBoolean(KEY_ENABLE_TRANSLATIONS)
    fun isCommentsEnabled(): Boolean = remoteConfig.getBoolean(KEY_ENABLE_COMMENTS)

    // Configuration Values
    fun getMaxImageSizeMB(): Long = remoteConfig.getLong(KEY_MAX_IMAGE_SIZE_MB)
    fun getFeaturedCharacterId(): String = remoteConfig.getString(KEY_FEATURED_CHARACTER_ID)

    /**
     * Check if app version is compatible
     * @param currentVersion Current app version string (e.g., "0.0.1")
     */
    fun isVersionCompatible(currentVersion: String): Boolean {
        val minVersion = getMinAppVersion()
        return compareVersions(currentVersion, minVersion) >= 0
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val v1 = parts1.getOrNull(i) ?: 0
            val v2 = parts2.getOrNull(i) ?: 0
            when {
                v1 > v2 -> return 1
                v1 < v2 -> return -1
            }
        }
        return 0
    }
}