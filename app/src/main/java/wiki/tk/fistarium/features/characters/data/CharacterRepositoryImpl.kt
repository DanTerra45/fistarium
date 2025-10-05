package wiki.tk.fistarium.features.characters.data

import wiki.tk.fistarium.features.characters.data.local.CharacterLocalDataSource
import wiki.tk.fistarium.features.characters.data.remote.CharacterRemoteDataSource
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterRepository
import kotlinx.coroutines.flow.Flow

class CharacterRepositoryImpl(
    private val localDataSource: CharacterLocalDataSource,
    private val remoteDataSource: CharacterRemoteDataSource
) : CharacterRepository {

    override fun getCharacters(): Flow<List<Character>> {
        return localDataSource.getCharacters()
    }

    override fun getCharacterById(id: String): Flow<Character?> {
        return localDataSource.getCharacterById(id)
    }

    override suspend fun syncCharactersFromRemote(): Result<Unit> {
        return try {
            val remoteCharacters = remoteDataSource.fetchCharacters().getOrThrow()
            localDataSource.saveCharacters(remoteCharacters)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}