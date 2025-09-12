package wiki.tk.fistarium.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val statsJson: String // Store stats as JSON string
)