package wiki.tk.fistarium.features.versus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase
import wiki.tk.fistarium.features.characters.domain.Move
import wiki.tk.fistarium.features.versus.domain.VersusUseCase

class VersusViewModel(
    private val characterUseCase: CharacterUseCase,
    private val versusUseCase: VersusUseCase
) : ViewModel() {

    // Consolidated state
    private val _state = MutableStateFlow(VersusState())
    val state: StateFlow<VersusState> = _state.asStateFlow()

    // Backward compatibility accessors
    val player1: StateFlow<Character?> get() = MutableStateFlow(_state.value.player1)
    val player2: StateFlow<Character?> get() = MutableStateFlow(_state.value.player2)
    val allCharacters: StateFlow<List<Character>> get() = MutableStateFlow(_state.value.allCharacters)
    val comparisonResult: StateFlow<ComparisonResult?> get() = MutableStateFlow(_state.value.comparisonResult)

    // Punisher result as StateFlow (reactive)
    private val _punisherResult = MutableStateFlow<PunisherResult?>(null)
    val punisherResult: StateFlow<PunisherResult?> = _punisherResult.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            characterUseCase.getCharacters().collect { chars ->
                // Filter only Tekken 8 characters for Versus mode
                _state.update { it.copy(allCharacters = chars.filter { c -> c.games.contains("TK8") }) }
            }
        }
    }

    fun selectPlayer1(character: Character?) {
        _state.update { it.copy(player1 = character) }
        calculateComparison()
    }

    fun selectPlayer2(character: Character?) {
        _state.update { it.copy(player2 = character) }
        calculateComparison()
    }

    fun clearSelection() {
        _state.update { 
            it.copy(
                player1 = null, 
                player2 = null, 
                comparisonResult = null
            ) 
        }
        _punisherResult.value = null
    }

    private fun calculateComparison() {
        val p1 = _state.value.player1
        val p2 = _state.value.player2

        if (p1 != null && p2 != null) {
            val statsDiff = versusUseCase.compareStats(p1.stats, p2.stats)
            _state.update { 
                it.copy(comparisonResult = ComparisonResult(p1 = p1, p2 = p2, statsDiff = statsDiff))
            }
        }
    }

    // Async version - updates StateFlow instead of returning synchronously
    fun findPunishersAsync(targetMoveId: String, attacker: Character, defender: Character) {
        viewModelScope.launch {
            val punishers = versusUseCase.findPunishers(targetMoveId, attacker, defender)
            val targetMove = attacker.moveList.find { it.id == targetMoveId }
            val frameData = attacker.frameData[targetMoveId]
            
            _punisherResult.value = PunisherResult(
                targetMove = targetMove,
                onBlock = frameData?.onBlock,
                punishers = punishers,
                defender = defender
            )
        }
    }

    // Keep sync version for backward compatibility, but prefer async
    fun findPunishers(targetMoveId: String, attacker: Character, defender: Character): List<Move> {
        return versusUseCase.findPunishers(targetMoveId, attacker, defender)
    }

    fun clearPunisherResult() {
        _punisherResult.value = null
    }

    // Consolidated state
    data class VersusState(
        val player1: Character? = null,
        val player2: Character? = null,
        val allCharacters: List<Character> = emptyList(),
        val comparisonResult: ComparisonResult? = null
    )

    // Punisher calculation result
    data class PunisherResult(
        val targetMove: Move?,
        val onBlock: Int?,
        val punishers: List<Move>,
        val defender: Character
    )
}

data class ComparisonResult(
    val p1: Character,
    val p2: Character,
    val statsDiff: Map<String, Int>
)
