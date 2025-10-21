package wiki.tk.fistarium.features.characters.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import wiki.tk.fistarium.features.characters.domain.*

class CharacterRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val gson: Gson = Gson()
) {

    private companion object {
        const val TAG = "CharacterRemoteDS"
        const val COLLECTION_CHARACTERS = "characters"
    }

    suspend fun fetchCharacters(): Result<List<Character>> {
        Log.d(TAG, "fetchCharacters: Starting Firestore query, currentUser=${FirebaseAuth.getInstance().currentUser?.uid}")
        return try {
            val snapshot = firestore.collection(COLLECTION_CHARACTERS).get().await()
            val characters = snapshot.documents.mapNotNull { doc ->
                doc.toCharacter()
            }
            Log.d(TAG, "fetchCharacters: Success, fetched ${characters.size} characters")
            Result.success(characters)
        } catch (e: Exception) {
            Log.e(TAG, "fetchCharacters: Error occurred", e)
            // If permission denied and no authenticated user, try anonymous sign-in and retry once
            if (isPermissionDenied(e)) {
                Log.w(TAG, "fetchCharacters: PERMISSION_DENIED detected, attempting anonymous auth + retry")
                try {
                    ensureAnonymousAuth()
                    val snapshot = firestore.collection(COLLECTION_CHARACTERS).get().await()
                    val characters = snapshot.documents.mapNotNull { doc -> doc.toCharacter() }
                    Log.d(TAG, "fetchCharacters: Retry successful after auth, fetched ${characters.size} characters")
                    return Result.success(characters)
                } catch (ex: Exception) {
                    Log.e(TAG, "fetchCharacters: Retry failed after auth", ex)
                    return Result.failure(ex)
                }
            }
            Result.failure(e)
        }
    }

    suspend fun fetchCharacterById(id: String): Result<Character?> {
        Log.d(TAG, "fetchCharacterById: Starting query for id=$id, currentUser=${FirebaseAuth.getInstance().currentUser?.uid}")
        return try {
            val doc = firestore.collection(COLLECTION_CHARACTERS).document(id).get().await()
            val character = doc.toCharacter()
            Log.d(TAG, "fetchCharacterById: Success, character=${character?.name ?: "null"}")
            Result.success(character)
        } catch (e: Exception) {
            Log.e(TAG, "fetchCharacterById: Error occurred for id=$id", e)
            if (isPermissionDenied(e)) {
                Log.w(TAG, "fetchCharacterById: PERMISSION_DENIED detected, attempting anonymous auth + retry")
                try {
                    ensureAnonymousAuth()
                    val doc = firestore.collection(COLLECTION_CHARACTERS).document(id).get().await()
                    val character = doc.toCharacter()
                    Log.d(TAG, "fetchCharacterById: Retry successful after auth, character=${character?.name ?: "null"}")
                    return Result.success(character)
                } catch (ex: Exception) {
                    Log.e(TAG, "fetchCharacterById: Retry failed after auth", ex)
                    return Result.failure(ex)
                }
            }
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
            if (isPermissionDenied(e)) {
                try {
                    ensureAnonymousAuth()
                    val data = character.toFirestoreMap()
                    firestore.collection(COLLECTION_CHARACTERS)
                        .document(character.id)
                        .set(data)
                        .await()
                    return Result.success(Unit)
                } catch (ex: Exception) {
                    return Result.failure(ex)
                }
            }
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
            if (isPermissionDenied(e)) {
                try {
                    ensureAnonymousAuth()
                    val data = character.toFirestoreMap()
                    firestore.collection(COLLECTION_CHARACTERS)
                        .document(character.id)
                        .update(data)
                        .await()
                    return Result.success(Unit)
                } catch (ex: Exception) {
                    return Result.failure(ex)
                }
            }
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
            if (isPermissionDenied(e)) {
                try {
                    ensureAnonymousAuth()
                    firestore.collection(COLLECTION_CHARACTERS)
                        .document(characterId)
                        .delete()
                        .await()
                    return Result.success(Unit)
                } catch (ex: Exception) {
                    return Result.failure(ex)
                }
            }
            Result.failure(e)
        }
    }

    private fun isPermissionDenied(e: Exception): Boolean {
        val isDenied = when (e) {
            is FirebaseFirestoreException -> e.code == Code.PERMISSION_DENIED
            else -> e.message?.contains("PERMISSION_DENIED") == true
        }
        if (isDenied) {
            Log.w(TAG, "PERMISSION_DENIED detected: ${e.message}")
        }
        return isDenied
    }

    private suspend fun ensureAnonymousAuth() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            Log.d(TAG, "ensureAnonymousAuth: No user, signing in anonymously...")
            try {
                auth.signInAnonymously().await()
                Log.d(TAG, "ensureAnonymousAuth: Anonymous sign-in successful, UID=${auth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.e(TAG, "ensureAnonymousAuth: Anonymous sign-in failed", e)
                throw e
            }
        } else {
            Log.d(TAG, "✅ ensureAnonymousAuth: User already exists, UID=${currentUser.uid}, isAnonymous=${currentUser.isAnonymous}")
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCharacter(): Character? {
        val data = this.data ?: return null
        return try {
            Character(
                id = this.id,
                name = data["name"] as? String ?: return null,
                description = data["description"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String,
                imageUrls = (data["imageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                stats = (data["stats"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { (it.value as? Number)?.toInt() ?: 0 } ?: emptyMap(),
                fightingStyle = data["fightingStyle"] as? String,
                country = data["country"] as? String,
                difficulty = data["difficulty"] as? String,
                moveList = gson.fromJson(data["moveList"] as? String ?: "[]", object : TypeToken<List<Move>>() {}.type) ?: emptyList(),
                combos = gson.fromJson(data["combos"] as? String ?: "[]", object : TypeToken<List<Combo>>() {}.type) ?: emptyList(),
                frameData = gson.fromJson(data["frameData"] as? String ?: "{}", object : TypeToken<Map<String, FrameDataEntry>>() {}.type) ?: emptyMap(),
                translations = gson.fromJson(data["translations"] as? String ?: "{}", object : TypeToken<Map<String, CharacterTranslation>>() {}.type) ?: emptyMap(),
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
            "imageUrl" to imageUrl,
            "imageUrls" to imageUrls,
            "stats" to stats,
            "fightingStyle" to fightingStyle,
            "country" to country,
            "difficulty" to difficulty,
            "moveList" to gson.toJson(moveList),
            "combos" to gson.toJson(combos),
            "frameData" to gson.toJson(frameData),
            "translations" to gson.toJson(translations),
            "createdBy" to createdBy,
            "createdAt" to createdAt,
            "updatedBy" to updatedBy,
            "updatedAt" to updatedAt,
            "isOfficial" to isOfficial,
            "version" to version
        )
    }
}
