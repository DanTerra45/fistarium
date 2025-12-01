package wiki.tk.fistarium.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.settings.presentation.SettingsViewModel

val settingsModule = module {

    // ViewModel
    viewModel { SettingsViewModel(get()) }
}
