package wiki.tk.fistarium.features.characters.domain

import kotlinx.coroutines.flow.Flow

class CharacterUseCase(private val characterRepository: CharacterRepository) {

    fun getCharacters(): Flow<List<Character>> {
        return characterRepository.getCharacters()
    }

    fun getCharacterById(id: String): Flow<Character?> {
        return characterRepository.getCharacterById(id)
    }

    suspend fun syncCharacters(): Result<Unit> {
        return characterRepository.syncCharactersFromRemote()
    }
}