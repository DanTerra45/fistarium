package wiki.tk.fistarium.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.FirebaseFirestoreSettings
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
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel

val appModule = module {

    // Database
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "tekken_db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

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

    // Data sources
    single { CharacterLocalDataSource(get(), get()) }
    single { 
        val firestore = FirebaseFirestore.getInstance()
        // Configure Firestore settings with persistent cache (new API)
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        firestore.firestoreSettings = settings
        
        CharacterRemoteDataSource(firestore, get())
    }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl() }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }

    // Use cases
    single { AuthUseCase(get()) }
    single { CharacterUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { CharacterViewModel(get(), get(), get(), get()) }
}