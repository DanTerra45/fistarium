package wiki.tk.fistarium.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import wiki.tk.fistarium.data.local.dao.CharacterDao
import wiki.tk.fistarium.data.local.entity.CharacterEntity
import wiki.tk.fistarium.domain.model.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterLocalDataSource(
    private val characterDao: CharacterDao,
    private val gson: Gson = Gson()
) {

    fun getCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getCharacterById(id: String): Flow<Character?> {
        return characterDao.getCharacterById(id).map { it?.toDomain() }
    }

    suspend fun saveCharacters(characters: List<CharacterEntity>) {
        characterDao.insertCharacters(characters)
    }

    suspend fun saveCharacter(character: CharacterEntity) {
        characterDao.insertCharacter(character)
    }

    private fun CharacterEntity.toDomain(): Character {
        val stats = try {
            val statsMap: Map<String, Any> = gson.fromJson(statsJson, object : TypeToken<Map<String, Any>>() {}.type)
            statsMap.mapValues { (_, value) -> (value as? Number)?.toInt() ?: 0 }
        } catch (_: Exception) {
            emptyMap()
        }
        return Character(
            id = id,
            name = name,
            description = description,
            imageUrl = imageUrl,
            stats = stats
        )
    }
}