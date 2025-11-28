package wiki.tk.fistarium.features.versus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase
import wiki.tk.fistarium.features.characters.domain.Move
import wiki.tk.fistarium.features.versus.domain.VersusUseCase

class VersusViewModel(
    private val characterUseCase: CharacterUseCase,
    private val versusUseCase: VersusUseCase
) : ViewModel() {

    private val _player1 = MutableStateFlow<Character?>(null)
    val player1: StateFlow<Character?> = _player1

    private val _player2 = MutableStateFlow<Character?>(null)
    val player2: StateFlow<Character?> = _player2

    private val _allCharacters = MutableStateFlow<List<Character>>(emptyList())
    val allCharacters: StateFlow<List<Character>> = _allCharacters

    private val _comparisonResult = MutableStateFlow<ComparisonResult?>(null)
    val comparisonResult: StateFlow<ComparisonResult?> = _comparisonResult

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            characterUseCase.getCharacters().collect { chars ->
                // Filter only Tekken 8 characters for Versus mode
                _allCharacters.value = chars.filter { it.games.contains("TK8") }
            }
        }
    }

    fun selectPlayer1(character: Character) {
        _player1.value = character
        calculateComparison()
    }

    fun selectPlayer2(character: Character) {
        _player2.value = character
        calculateComparison()
    }

    fun clearSelection() {
        _player1.value = null
        _player2.value = null
        _comparisonResult.value = null
    }

    private fun calculateComparison() {
        val p1 = _player1.value
        val p2 = _player2.value

        if (p1 != null && p2 != null) {
            val statsDiff = versusUseCase.compareStats(p1.stats, p2.stats)
            _comparisonResult.value = ComparisonResult(
                p1 = p1,
                p2 = p2,
                statsDiff = statsDiff
            )
        }
    }

    fun findPunishers(targetMoveId: String, attacker: Character, defender: Character): List<Move> {
        return versusUseCase.findPunishers(targetMoveId, attacker, defender)
    }
}

data class ComparisonResult(
    val p1: Character,
    val p2: Character,
    val statsDiff: Map<String, Int>
)
