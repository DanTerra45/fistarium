package wiki.tk.fistarium.features.versus.domain

import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.Move

class VersusUseCase {

    companion object {
        // Pre-computed regex for damage parsing (LOW priority optimization)
        private val DAMAGE_NUMBER_REGEX = Regex("[^0-9]")
    }

    fun compareStats(stats1: Map<String, Int>, stats2: Map<String, Int>): Map<String, Int> {
        val allKeys = stats1.keys + stats2.keys
        return allKeys.associateWith { key ->
            (stats1[key] ?: 0) - (stats2[key] ?: 0)
        }
    }

    fun findPunishers(targetMoveId: String, attacker: Character, defender: Character): List<Move> {
        val frameData = attacker.frameData[targetMoveId] ?: return emptyList()
        val blockFrames = frameData.onBlock ?: return emptyList()
        
        if (blockFrames >= 0) return emptyList() // Safe or plus on block

        val punishFrames = -blockFrames
        return defender.moveList.filter { move ->
            val moveFrameData = defender.frameData[move.id]
            val startup = moveFrameData?.startup
            startup != null && startup <= punishFrames
        }.sortedByDescending { 
            it.damage?.replace(DAMAGE_NUMBER_REGEX, "")?.toIntOrNull() ?: 0 
        }
    }
}
