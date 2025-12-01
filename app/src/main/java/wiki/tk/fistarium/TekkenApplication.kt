package wiki.tk.fistarium

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import wiki.tk.fistarium.core.preferences.PreferencesManager
import wiki.tk.fistarium.di.appModule

class TekkenApplication : Application() {

    companion object {
        private const val TAG = "FistariumApp"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        
        // Configure Timber for logging
        // Note: Timber logs are stripped in release builds via proguard/R8
        Timber.plant(Timber.DebugTree())
        
        Timber.tag(TAG).d("Application onCreate started")

        // Initialize Koin first
        startKoin {
            androidContext(this@TekkenApplication)
            modules(appModule)
        }
        Timber.tag(TAG).d("Koin initialized")

        // Apply saved language preference asynchronously
        // Language will be applied on next Activity recreation or immediately if fast enough
        applySavedLanguageAsync()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Timber.tag(TAG).d("Firebase initialized")

        // Initialize Analytics (ensures Remote Config ABT can register experiments)
        try {
            FirebaseAnalytics.getInstance(this)
            Timber.tag(TAG).d("Analytics initialized")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Analytics initialization failed")
        }

        Timber.tag(TAG).d("Application onCreate completed")
    }

    private fun applySavedLanguageAsync() {
        applicationScope.launch {
            try {
                val preferencesManager: PreferencesManager by inject()
                val savedLanguage = preferencesManager.appLanguage.first()
                val appLocale = LocaleListCompat.forLanguageTags(savedLanguage)
                AppCompatDelegate.setApplicationLocales(appLocale)
                Timber.tag(TAG).d("Language applied: $savedLanguage")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to apply saved language")
            }
        }
    }
}