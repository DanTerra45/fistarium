package wiki.tk.fistarium.features.characters.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import wiki.tk.fistarium.features.characters.data.local.CharacterLocalDataSource
import wiki.tk.fistarium.features.characters.data.remote.CharacterRemoteDataSource
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterRepository
import kotlinx.coroutines.flow.Flow

class CharacterRepositoryImpl(
    private val localDataSource: CharacterLocalDataSource,
    private val remoteDataSource: CharacterRemoteDataSource,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : CharacterRepository {

    override fun getCharacters(): Flow<List<Character>> {
        return localDataSource.getCharacters()
    }

    override fun getCharacterById(id: String): Flow<Character?> {
        return localDataSource.getCharacterById(id)
    }

    override fun getFavoriteCharacters(): Flow<List<Character>> {
        return localDataSource.getFavoriteCharacters()
    }

    override suspend fun searchCharacters(query: String): List<Character> {
        return localDataSource.searchCharacters(query)
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

    override suspend fun createCharacter(character: Character): Result<String> {
        return try {
            // Save to remote first
            remoteDataSource.saveCharacter(character).getOrThrow()
            // Then save to local
            localDataSource.saveCharacter(character)
            Result.success(character.id)
        } catch (e: Exception) {
            // If remote fails, still save locally
            localDataSource.saveCharacter(character)
            Result.failure(e)
        }
    }

    override suspend fun updateCharacter(character: Character): Result<Unit> {
        return try {
            // Update remote first
            remoteDataSource.updateCharacter(character).getOrThrow()
            // Then update local
            localDataSource.saveCharacter(character)
            Result.success(Unit)
        } catch (e: Exception) {
            // If remote fails, still update locally
            localDataSource.saveCharacter(character)
            Result.failure(e)
        }
    }

    override suspend fun deleteCharacter(characterId: String): Result<Unit> {
        return try {
            // Delete from remote first
            remoteDataSource.deleteCharacter(characterId).getOrThrow()
            // Then delete from local
            localDataSource.deleteCharacter(characterId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(characterId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            localDataSource.updateFavoriteStatus(characterId, isFavorite)
            
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                remoteDataSource.updateUserFavorite(userId, characterId, isFavorite)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearFavorites(): Result<Unit> {
        return try {
            localDataSource.clearAllFavorites()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUserFavorites(userId: String): Result<Unit> {
        return try {
            // 1. Get local favorites (Guest favorites)
            val localFavorites = localDataSource.getFavoriteCharacters().first().map { it.id }
            
            // 2. Get remote favorites
            val remoteFavorites = remoteDataSource.getUserFavorites(userId).getOrThrow()
            
            // 3. Merge (Union)
            val allFavorites = (localFavorites + remoteFavorites).distinct()
            
            // 4. Update Remote (Push local favorites to account)
            val missingInRemote = localFavorites - remoteFavorites.toSet()
            missingInRemote.forEach { id ->
                remoteDataSource.updateUserFavorite(userId, id, true)
            }
            
            // 5. Update Local (Pull remote favorites to device)
            allFavorites.forEach { characterId ->
                localDataSource.updateFavoriteStatus(characterId, true)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}