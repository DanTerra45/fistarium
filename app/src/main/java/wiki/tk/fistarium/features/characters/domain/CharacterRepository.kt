package wiki.tk.fistarium.features.characters.domain

import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(): Flow<List<Character>>
    fun getCharacterById(id: String): Flow<Character?>
    suspend fun syncCharactersFromRemote(): Result<Unit>
}