package wiki.tk.fistarium.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import wiki.tk.fistarium.features.news.data.NewsRepository
import wiki.tk.fistarium.features.news.data.NewsRepositoryImpl
import wiki.tk.fistarium.features.news.presentation.NewsViewModel

val newsModule = module {
    single<NewsRepository> { NewsRepositoryImpl(get()) }
    viewModel { NewsViewModel(get()) }
}
