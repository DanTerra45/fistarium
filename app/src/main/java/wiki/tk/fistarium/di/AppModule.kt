package wiki.tk.fistarium.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.characters.data.local.AppDatabase
import wiki.tk.fistarium.features.characters.data.local.CharacterLocalDataSource
import wiki.tk.fistarium.features.characters.data.local.CharacterMapper
import wiki.tk.fistarium.features.auth.data.AuthRepositoryImpl
import wiki.tk.fistarium.features.characters.data.remote.CharacterRemoteDataSource
import wiki.tk.fistarium.features.characters.data.CharacterRepositoryImpl
import wiki.tk.fistarium.features.auth.domain.AuthRepository
import wiki.tk.fistarium.features.characters.domain.CharacterRepository
import wiki.tk.fistarium.features.auth.domain.AuthUseCase
import wiki.tk.fistarium.features.auth.domain.SyncFavoritesUseCase
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel
import wiki.tk.fistarium.features.versus.presentation.VersusViewModel
import wiki.tk.fistarium.features.versus.domain.VersusUseCase
import wiki.tk.fistarium.features.settings.presentation.SettingsViewModel

val appModule = module {

    // Database
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "tekken_db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single { FirebaseAuth.getInstance() }

    single { get<AppDatabase>().characterDao() }

    // Remote Config
    single { wiki.tk.fistarium.core.config.RemoteConfigManager() }

    // Storage
    single { wiki.tk.fistarium.core.storage.ImageUploadManager() }

    // Notifications
    single { wiki.tk.fistarium.features.notification.domain.NotificationManager() }

    // Json
    single { Json { ignoreUnknownKeys = true; isLenient = true } }

    // Mappers
    single { CharacterMapper(get()) }

    // Network Monitor
    single { wiki.tk.fistarium.core.utils.NetworkMonitor(androidContext()) }

    // Preferences
    single { wiki.tk.fistarium.core.preferences.PreferencesManager(androidContext()) }

    // Data sources
    single { CharacterLocalDataSource(get(), get()) }
    
    single { 
        val firestore = FirebaseFirestore.getInstance()
        // Configure Firestore settings with persistent cache (new API)
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        firestore.firestoreSettings = settings
        firestore
    }

    single { CharacterRemoteDataSource(get(), get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get(), get()) }

    // Use cases
    single { AuthUseCase(get()) }
    single { SyncFavoritesUseCase(get()) }
    single { CharacterUseCase(get()) }
    single { VersusUseCase() }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel { CharacterViewModel(get(), get(), get()) }
    viewModel { VersusViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
}