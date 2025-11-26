package wiki.tk.fistarium.data

import wiki.tk.fistarium.features.characters.domain.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataSeeder(private val repository: CharacterRepository) {
    
    /**
     * Seeds legacy character data to the repository.
     * This is a suspend function that should be called from a ViewModel's coroutine scope.
     */
    suspend fun seedLegacyData() {
        withContext(Dispatchers.IO) {
            val allLegacyCharacters = listOf(
                tekken1Characters,
                tekken2Characters,
                tekken3Characters,
                tekken4Characters,
                tekken5Characters,
                tekken5DarkResurrectionNewCharacters,
                tekken6Characters,
                tekken7Characters,
                tekken8Characters
            ).flatten()

            allLegacyCharacters.forEach { character ->
                repository.createCharacter(character)
            }
        }
    }
}
