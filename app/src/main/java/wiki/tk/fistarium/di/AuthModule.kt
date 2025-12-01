package wiki.tk.fistarium.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.auth.data.AuthRepositoryImpl
import wiki.tk.fistarium.features.auth.domain.AuthRepository
import wiki.tk.fistarium.features.auth.domain.AuthUseCase
import wiki.tk.fistarium.features.auth.domain.SyncFavoritesUseCase
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel

val authModule = module {
    
    // Repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    // Use cases
    single { AuthUseCase(get()) }
    single { SyncFavoritesUseCase(get()) }

    // ViewModel
    viewModel { AuthViewModel(get(), get()) }
}
