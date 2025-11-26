package wiki.tk.fistarium.features.characters.domain

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: String,
    val name: String,
    val description: String,
    val story: String? = null,
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(), // Multiple images gallery
    val stats: Map<String, Int> = emptyMap(), // e.g., "health" to 100, "attack" to 80, etc.
    val fightingStyle: String? = null, // e.g., "Mishima Style Karate"
    val country: String? = null,
    val difficulty: String? = null, // Easy, Medium, Hard, Very Hard
    val moveList: List<Move> = emptyList(),
    val combos: List<Combo> = emptyList(),
    val frameData: Map<String, FrameDataEntry> = emptyMap(), // Move name -> frame data
    val translations: Map<String, CharacterTranslation> = emptyMap(), // Language code -> translation
    val createdBy: String? = null, // User ID who created this character
    val createdAt: Long = System.currentTimeMillis(),
    val updatedBy: String? = null, // User ID who last updated
    val updatedAt: Long = System.currentTimeMillis(),
    val isOfficial: Boolean = true, // Official vs user-created
    val version: Int = 1, // For version control
    val isFavorite: Boolean = false, // Local favorite flag
    val weakSide: String? = null, // e.g., "SSL" (Sidestep Left), "SSR", "SWL", "SWR"
    val games: List<String> = listOf("TK8") // List of games this character appears in (e.g., "TK1", "TK2", "TK8")
) {
    fun getLocalizedName(languageCode: String): String {
        return translations[languageCode]?.name ?: name
    }

    fun getLocalizedDescription(languageCode: String): String {
        return translations[languageCode]?.description ?: description
    }

    fun getLocalizedStory(languageCode: String): String {
        return translations[languageCode]?.story ?: story ?: ""
    }

    fun getLocalizedFightingStyle(languageCode: String): String? {
        return translations[languageCode]?.fightingStyle ?: fightingStyle
    }

    fun getLocalizedCountry(languageCode: String): String? {
        return translations[languageCode]?.country ?: country
    }

    fun getLocalizedDifficulty(languageCode: String): String? {
        return translations[languageCode]?.difficulty ?: difficulty
    }
}

@Serializable
data class Move(
    val id: String,
    val name: String,
    val command: String, // e.g., "1,2" or "f+2"
    val damage: String? = null,
    val hitLevel: String? = null, // High, Mid, Low, Special Mid
    val notes: String? = null
)

@Serializable
data class Combo(
    val id: String,
    val name: String,
    val commands: String, // Sequence of moves
    val damage: String? = null,
    val difficulty: String? = null,
    val situation: String? = null, // Launcher, Wall, Counter Hit, etc.
    val videoUrl: String? = null,
    val createdBy: String? = null
)

@Serializable
data class FrameDataEntry(
    val startup: Int? = null,
    val onBlock: Int? = null,
    val onHit: Int? = null,
    val onCounterHit: Int? = null,
    val notes: String? = null
)

@Serializable
data class CharacterTranslation(
    val languageCode: String,
    val name: String,
    val description: String,
    val story: String? = null,
    val fightingStyle: String? = null,
    val country: String? = null,
    val difficulty: String? = null,
    val translatedBy: String? = null,
    val translatedAt: Long = System.currentTimeMillis()
)