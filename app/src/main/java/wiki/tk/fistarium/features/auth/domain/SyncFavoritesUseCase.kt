package wiki.tk.fistarium.features.auth.domain

import wiki.tk.fistarium.features.characters.domain.CharacterRepository

/**
 * Use case for syncing user favorites between local and remote storage.
 * This bridges the auth and characters features without creating direct dependencies
 * between their ViewModels.
 */
class SyncFavoritesUseCase(
    private val characterRepository: CharacterRepository
) {
    /**
     * Syncs favorites from remote to local and vice versa for the given user.
     * This should be called after successful login.
     */
    suspend fun syncUserFavorites(userId: String): Result<Unit> {
        return characterRepository.syncUserFavorites(userId)
    }

    /**
     * Clears all local favorites.
     * This should be called after logout.
     */
    suspend fun clearFavorites(): Result<Unit> {
        return characterRepository.clearFavorites()
    }
}
