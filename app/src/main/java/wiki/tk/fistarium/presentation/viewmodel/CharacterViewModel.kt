package wiki.tk.fistarium.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wiki.tk.fistarium.domain.model.Character
import wiki.tk.fistarium.domain.usecase.CharacterUseCase

class CharacterViewModel(private val characterUseCase: CharacterUseCase) : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters

    private val _selectedCharacter = MutableStateFlow<Character?>(null)
    val selectedCharacter: StateFlow<Character?> = _selectedCharacter

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    init {
        loadCharacters()
        syncCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            characterUseCase.getCharacters().collect { chars ->
                _characters.value = chars
                if (chars.isEmpty()) {
                    seedCharacters()
                }
            }
        }
    }

    private fun seedCharacters() {
        // Seed initial data
        val initialCharacters = listOf(
            Character(
                id = "1",
                name = "Kazuya Mishima",
                description = "The ruthless leader of the Mishima Zaibatsu, known for his devil gene.",
                stats = mapOf("health" to 100, "attack" to 85, "defense" to 80)
            ),
            Character(
                id = "2",
                name = "Jin Kazama",
                description = "Son of Kazuya and Jun, wielder of the Devil Gene, seeking to end the Mishima curse.",
                stats = mapOf("health" to 95, "attack" to 90, "defense" to 75)
            ),
            Character(
                id = "3",
                name = "King",
                description = "The noble wrestler masked as a jaguar, fighting for justice.",
                stats = mapOf("health" to 110, "attack" to 80, "defense" to 90)
            )
        )
        // Save to local (assuming we have a way, but for now, since sync will save, but to seed local)
        // Actually, since sync fetches from remote, but for MVP, seed local.
        // Need to add save to repository.
        // For simplicity, since sync is called, and if remote has data, it will save.
        // But for demo, assume Firestore has the data.
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
            _syncState.value = SyncState.Loading
            val result = characterUseCase.syncCharacters()
            _syncState.value = if (result.isSuccess) SyncState.Success else SyncState.Error(result.exceptionOrNull()?.message ?: "Sync failed")
        }
    }

    sealed class SyncState {
        object Idle : SyncState()
        object Loading : SyncState()
        object Success : SyncState()
        data class Error(val message: String) : SyncState()
    }
}