package wiki.tk.fistarium.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.core.preferences.PreferencesManager
import wiki.tk.fistarium.core.utils.NetworkMonitor
import wiki.tk.fistarium.features.characters.data.local.AppDatabase

val coreModule = module {
    
    // Database
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "tekken_db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
            .build()
    }
    
    single { get<AppDatabase>().characterDao() }

    // Firebase
    single { FirebaseAuth.getInstance() }
    
    single { 
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        firestore.firestoreSettings = settings
        firestore
    }

    // Json
    single { 
        Json { 
            ignoreUnknownKeys = true
            isLenient = true 
        } 
    }

    // Core utilities
    single { RemoteConfigManager() }
    single { NetworkMonitor(androidContext()) }
    single { PreferencesManager(androidContext()) }
}
