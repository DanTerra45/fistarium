package wiki.tk.fistarium.features.characters.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import wiki.tk.fistarium.core.utils.RetryUtils
import wiki.tk.fistarium.features.characters.data.local.CharacterLocalDataSource
import wiki.tk.fistarium.features.characters.data.remote.CharacterRemoteDataSource
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterRepository
import kotlinx.coroutines.flow.Flow

class CharacterRepositoryImpl(
    private val localDataSource: CharacterLocalDataSource,
    private val remoteDataSource: CharacterRemoteDataSource,
    private val firebaseAuth: FirebaseAuth
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
        return RetryUtils.withRetryResult {
            val remoteCharacters = remoteDataSource.fetchCharacters().getOrThrow()
            localDataSource.saveCharacters(remoteCharacters)
        }
    }

    override suspend fun createCharacter(character: Character): Result<String> {
        // Always save locally first (offline-first)
        localDataSource.saveCharacter(character)
        
        return try {
            RetryUtils.withRetry {
                remoteDataSource.saveCharacter(character).getOrThrow()
            }
            Result.success(character.id)
        } catch (e: Exception) {
            // Local save succeeded, remote failed - return success
            // Character will sync later when online
            Result.success(character.id)
        }
    }

    override suspend fun updateCharacter(character: Character): Result<Unit> {
        // Always update locally first (offline-first)
        localDataSource.saveCharacter(character)
        
        return try {
            RetryUtils.withRetry {
                remoteDataSource.updateCharacter(character).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Local update succeeded, remote failed
            Result.success(Unit)
        }
    }

    override suspend fun deleteCharacter(characterId: String): Result<Unit> {
        // Delete locally first (offline-first consistency)
        localDataSource.deleteCharacter(characterId)
        
        return try {
            RetryUtils.withRetry {
                remoteDataSource.deleteCharacter(characterId).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun toggleFavorite(characterId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            // Update locally first
            localDataSource.updateFavoriteStatus(characterId, isFavorite)
            
            // Then try remote with retry
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                try {
                    RetryUtils.withRetry(times = 2) {
                        remoteDataSource.updateUserFavorite(userId, characterId, isFavorite)
                    }
                } catch (e: Exception) {
                    // Remote failed but local succeeded - acceptable for favorites
                }
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
        return RetryUtils.withRetryResult {
            // 1. Get local favorites (Guest favorites)
            val localFavorites = localDataSource.getFavoriteCharacters().first().map { it.id }
            
            // 2. Get remote favorites
            val remoteFavorites = remoteDataSource.getUserFavorites(userId).getOrThrow()
            
            // 3. Merge (Union)
            val allFavorites = (localFavorites + remoteFavorites).distinct()
            
            // 4. Update Remote (Push local favorites to account)
            val missingInRemote = localFavorites - remoteFavorites.toSet()
            missingInRemote.forEach { id ->
                try {
                    remoteDataSource.updateUserFavorite(userId, id, true)
                } catch (e: Exception) {
                    // Continue with others even if one fails
                }
            }
            
            // 5. Update Local (Pull remote favorites to device)
            allFavorites.forEach { characterId ->
                localDataSource.updateFavoriteStatus(characterId, true)
            }
        }
    }
}