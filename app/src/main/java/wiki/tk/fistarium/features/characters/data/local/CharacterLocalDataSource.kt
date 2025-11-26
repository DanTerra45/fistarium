package wiki.tk.fistarium.features.characters.data.local

import wiki.tk.fistarium.features.characters.data.local.dao.CharacterDao
import wiki.tk.fistarium.features.characters.domain.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterLocalDataSource(
    private val characterDao: CharacterDao,
    private val mapper: CharacterMapper
) {

    fun getCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    fun getCharacterById(id: String): Flow<Character?> {
        return characterDao.getCharacterById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    fun getFavoriteCharacters(): Flow<List<Character>> {
        return characterDao.getFavoriteCharacters().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    suspend fun saveCharacters(characters: List<Character>) {
        // Preserve favorite status
        val currentFavorites = characterDao.getFavoriteCharactersSync().map { it.id }.toSet()
        
        val entities = mapper.toEntityList(characters).map { entity ->
            if (currentFavorites.contains(entity.id)) {
                entity.copy(isFavorite = true)
            } else {
                entity
            }
        }
        characterDao.insertCharacters(entities)
    }

    suspend fun saveCharacter(character: Character) {
        characterDao.insertCharacter(mapper.toEntity(character))
    }

    suspend fun updateFavoriteStatus(characterId: String, isFavorite: Boolean) {
        characterDao.updateFavoriteStatus(characterId, isFavorite)
    }

    suspend fun clearAllFavorites() {
        characterDao.clearAllFavorites()
    }

    suspend fun deleteCharacter(characterId: String) {
        characterDao.deleteCharacter(characterId)
    }

    suspend fun searchCharacters(query: String): List<Character> {
        return mapper.toDomainList(characterDao.searchCharacters("%$query%"))
    }
}