package wiki.tk.fistarium.domain.repository

import wiki.tk.fistarium.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(): Flow<List<Character>>
    fun getCharacterById(id: String): Flow<Character?>
    suspend fun syncCharactersFromRemote(): Result<Unit>
}