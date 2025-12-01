package wiki.tk.fistarium.features.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    // Consolidated state - single source of truth
    private val _state = MutableStateFlow(CharactersState())
    val state: StateFlow<CharactersState> = _state.asStateFlow()

    // Feature flags state (reactive)
    private val _featureFlags = MutableStateFlow(FeatureFlags())
    val featureFlags: StateFlow<FeatureFlags> = _featureFlags.asStateFlow()

    // Backward compatibility accessors - derived StateFlows that ARE reactive
    val characters: StateFlow<List<Character>> = _state
        .map { it.characters }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val selectedGameId: StateFlow<String> = _state
        .map { it.selectedGameId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "TK8")
    
    val filteredCharacters: StateFlow<List<Character>> = _state
        .map { it.filteredCharacters }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val favoriteCharacters: StateFlow<List<Character>> = _state
        .map { it.favorites }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val searchResults: StateFlow<List<Character>> = _state
        .map { it.searchResults }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val selectedCharacter: StateFlow<Character?> = _state
        .map { it.selectedCharacter }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    val syncState: StateFlow<SyncState> = _state
        .map { it.syncState }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SyncState.Idle)
    
    val isOnline: StateFlow<Boolean> = _state
        .map { it.isOnline }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val uiState: StateFlow<UiState> = _state
        .map { it.uiState }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Idle)

    // Legacy getters for feature flags
    val isEditingEnabled: Boolean get() = _featureFlags.value.isEditingEnabled
    val isTranslationsEnabled: Boolean get() = _featureFlags.value.isTranslationsEnabled
    val maxImageSizeMB: Long get() = _featureFlags.value.maxImageSizeMB

    init {
        observeCharacters()
        observeFavorites()
        observeConnectivity()
        fetchRemoteConfig()
        // Sync happens automatically when connectivity is observed
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            characterUseCase.getCharacters().collect { chars ->
                _state.update { current ->
                    current.copy(
                        characters = chars,
                        filteredCharacters = chars.filter { it.games.contains(current.selectedGameId) }
                    )
                }
            }
        }
    }

    fun filterByGame(gameId: String) {
        _state.update { current ->
            current.copy(
                selectedGameId = gameId,
                filteredCharacters = current.characters.filter { it.games.contains(gameId) }
            )
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            characterUseCase.getFavoriteCharacters().collect { favs ->
                _state.update { it.copy(favorites = favs) }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.observeConnectivity().collect { isConnected ->
                _state.update { it.copy(isOnline = isConnected) }
                if (isConnected) {
                    syncCharacters()
                }
            }
        }
    }

    private fun fetchRemoteConfig() {
        viewModelScope.launch {
            remoteConfigManager.fetchAndActivate()
            _featureFlags.update {
                FeatureFlags(
                    isEditingEnabled = remoteConfigManager.isCharacterEditingEnabled(),
                    isTranslationsEnabled = remoteConfigManager.isTranslationsEnabled(),
                    maxImageSizeMB = remoteConfigManager.getMaxImageSizeMB()
                )
            }
        }
    }

    fun getCharacterById(id: String) {
        viewModelScope.launch {
            characterUseCase.getCharacterById(id).collect { char ->
                _state.update { it.copy(selectedCharacter = char) }
            }
        }
    }

    fun syncCharacters() {
        viewModelScope.launch {
            if (!_state.value.isOnline) return@launch
            
            _state.update { it.copy(syncState = SyncState.Loading) }
            
            val result = characterUseCase.syncCharacters()
            val newSyncState = if (result.isSuccess) {
                SyncState.Success
            } else {
                val message = result.exceptionOrNull()?.message ?: "Sync failed"
                if (message.contains("PERMISSION_DENIED")) SyncState.Idle
                else SyncState.Error(message)
            }
            
            _state.update { it.copy(syncState = newSyncState) }
        }
    }

    fun searchCharacters(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val results = characterUseCase.searchCharacters(query)
            val currentGameId = _state.value.selectedGameId
            _state.update { 
                it.copy(
                    searchResults = results.filter { char -> char.games.contains(currentGameId) },
                    uiState = UiState.Success
                )
            }
        }
    }

    fun toggleFavorite(characterId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val result = characterUseCase.toggleFavorite(characterId, isFavorite)
            if (result.isFailure) {
                _state.update { it.copy(uiState = UiState.Error("Failed to update favorite")) }
            }
        }
    }

    fun createCharacter(character: Character, userId: String) {
        if (!_featureFlags.value.isEditingEnabled) {
            _state.update { it.copy(uiState = UiState.Error("Character editing is currently disabled")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val result = characterUseCase.createCharacter(character, userId)
            _state.update { 
                it.copy(uiState = if (result.isSuccess) {
                    UiState.CharacterCreated(result.getOrNull() ?: "")
                } else {
                    UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create character")
                })
            }
        }
    }

    fun updateCharacter(character: Character, userId: String) {
        if (!_featureFlags.value.isEditingEnabled) {
            _state.update { it.copy(uiState = UiState.Error("Character editing is currently disabled")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val result = characterUseCase.updateCharacter(character, userId)
            _state.update { 
                it.copy(uiState = if (result.isSuccess) {
                    UiState.CharacterUpdated
                } else {
                    UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update character")
                })
            }
        }
    }

    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val result = characterUseCase.deleteCharacter(characterId)
            _state.update { 
                it.copy(uiState = if (result.isSuccess) {
                    UiState.CharacterDeleted
                } else {
                    UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete character")
                })
            }
        }
    }

    fun clearUiState() {
        _state.update { it.copy(uiState = UiState.Idle) }
    }

    fun resetSyncState() {
        _state.update { it.copy(syncState = SyncState.Idle) }
    }

    // Consolidated state data class - single source of truth
    data class CharactersState(
        val characters: List<Character> = emptyList(),
        val filteredCharacters: List<Character> = emptyList(),
        val favorites: List<Character> = emptyList(),
        val searchResults: List<Character> = emptyList(),
        val selectedCharacter: Character? = null,
        val selectedGameId: String = "TK8",
        val syncState: SyncState = SyncState.Idle,
        val uiState: UiState = UiState.Idle,
        val isOnline: Boolean = false
    )

    // Feature flags data class
    data class FeatureFlags(
        val isEditingEnabled: Boolean = false,
        val isTranslationsEnabled: Boolean = false,
        val maxImageSizeMB: Long = 5L
    )

    sealed class SyncState {
        data object Idle : SyncState()
        data object Loading : SyncState()
        data object Success : SyncState()
        data class Error(val message: String) : SyncState()
    }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data object Success : UiState()
        data class CharacterCreated(val id: String) : UiState()
        data object CharacterUpdated : UiState()
        data object CharacterDeleted : UiState()
        data class Error(val message: String) : UiState()
    }
}