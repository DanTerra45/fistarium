package wiki.tk.fistarium

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import wiki.tk.fistarium.di.appModule

class TekkenApplication : Application() {

    companion object {
        private const val TAG = "FistariumApp"
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("Application onCreate started")

        // Initialize Koin first
        startKoin {
            androidContext(this@TekkenApplication)
            modules(appModule)
        }
        Timber.tag(TAG).d("Koin initialized")

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
}