package wiki.tk.fistarium.features.versus.domain

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.FrameDataEntry
import wiki.tk.fistarium.features.characters.domain.Move

class VersusUseCaseTest {

    private lateinit var versusUseCase: VersusUseCase

    // Test data
    private val testMoves = listOf(
        Move(id = "move1", name = "1 Jab", command = "1", damage = "10", hitLevel = "High"),
        Move(id = "move2", name = "Magic 4", command = "4", damage = "17", hitLevel = "High"),
        Move(id = "move3", name = "Demon Paw", command = "f+4", damage = "20", hitLevel = "Mid"),
        Move(id = "move4", name = "EWGF", command = "f,n,d,df+2", damage = "50", hitLevel = "High")
    )

    private val testFrameData = mapOf(
        "move1" to FrameDataEntry(startup = 10, onBlock = 1, onHit = 8, onCounterHit = 8),
        "move2" to FrameDataEntry(startup = 11, onBlock = -9, onHit = 4, onCounterHit = 20),
        "move3" to FrameDataEntry(startup = 16, onBlock = -12, onHit = 6, onCounterHit = 6),
        "move4" to FrameDataEntry(startup = 14, onBlock = -10, onHit = 15, onCounterHit = 15)
    )

    private val punisherMoves = listOf(
        Move(id = "punish1", name = "1,2", command = "1,2", damage = "25", hitLevel = "High,High"),
        Move(id = "punish2", name = "f+2,4", command = "f+2,4", damage = "35", hitLevel = "Mid,Mid"),
        Move(id = "punish3", name = "WS 2,2", command = "WS 2,2", damage = "40", hitLevel = "Mid,Mid")
    )

    private val punisherFrameData = mapOf(
        "punish1" to FrameDataEntry(startup = 10, onBlock = -1, onHit = 5, onCounterHit = 5),
        "punish2" to FrameDataEntry(startup = 12, onBlock = -7, onHit = 6, onCounterHit = 6),
        "punish3" to FrameDataEntry(startup = 15, onBlock = -5, onHit = 10, onCounterHit = 10)
    )

    private val attacker = Character(
        id = "attacker",
        name = "Kazuya",
        description = "Devil",
        moveList = testMoves,
        frameData = testFrameData
    )

    private val defender = Character(
        id = "defender",
        name = "Jin",
        description = "Protagonist",
        moveList = punisherMoves,
        frameData = punisherFrameData
    )

    @Before
    fun setup() {
        versusUseCase = VersusUseCase()
    }

    // ==================== Compare Stats Tests ====================

    @Test
    fun `compareStats returns difference for matching keys`() {
        val stats1 = mapOf("power" to 80, "speed" to 60)
        val stats2 = mapOf("power" to 70, "speed" to 75)

        val result = versusUseCase.compareStats(stats1, stats2)

        assertEquals(10, result["power"])  // 80 - 70
        assertEquals(-15, result["speed"]) // 60 - 75
    }

    @Test
    fun `compareStats handles missing keys in first map`() {
        val stats1 = mapOf("power" to 80)
        val stats2 = mapOf("power" to 70, "speed" to 75)

        val result = versusUseCase.compareStats(stats1, stats2)

        assertEquals(10, result["power"])
        assertEquals(-75, result["speed"]) // 0 - 75
    }

    @Test
    fun `compareStats handles missing keys in second map`() {
        val stats1 = mapOf("power" to 80, "range" to 90)
        val stats2 = mapOf("power" to 70)

        val result = versusUseCase.compareStats(stats1, stats2)

        assertEquals(10, result["power"])
        assertEquals(90, result["range"]) // 90 - 0
    }

