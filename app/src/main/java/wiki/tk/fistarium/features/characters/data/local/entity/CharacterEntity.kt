package wiki.tk.fistarium.features.characters.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val imageUrlsJson: String? = null, // JSON array of image URLs
    val statsJson: String, // Store stats as JSON string
    val fightingStyle: String? = null,
    val country: String? = null,
    val difficulty: String? = null,
    val moveListJson: String? = null, // JSON array of moves
    val combosJson: String? = null, // JSON array of combos
    val frameDataJson: String? = null, // JSON map of frame data
    val translationsJson: String? = null, // JSON map of translations
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedBy: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isOfficial: Boolean = true,
    val version: Int = 1,
    val isFavorite: Boolean = false,
    val gamesJson: String? = null // JSON array of games
)