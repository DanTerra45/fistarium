package wiki.tk.fistarium.features.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.core.utils.NetworkMonitor
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase

class CharacterViewModel(
    private val characterUseCase: CharacterUseCase,
    private val remoteConfigManager: RemoteConfigManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters

    private val _selectedGameId = MutableStateFlow<String>("TK8")
    val selectedGameId: StateFlow<String> = _selectedGameId

    private val _filteredCharacters = MutableStateFlow<List<Character>>(emptyList())
    val filteredCharacters: StateFlow<List<Character>> = _filteredCharacters

    private val _favoriteCharacters = MutableStateFlow<List<Character>>(emptyList())
    val favoriteCharacters: StateFlow<List<Character>> = _favoriteCharacters

    private val _searchResults = MutableStateFlow<List<Character>>(emptyList())
    val searchResults: StateFlow<List<Character>> = _searchResults

    private val _selectedCharacter = MutableStateFlow<Character?>(null)
    val selectedCharacter: StateFlow<Character?> = _selectedCharacter

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    // Feature flags from Remote Config
    val isEditingEnabled: Boolean get() = remoteConfigManager.isCharacterEditingEnabled()
    val isTranslationsEnabled: Boolean get() = remoteConfigManager.isTranslationsEnabled()
    val maxImageSizeMB: Long get() = remoteConfigManager.getMaxImageSizeMB()

    init {
        loadCharacters()
        loadFavorites()
        observeConnectivity()
        fetchRemoteConfig()
        
        // Combine characters and selectedGameId to produce filteredCharacters
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(_characters, _selectedGameId) { chars, gameId ->
                chars.filter { char -> char.games.contains(gameId) }
            }.collect { filtered ->
                _filteredCharacters.value = filtered
            }
        }
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            characterUseCase.getCharacters().collect { chars ->
                _characters.value = chars
            }
        }
    }

    fun filterByGame(gameId: String) {
        _selectedGameId.value = gameId
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            characterUseCase.getFavoriteCharacters().collect { favs ->
                _favoriteCharacters.value = favs
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.observeConnectivity().collect { isConnected ->
                _isOnline.value = isConnected
                if (isConnected) {
                    // Auto-sync when coming online
                    syncCharacters()
                }
            }
        }
    }

    private fun fetchRemoteConfig() {
        viewModelScope.launch {
            remoteConfigManager.fetchAndActivate()
        }
    }

    fun getCharacterById(id: String) {
        viewModelScope.launch {
            characterUseCase.getCharacterById(id).collect { char ->
                _selectedCharacter.value = char
            }
        }
    }

    fun syncCharacters() {
        viewModelScope.launch {
            if (!_isOnline.value) {
                // Don't show error for offline, just return
                return@launch
            }
            
            _syncState.value = SyncState.Loading
            val result = characterUseCase.syncCharacters()
            _syncState.value = if (result.isSuccess) {
                SyncState.Success
            } else {
                val error = result.exceptionOrNull()
                val message = error?.message ?: "Sync failed"
                // Suppress PERMISSION_DENIED error for guest users or initial sync
                if (message.contains("PERMISSION_DENIED")) {
                    SyncState.Idle
                } else {
                    SyncState.Error(message)
                }
            }
        }
    }

    fun searchCharacters(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val results = characterUseCase.searchCharacters(query)
            // Filter results by the currently selected game
            val currentGameId = _selectedGameId.value
            _searchResults.value = results.filter { it.games.contains(currentGameId) }
            _uiState.value = UiState.Success
        }
    }

    fun toggleFavorite(characterId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val result = characterUseCase.toggleFavorite(characterId, isFavorite)
            if (result.isFailure) {
                _uiState.value = UiState.Error("Failed to update favorite")
            }
        }
    }

    fun createCharacter(character: Character, userId: String) {
        if (!isEditingEnabled) {
            _uiState.value = UiState.Error("Character editing is currently disabled")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = characterUseCase.createCharacter(character, userId)
            _uiState.value = if (result.isSuccess) {
                UiState.CharacterCreated(result.getOrNull() ?: "")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create character")
            }
        }
    }

    fun updateCharacter(character: Character, userId: String) {
        if (!isEditingEnabled) {
            _uiState.value = UiState.Error("Character editing is currently disabled")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = characterUseCase.updateCharacter(character, userId)
            _uiState.value = if (result.isSuccess) {
                UiState.CharacterUpdated
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update character")
            }
        }
    }

    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = characterUseCase.deleteCharacter(characterId)
            _uiState.value = if (result.isSuccess) {
                UiState.CharacterDeleted
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete character")
            }
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }

    sealed class SyncState {
        object Idle : SyncState()
        object Loading : SyncState()
        object Success : SyncState()
        data class Error(val message: String) : SyncState()
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class CharacterCreated(val id: String) : UiState()
        object CharacterUpdated : UiState()
        object CharacterDeleted : UiState()
        data class Error(val message: String) : UiState()
    }
}