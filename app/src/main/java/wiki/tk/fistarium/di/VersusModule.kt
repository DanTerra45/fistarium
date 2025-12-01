package wiki.tk.fistarium.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.versus.domain.VersusUseCase
import wiki.tk.fistarium.features.versus.presentation.VersusViewModel

val versusModule = module {
    
    // Use case
    single { VersusUseCase() }

    // ViewModel
    viewModel { VersusViewModel(get(), get()) }
}
