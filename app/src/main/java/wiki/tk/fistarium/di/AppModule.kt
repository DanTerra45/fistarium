package wiki.tk.fistarium.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.characters.data.local.AppDatabase
import wiki.tk.fistarium.features.characters.data.local.CharacterLocalDataSource
import wiki.tk.fistarium.features.characters.data.local.CharacterSeeder
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
            .build()
    }

    single { get<AppDatabase>().characterDao() }

    // Gson
    single { Gson() }

    // Seeder
    single { CharacterSeeder() }

    // Data sources
    single { CharacterLocalDataSource(get(), get()) }
    single { CharacterRemoteDataSource(FirebaseFirestore.getInstance(), get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl() }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }

    // Use cases
    single { AuthUseCase(get()) }
    single { CharacterUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { CharacterViewModel(get()) }
}