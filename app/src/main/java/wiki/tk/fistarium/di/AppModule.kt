package wiki.tk.fistarium.di

import org.koin.dsl.module

/**
 * Main application module that aggregates all feature modules.
 * 
 * Module structure:
 * - coreModule: Database, Firebase, Json, Network utilities
 * - authModule: Authentication repository, use cases, viewmodel
 * - characterModule: Character data sources, repository, use cases, viewmodel
 * - versusModule: Versus comparison use case and viewmodel
 * - settingsModule: Settings viewmodel
 */
val appModule = module {
    includes(
        coreModule,
        authModule,
        characterModule,
        versusModule,
        settingsModule
    )
}