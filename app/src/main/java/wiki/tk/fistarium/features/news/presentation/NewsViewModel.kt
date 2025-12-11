package wiki.tk.fistarium.features.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wiki.tk.fistarium.features.news.data.NewsRepository
import wiki.tk.fistarium.features.news.domain.NewsArticle

class NewsViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState())
    val state: StateFlow<NewsState> = _state.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = newsRepository.getNewsArticles()
            
            if (result.isSuccess) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        articles = result.getOrNull() ?: emptyList(),
                        error = null
                    ) 
                }
            } else {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.message ?: "Failed to load news"
                    ) 
                }
            }
        }
    }

    data class NewsState(
        val articles: List<NewsArticle> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
