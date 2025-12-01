package wiki.tk.fistarium.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.characters.data.CharacterRepositoryImpl
import wiki.tk.fistarium.features.characters.data.local.CharacterLocalDataSource
import wiki.tk.fistarium.features.characters.data.local.CharacterMapper
import wiki.tk.fistarium.features.characters.data.remote.CharacterRemoteDataSource
import wiki.tk.fistarium.features.characters.domain.CharacterRepository
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel

val characterModule = module {

    // Mappers
    factory { CharacterMapper(get()) }

    // Data sources
    single { CharacterLocalDataSource(get(), get()) }
    single { CharacterRemoteDataSource(get(), get()) }

    // Repository
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get(), get()) }

    // Use case
    single { CharacterUseCase(get()) }

    // ViewModel
    viewModel { CharacterViewModel(get(), get(), get()) }
}
