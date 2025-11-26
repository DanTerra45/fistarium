package wiki.tk.fistarium.features.characters.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import timber.log.Timber
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterTranslation
import wiki.tk.fistarium.features.characters.domain.Combo
import wiki.tk.fistarium.features.characters.domain.FrameDataEntry
import wiki.tk.fistarium.features.characters.domain.Move

class CharacterRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) {

    private companion object {
        const val TAG = "CharacterRemoteDS"
        const val COLLECTION_CHARACTERS = "characters"
    }

    suspend fun fetchCharacters(): Result<List<Character>> {
        Timber.tag(TAG)
            .d("fetchCharacters: Starting Firestore query, currentUser=${FirebaseAuth.getInstance().currentUser?.uid}")
        return try {
            val snapshot = firestore.collection(COLLECTION_CHARACTERS).get().await()
            val characters = snapshot.documents.mapNotNull { doc ->
                doc.toCharacter()
            }
            Timber.tag(TAG).d("fetchCharacters: Success, fetched ${characters.size} characters")
            Result.success(characters)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "fetchCharacters: Error occurred")
            Result.failure(e)
        }
    }

    suspend fun fetchCharacterById(id: String): Result<Character?> {
        Timber.tag(TAG)
            .d("fetchCharacterById: Starting query for id=$id, currentUser=${FirebaseAuth.getInstance().currentUser?.uid}")
        return try {
            val doc = firestore.collection(COLLECTION_CHARACTERS).document(id).get().await()
            val character = doc.toCharacter()
            Timber.tag(TAG).d("fetchCharacterById: Success, character=${character?.name ?: "null"}")
            Result.success(character)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "fetchCharacterById: Error occurred for id=$id")
            Result.failure(e)
        }
    }

    suspend fun saveCharacter(character: Character): Result<Unit> {
        return try {
            val data = character.toFirestoreMap()
            firestore.collection(COLLECTION_CHARACTERS)
                .document(character.id)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCharacter(character: Character): Result<Unit> {
        return try {
            val data = character.toFirestoreMap()
            firestore.collection(COLLECTION_CHARACTERS)
                .document(character.id)
                .update(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCharacter(characterId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_CHARACTERS)
                .document(characterId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFavorites(userId: String): Result<List<String>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()
            val favoriteIds = snapshot.documents.map { it.id }
            Result.success(favoriteIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserFavorite(userId: String, characterId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            val favoritesRef = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(characterId)
            
            if (isFavorite) {
                favoritesRef.set(mapOf("timestamp" to com.google.firebase.Timestamp.now())).await()
            } else {
                favoritesRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCharacter(): Character? {
        val data = this.data ?: return null
        return try {
            Character(
                id = this.id,
                name = data["name"] as? String ?: return null,
                description = data["description"] as? String ?: "",
                story = data["story"] as? String,
                imageUrl = data["imageUrl"] as? String,
                imageUrls = (data["imageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                stats = (data["stats"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { (it.value as? Number)?.toInt() ?: 0 } ?: emptyMap(),
                fightingStyle = data["fightingStyle"] as? String,
                country = data["country"] as? String,
                difficulty = data["difficulty"] as? String,
                games = (data["games"] as? List<*>)?.mapNotNull { it as? String } ?: listOf("TK8"),
                moveList = json.decodeFromString<List<Move>>(data["moveList"] as? String ?: "[]"),
                combos = json.decodeFromString<List<Combo>>(data["combos"] as? String ?: "[]"),
                frameData = json.decodeFromString<Map<String, FrameDataEntry>>(data["frameData"] as? String ?: "{}"),
                translations = json.decodeFromString<Map<String, CharacterTranslation>>(data["translations"] as? String ?: "{}"),
                createdBy = data["createdBy"] as? String,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedBy = data["updatedBy"] as? String,
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isOfficial = (data["isOfficial"] as? Boolean) ?: false,
                version = (data["version"] as? Number)?.toInt() ?: 1,
                isFavorite = false // This is a local-only field
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun Character.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "description" to description,
            "story" to story,
            "imageUrl" to imageUrl,
            "imageUrls" to imageUrls,
            "stats" to stats,
            "fightingStyle" to fightingStyle,
            "country" to country,
            "difficulty" to difficulty,
            "games" to games,
            "moveList" to json.encodeToString(moveList),
            "combos" to json.encodeToString(combos),
            "frameData" to json.encodeToString(frameData),
            "translations" to json.encodeToString(translations),
            "createdBy" to createdBy,
            "createdAt" to createdAt,
            "updatedBy" to updatedBy,
            "updatedAt" to updatedAt,
            "isOfficial" to isOfficial,
            "version" to version
        )
    }
}