    @Test
    fun `compareStats returns empty map for empty inputs`() {
        val result = versusUseCase.compareStats(emptyMap(), emptyMap())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `compareStats handles equal values`() {
        val stats1 = mapOf("power" to 50)
        val stats2 = mapOf("power" to 50)

        val result = versusUseCase.compareStats(stats1, stats2)

        assertEquals(0, result["power"])
    }

    @Test
    fun `compareStats combines all keys from both maps`() {
        val stats1 = mapOf("power" to 80, "technique" to 70)
        val stats2 = mapOf("speed" to 60, "range" to 50)

        val result = versusUseCase.compareStats(stats1, stats2)

        assertEquals(4, result.size)
        assertTrue(result.containsKey("power"))
        assertTrue(result.containsKey("technique"))
        assertTrue(result.containsKey("speed"))
        assertTrue(result.containsKey("range"))
    }

    // ==================== Find Punishers Tests ====================

    @Test
    fun `findPunishers returns empty for safe move`() {
        // move1 is +1 on block (safe)
        val result = versusUseCase.findPunishers("move1", attacker, defender)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findPunishers returns empty for plus move`() {
        // move1 is +1 on block
        val result = versusUseCase.findPunishers("move1", attacker, defender)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findPunishers returns moves for minus 10 move`() {
        // move4 (EWGF) is -10 on block, so i10 punisher should work
        val result = versusUseCase.findPunishers("move4", attacker, defender)

        assertTrue(result.isNotEmpty())
        assertTrue(result.any { it.id == "punish1" }) // i10 punisher
    }

    @Test
    fun `findPunishers returns only fast enough moves`() {
        // move2 (Magic 4) is -9 on block
        val result = versusUseCase.findPunishers("move2", attacker, defender)

        // No punishers should be returned since no move is i9 or faster
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findPunishers returns multiple punishers for very negative move`() {
        // move3 (Demon Paw) is -12 on block
        val result = versusUseCase.findPunishers("move3", attacker, defender)

        assertTrue(result.size >= 2) // i10 and i12 punishers should work
        assertTrue(result.any { it.id == "punish1" }) // i10
        assertTrue(result.any { it.id == "punish2" }) // i12
    }

    @Test
    fun `findPunishers sorts by damage descending`() {
        // move3 (Demon Paw) is -12 on block
        val result = versusUseCase.findPunishers("move3", attacker, defender)

        if (result.size >= 2) {
            val firstDamage = result[0].damage?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
            val secondDamage = result[1].damage?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
            assertTrue(firstDamage >= secondDamage)
        }
    }

    @Test
    fun `findPunishers returns empty for nonexistent move`() {
        val result = versusUseCase.findPunishers("nonexistent", attacker, defender)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findPunishers returns empty when frame data has no onBlock`() {
        val attackerNoBlockData = attacker.copy(
            frameData = mapOf("move1" to FrameDataEntry(startup = 10, onBlock = null))
        )

        val result = versusUseCase.findPunishers("move1", attackerNoBlockData, defender)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findPunishers handles defender with no frame data`() {
        val defenderNoFrameData = defender.copy(frameData = emptyMap())

        val result = versusUseCase.findPunishers("move3", attacker, defenderNoFrameData)

        // Should return empty since we can't determine punisher startup frames
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findPunishers handles damage with non-numeric characters`() {
        val defenderWithComplexDamage = defender.copy(
            moveList = listOf(
                Move(id = "complex1", name = "Combo", command = "1,2,3", damage = "10+15+20", hitLevel = "High"),
                Move(id = "complex2", name = "Single", command = "2", damage = "25", hitLevel = "Mid")
            ),
            frameData = mapOf(
                "complex1" to FrameDataEntry(startup = 10),
                "complex2" to FrameDataEntry(startup = 12)
            )
        )

        val result = versusUseCase.findPunishers("move3", attacker, defenderWithComplexDamage)

        // Should not crash and should sort correctly
        assertNotNull(result)
    }

    @Test
    fun `findPunishers handles null damage values`() {
        val defenderNullDamage = defender.copy(
            moveList = listOf(
                Move(id = "nodmg1", name = "No Damage", command = "1", damage = null),
                Move(id = "nodmg2", name = "With Damage", command = "2", damage = "30")
            ),
            frameData = mapOf(
                "nodmg1" to FrameDataEntry(startup = 10),
                "nodmg2" to FrameDataEntry(startup = 12)
            )
        )

        val result = versusUseCase.findPunishers("move3", attacker, defenderNullDamage)

        // Should handle null damage without crashing
        assertNotNull(result)
    }
}
