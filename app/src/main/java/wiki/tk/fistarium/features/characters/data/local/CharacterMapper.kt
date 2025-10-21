package wiki.tk.fistarium.features.characters.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import wiki.tk.fistarium.features.characters.data.local.entity.CharacterEntity
import wiki.tk.fistarium.features.characters.domain.*

/**
 * Mapper to convert between domain and data layer models
 */
class CharacterMapper(private val gson: Gson) {

    fun toDomain(entity: CharacterEntity): Character {
        return Character(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            imageUrl = entity.imageUrl,
            imageUrls = entity.imageUrlsJson?.let { 
                gson.fromJson(it, object : TypeToken<List<String>>() {}.type) 
            } ?: emptyList(),
            stats = gson.fromJson(entity.statsJson, object : TypeToken<Map<String, Int>>() {}.type),
            fightingStyle = entity.fightingStyle,
            country = entity.country,
            difficulty = entity.difficulty,
            moveList = entity.moveListJson?.let {
                gson.fromJson(it, object : TypeToken<List<Move>>() {}.type)
            } ?: emptyList(),
            combos = entity.combosJson?.let {
                gson.fromJson(it, object : TypeToken<List<Combo>>() {}.type)
            } ?: emptyList(),
            frameData = entity.frameDataJson?.let {
                gson.fromJson(it, object : TypeToken<Map<String, FrameDataEntry>>() {}.type)
            } ?: emptyMap(),
            translations = entity.translationsJson?.let {
                gson.fromJson(it, object : TypeToken<Map<String, CharacterTranslation>>() {}.type)
            } ?: emptyMap(),
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            updatedBy = entity.updatedBy,
            updatedAt = entity.updatedAt,
            isOfficial = entity.isOfficial,
            version = entity.version,
            isFavorite = entity.isFavorite
        )
    }

    fun toEntity(character: Character): CharacterEntity {
        return CharacterEntity(
            id = character.id,
            name = character.name,
            description = character.description,
            imageUrl = character.imageUrl,
            imageUrlsJson = if (character.imageUrls.isNotEmpty()) gson.toJson(character.imageUrls) else null,
            statsJson = gson.toJson(character.stats),
            fightingStyle = character.fightingStyle,
            country = character.country,
            difficulty = character.difficulty,
            moveListJson = if (character.moveList.isNotEmpty()) gson.toJson(character.moveList) else null,
            combosJson = if (character.combos.isNotEmpty()) gson.toJson(character.combos) else null,
            frameDataJson = if (character.frameData.isNotEmpty()) gson.toJson(character.frameData) else null,
            translationsJson = if (character.translations.isNotEmpty()) gson.toJson(character.translations) else null,
            createdBy = character.createdBy,
            createdAt = character.createdAt,
            updatedBy = character.updatedBy,
            updatedAt = character.updatedAt,
            isOfficial = character.isOfficial,
            version = character.version,
            isFavorite = character.isFavorite
        )
    }

    fun toDomainList(entities: List<CharacterEntity>): List<Character> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(characters: List<Character>): List<CharacterEntity> {
        return characters.map { toEntity(it) }
    }
}
