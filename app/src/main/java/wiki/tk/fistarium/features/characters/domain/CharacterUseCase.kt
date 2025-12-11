package wiki.tk.fistarium.features.characters.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class CharacterUseCase(private val characterRepository: CharacterRepository) {

    /**
     * Generate a unique ID for a new character.
     * ID generation belongs in the domain layer, not in the UI.
     */
    fun generateCharacterId(): String = UUID.randomUUID().toString()

    fun getCharacters(): Flow<List<Character>> {
        return characterRepository.getCharacters()
    }

    fun getCharacterById(id: String): Flow<Character?> {
        return characterRepository.getCharacterById(id)
    }

    fun getFavoriteCharacters(): Flow<List<Character>> {
        return characterRepository.getFavoriteCharacters()
    }

    suspend fun searchCharacters(query: String): List<Character> {
        return characterRepository.searchCharacters(query)
    }

    suspend fun syncCharacters(): Result<Unit> {
        return characterRepository.syncCharactersFromRemote()
    }

    suspend fun createCharacter(character: Character, userId: String): Result<String> {
        val newCharacter = character.copy(
            createdBy = userId,
            createdAt = System.currentTimeMillis(),
            updatedBy = userId,
            updatedAt = System.currentTimeMillis(),
            isOfficial = false,
            version = 1
        )
        return characterRepository.createCharacter(newCharacter)
    }

    suspend fun updateCharacter(character: Character, userId: String): Result<Unit> {
        val existingCharacter = characterRepository.getCharacterById(character.id).first()
        
        // If character exists, preserve creation info
        val createdBy = existingCharacter?.createdBy ?: userId
        val createdAt = existingCharacter?.createdAt ?: System.currentTimeMillis()
        
        val updatedCharacter = character.copy(
            createdBy = createdBy,
            createdAt = createdAt,
            updatedBy = userId,
            updatedAt = System.currentTimeMillis(),
            version = character.version + 1
        )
        return characterRepository.updateCharacter(updatedCharacter)
    }

    suspend fun deleteCharacter(characterId: String, userId: String, isAdmin: Boolean): Result<Unit> {
        val character = characterRepository.getCharacterById(characterId).first()
            ?: return Result.failure(Exception("Character not found"))
            
        if (character.createdBy != userId && !isAdmin) {
            return Result.failure(Exception("Permission denied: You can only delete your own characters"))
        }
        
        return characterRepository.deleteCharacter(characterId)
    }

    suspend fun toggleFavorite(characterId: String, isFavorite: Boolean): Result<Unit> {
        return characterRepository.toggleFavorite(characterId, isFavorite)
    }
}