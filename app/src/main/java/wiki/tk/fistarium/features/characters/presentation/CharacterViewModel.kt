package wiki.tk.fistarium.features.characters.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.core.storage.ImageUploadManager
import wiki.tk.fistarium.core.utils.NetworkMonitor
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase
import wiki.tk.fistarium.features.characters.data.remote.CharacterDataSeeder

class CharacterViewModel(
    private val characterUseCase: CharacterUseCase,
    private val remoteConfigManager: RemoteConfigManager,
    private val imageUploadManager: ImageUploadManager,
    private val networkMonitor: NetworkMonitor,
    private val characterDataSeeder: CharacterDataSeeder
) : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters

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

    private val _imageUploadProgress = MutableStateFlow<ImageUploadManager.UploadResult?>(null)
    val imageUploadProgress: StateFlow<ImageUploadManager.UploadResult?> = _imageUploadProgress

    // Feature flags from Remote Config
    val isEditingEnabled: Boolean get() = remoteConfigManager.isCharacterEditingEnabled()
    val isTranslationsEnabled: Boolean get() = remoteConfigManager.isTranslationsEnabled()
    val maxImageSizeMB: Long get() = remoteConfigManager.getMaxImageSizeMB()

    init {
        loadCharacters()
        loadFavorites()
        observeConnectivity()
        fetchRemoteConfig()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            characterUseCase.getCharacters().collect { chars ->
                _characters.value = chars
            }
        }
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
                _syncState.value = SyncState.Error("No internet connection")
                return@launch
            }
            
            _syncState.value = SyncState.Loading
            val result = characterUseCase.syncCharacters()
            _syncState.value = if (result.isSuccess) {
                SyncState.Success
            } else {
                SyncState.Error(result.exceptionOrNull()?.message ?: "Sync failed")
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
            _searchResults.value = results
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

    fun uploadCharacterImage(imageUri: Uri, characterId: String) {
        viewModelScope.launch {
            imageUploadManager.uploadCharacterImage(imageUri, characterId).collect { result ->
                _imageUploadProgress.value = result
            }
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    /**
     * Seeds sample Tekken characters into Firestore.
     * For development/testing purposes only.
     */
    fun seedSampleCharacters() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            val result = characterDataSeeder.seedSampleCharacters()
            _syncState.value = if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                // Trigger a sync to pull the new data
                syncCharacters()
                SyncState.Success
            } else {
                SyncState.Error(result.exceptionOrNull()?.message ?: "Failed to seed characters")
            }
        }
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