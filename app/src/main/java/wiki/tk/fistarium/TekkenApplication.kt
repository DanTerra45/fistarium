package wiki.tk.fistarium

import android.app.Application
import android.util.Log
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
import wiki.tk.fistarium.di.appModule

class TekkenApplication : Application() {

    companion object {
        private const val TAG = "FistariumApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate started")

        // Initialize Koin first
        startKoin {
            androidContext(this@TekkenApplication)
            modules(appModule)
        }
        Log.d(TAG, "Koin initialized")

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase initialized")

        // Initialize App Check with Debug Provider (dev/testing mode)
        // For production, replace with SafetyNet or Play Integrity provider
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "App Check initialized (Debug mode)")
        } catch (e: Exception) {
            Log.e(TAG, "App Check initialization failed", e)
        }

        // Initialize Analytics (ensures Remote Config ABT can register experiments)
        try {
            FirebaseAnalytics.getInstance(this)
            Log.d(TAG, "Analytics initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Analytics initialization failed", e)
        }

        // Perform anonymous sign-in if no user is logged in
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                
                if (currentUser == null) {
                    Log.d(TAG, "No user logged in, attempting anonymous sign-in...")
                    auth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Anonymous sign-in successful: UID=${auth.currentUser?.uid}")
                        } else {
                            Log.e(TAG, "Anonymous sign-in failed", task.exception)
                        }
                    }
                } else {
                    Log.d(TAG, "User already logged in: UID=${currentUser.uid}, isAnonymous=${currentUser.isAnonymous}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auth check/sign-in failed", e)
            }
        }
        
        Log.d(TAG, "Application onCreate completed")
    }
}