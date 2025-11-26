package wiki.tk.fistarium.features.characters.domain

import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(): Flow<List<Character>>
    fun getCharacterById(id: String): Flow<Character?>
    fun getFavoriteCharacters(): Flow<List<Character>>
    suspend fun searchCharacters(query: String): List<Character>
    suspend fun syncCharactersFromRemote(): Result<Unit>
    suspend fun createCharacter(character: Character): Result<String>
    suspend fun updateCharacter(character: Character): Result<Unit>
    suspend fun deleteCharacter(characterId: String): Result<Unit>
    suspend fun toggleFavorite(characterId: String, isFavorite: Boolean): Result<Unit>
    suspend fun clearFavorites(): Result<Unit>
    suspend fun syncUserFavorites(userId: String): Result<Unit>
}