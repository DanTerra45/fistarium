package wiki.tk.fistarium.features.characters.data.local

import com.google.gson.Gson
import wiki.tk.fistarium.features.characters.data.local.entity.CharacterEntity

class CharacterSeeder {

    private val gson = Gson()

    fun getInitialCharacters(): List<CharacterEntity> {
        return listOf(
            CharacterEntity(
                id = "1",
                name = "Kazuya Mishima",
                description = "The ruthless leader of the Mishima Zaibatsu, known for his devil gene.",
                imageUrl = null,
                statsJson = gson.toJson(mapOf("health" to 100, "attack" to 85, "defense" to 80))
            ),
            CharacterEntity(
                id = "2",
                name = "Jin Kazama",
                description = "Son of Kazuya and Jun, wielder of the Devil Gene, seeking to end the Mishima curse.",
                imageUrl = null,
                statsJson = gson.toJson(mapOf("health" to 95, "attack" to 90, "defense" to 75))
            ),
            CharacterEntity(
                id = "3",
                name = "King",
                description = "The noble wrestler masked as a jaguar, fighting for justice.",
                imageUrl = null,
                statsJson = gson.toJson(mapOf("health" to 110, "attack" to 80, "defense" to 90))
            )
            // Add more as needed
        )
    }
}