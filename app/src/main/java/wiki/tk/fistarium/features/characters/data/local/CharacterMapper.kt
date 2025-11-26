package wiki.tk.fistarium.features.characters.data.local

import kotlinx.serialization.json.Json
import wiki.tk.fistarium.features.characters.data.local.entity.CharacterEntity
import wiki.tk.fistarium.features.characters.domain.*

class CharacterMapper(private val json: Json) {

    fun toDomain(entity: CharacterEntity): Character {
        return Character(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            imageUrl = entity.imageUrl,
            imageUrls = entity.imageUrlsJson?.let { 
                json.decodeFromString<List<String>>(it) 
            } ?: emptyList(),
            stats = json.decodeFromString<Map<String, Int>>(entity.statsJson),
            fightingStyle = entity.fightingStyle,
            country = entity.country,
            difficulty = entity.difficulty,
            moveList = entity.moveListJson?.let {
                json.decodeFromString<List<Move>>(it)
            } ?: emptyList(),
            combos = entity.combosJson?.let {
                json.decodeFromString<List<Combo>>(it)
            } ?: emptyList(),
            frameData = entity.frameDataJson?.let {
                json.decodeFromString<Map<String, FrameDataEntry>>(it)
            } ?: emptyMap(),
            translations = entity.translationsJson?.let {
                json.decodeFromString<Map<String, CharacterTranslation>>(it)
            } ?: emptyMap(),
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            updatedBy = entity.updatedBy,
            updatedAt = entity.updatedAt,
            isOfficial = entity.isOfficial,
            version = entity.version,
            isFavorite = entity.isFavorite,
            games = entity.gamesJson?.let {
                json.decodeFromString<List<String>>(it)
            } ?: listOf("TK8")
        )
    }

    fun toEntity(character: Character): CharacterEntity {
        return CharacterEntity(
            id = character.id,
            name = character.name,
            description = character.description,
            imageUrl = character.imageUrl,
            imageUrlsJson = if (character.imageUrls.isNotEmpty()) json.encodeToString(character.imageUrls) else null,
            statsJson = json.encodeToString(character.stats),
            fightingStyle = character.fightingStyle,
            country = character.country,
            difficulty = character.difficulty,
            moveListJson = if (character.moveList.isNotEmpty()) json.encodeToString(character.moveList) else null,
            combosJson = if (character.combos.isNotEmpty()) json.encodeToString(character.combos) else null,
            frameDataJson = if (character.frameData.isNotEmpty()) json.encodeToString(character.frameData) else null,
            translationsJson = if (character.translations.isNotEmpty()) json.encodeToString(character.translations) else null,
            createdBy = character.createdBy,
            createdAt = character.createdAt,
            updatedBy = character.updatedBy,
            updatedAt = character.updatedAt,
            isOfficial = character.isOfficial,
            version = character.version,
            isFavorite = character.isFavorite,
            gamesJson = json.encodeToString(character.games)
        )
    }

    fun toDomainList(entities: List<CharacterEntity>): List<Character> {
        return entities.map { toDomain(it) }
    }

    fun toEntityList(characters: List<Character>): List<CharacterEntity> {
        return characters.map { toEntity(it) }
    }
}
