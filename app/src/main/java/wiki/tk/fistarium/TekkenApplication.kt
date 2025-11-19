package wiki.tk.fistarium

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // Initialize App Check with Debug Provider (dev/testing mode)
        // For production, replace with SafetyNet or Play Integrity provider
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Timber.tag(TAG).d("App Check initialized (Debug mode)")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "App Check initialization failed")
        }

        // Initialize Analytics (ensures Remote Config ABT can register experiments)
        try {
            FirebaseAnalytics.getInstance(this)
            Timber.tag(TAG).d("Analytics initialized")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Analytics initialization failed")
        }

        // Perform anonymous sign-in if no user is logged in
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                
                if (currentUser == null) {
                    Timber.tag(TAG).d("No user logged in, attempting anonymous sign-in...")
                    auth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.tag(TAG)
                                .d("Anonymous sign-in successful: UID=${auth.currentUser?.uid}")
                        } else {
                            Timber.tag(TAG).e(task.exception, "Anonymous sign-in failed")
                        }
                    }
                } else {
                    Timber.tag(TAG)
                        .d("User already logged in: UID=${currentUser.uid}, isAnonymous=${currentUser.isAnonymous}")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Auth check/sign-in failed")
            }
        }

        Timber.tag(TAG).d("Application onCreate completed")
    }
}