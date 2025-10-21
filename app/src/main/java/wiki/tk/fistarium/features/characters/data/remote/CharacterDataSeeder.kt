package wiki.tk.fistarium.features.characters.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import wiki.tk.fistarium.features.characters.domain.*

/**
 * Helper class to seed initial character data into Firestore.
 * For development/testing purposes only.
 */
class CharacterDataSeeder(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    companion object {
        private const val TAG = "CharacterDataSeeder"
        private const val COLLECTION_CHARACTERS = "characters"
    }
    
    /**
     * Seeds sample Tekken characters into Firestore.
     * @return Number of characters successfully added
     */
    suspend fun seedSampleCharacters(): Result<Int> {
        return try {
            val characters = getSampleCharacters()
            var successCount = 0
            
            Log.d(TAG, "Starting to seed ${characters.size} sample characters...")
            
            for (character in characters) {
                try {
                    val data = character.toFirestoreMap()
                    firestore.collection(COLLECTION_CHARACTERS)
                        .document(character.id)
                        .set(data)
                        .await()
                    
                    successCount++
                    Log.d(TAG, "Added character: ${character.name} (${character.id})")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add character: ${character.name}", e)
                }
            }
            
            Log.d(TAG, "Seeding complete! Added $successCount/${characters.size} characters")
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Seeding failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deletes all characters from Firestore.
     * Use with caution!
     */
    suspend fun clearAllCharacters(): Result<Int> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CHARACTERS).get().await()
            var deleteCount = 0
            
            Log.d(TAG, "Deleting ${snapshot.documents.size} characters...")
            
            for (doc in snapshot.documents) {
                try {
                    doc.reference.delete().await()
                    deleteCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete ${doc.id}", e)
                }
            }
            
            Log.d(TAG, "Deleted $deleteCount characters")
            Result.success(deleteCount)
        } catch (e: Exception) {
            Log.e(TAG, "Clear failed", e)
            Result.failure(e)
        }
    }
    
    private fun getSampleCharacters(): List<Character> {
        val currentTime = System.currentTimeMillis()
        
        return listOf(
            Character(
                id = "jin-kazama",
                name = "Jin Kazama",
                description = "The main protagonist of the Tekken series and the son of Kazuya Mishima and Jun Kazama. He fights to end the cursed Mishima bloodline.",
                imageUrl = null,
                imageUrls = emptyList(),
                stats = mapOf(
                    "power" to 85,
                    "speed" to 80,
                    "technique" to 90,
                    "range" to 75,
                    "ease_of_use" to 70
                ),
                fightingStyle = "Traditional Karate",
                country = "Japan",
                difficulty = "Medium",
                moveList = listOf(
                    Move(
                        id = "1",
                        name = "Demon's Paw",
                        command = "f,n,d,df+2",
                        damage = "25",
                        hitLevel = "Mid",
                        notes = "Electric Wind God Fist variant (launcher)"
                    ),
                    Move(
                        id = "2",
                        name = "Spinning Flare Kick",
                        command = "b+3",
                        damage = "20",
                        hitLevel = "Mid",
                        notes = "Good poke"
                    )
                ),
                combos = listOf(
                    Combo(
                        id = "1",
                        name = "Staple Combo",
                        commands = "EWGF, 4, bf+2,1,2, S! dash f+3~3, 2,1,4",
                        damage = "68",
                        difficulty = "Hard",
                        situation = "Electric Wind God Fist launcher"
                    )
                ),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = "system",
                createdAt = currentTime,
                updatedBy = "system",
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            
            Character(
                id = "kazuya-mishima",
                name = "Kazuya Mishima",
                description = "The anti-hero of the Tekken series. He seeks revenge against his father Heihachi and battles with the Devil Gene within him.",
                imageUrl = null,
                imageUrls = emptyList(),
                stats = mapOf(
                    "power" to 90,
                    "speed" to 75,
                    "technique" to 95,
                    "range" to 70,
                    "ease_of_use" to 65
                ),
                fightingStyle = "Mishima Style Fighting Karate",
                country = "Japan",
                difficulty = "Hard",
                moveList = listOf(
                    Move(
                        id = "1",
                        name = "Electric Wind God Fist",
                        command = "f,n,d,df+2",
                        damage = "25",
                        hitLevel = "Mid",
                        notes = "Frame 13 launcher, requires just frame input"
                    ),
                    Move(
                        id = "2",
                        name = "Demon Uppercut",
                        command = "f,n,d,df+1",
                        damage = "25",
                        hitLevel = "Mid",
                        notes = "Electric version is +5 on block (launcher)"
                    )
                ),
                combos = emptyList(),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = "system",
                createdAt = currentTime,
                updatedBy = "system",
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            
            Character(
                id = "nina-williams",
                name = "Nina Williams",
                description = "A cold-blooded Irish assassin who has been a staple of the series since the original Tekken. Known for her chain throws and pressure game.",
                imageUrl = null,
                imageUrls = emptyList(),
                stats = mapOf(
                    "power" to 75,
                    "speed" to 90,
                    "technique" to 85,
                    "range" to 70,
                    "ease_of_use" to 60
                ),
                fightingStyle = "Assassination Arts (Aikido-based)",
                country = "Ireland",
                difficulty = "Hard",
                moveList = listOf(
                    Move(
                        id = "1",
                        name = "Divine Cannon",
                        command = "d+4,1",
                        damage = "28",
                        hitLevel = "Low-Mid",
                        notes = "Good low poke into mid"
                    )
                ),
                combos = emptyList(),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = "system",
                createdAt = currentTime,
                updatedBy = "system",
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            
            Character(
                id = "paul-phoenix",
                name = "Paul Phoenix",
                description = "An American martial artist and one of the original Tekken characters. Known for his massive damage output and iconic hairstyle.",
                imageUrl = null,
                imageUrls = emptyList(),
                stats = mapOf(
                    "power" to 95,
                    "speed" to 70,
                    "technique" to 75,
                    "range" to 75,
                    "ease_of_use" to 85
                ),
                fightingStyle = "Judo-based Martial Arts",
                country = "USA",
                difficulty = "Easy",
                moveList = listOf(
                    Move(
                        id = "1",
                        name = "Deathfist",
                        command = "qcf+2",
                        damage = "40",
                        hitLevel = "Mid",
                        notes = "Iconic high-damage move"
                    ),
                    Move(
                        id = "2",
                        name = "Demolition Man",
                        command = "qcf+1",
                        damage = "30",
                        hitLevel = "Mid",
                        notes = "Great combo starter (launcher)"
                    )
                ),
                combos = emptyList(),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = "system",
                createdAt = currentTime,
                updatedBy = "system",
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            
            Character(
                id = "king",
                name = "King",
                description = "A masked Mexican wrestler who fights to support orphans. Famous for his extensive chain throw system.",
                imageUrl = null,
                imageUrls = emptyList(),
                stats = mapOf(
                    "power" to 85,
                    "speed" to 75,
                    "technique" to 80,
                    "range" to 80,
                    "ease_of_use" to 70
                ),
                fightingStyle = "Pro Wrestling",
                country = "Mexico",
                difficulty = "Medium",
                moveList = listOf(
                    Move(
                        id = "1",
                        name = "Giant Swing",
                        command = "f,hcf+1",
                        damage = "45",
                        hitLevel = "Throw",
                        notes = "Iconic throw with high damage"
                    ),
                    Move(
                        id = "2",
                        name = "Jaguar Step",
                        command = "f+2,1",
                        damage = "23",
                        hitLevel = "High-Mid",
                        notes = "Good pressure tool"
                    )
                ),
                combos = emptyList(),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = "system",
                createdAt = currentTime,
                updatedBy = "system",
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            )
        )
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
            "moveList" to com.google.gson.Gson().toJson(moveList),
            "combos" to com.google.gson.Gson().toJson(combos),
            "frameData" to com.google.gson.Gson().toJson(frameData),
            "translations" to com.google.gson.Gson().toJson(translations),
            "createdBy" to createdBy,
            "createdAt" to createdAt,
            "updatedBy" to updatedBy,
            "updatedAt" to updatedAt,
            "isOfficial" to isOfficial,
            "version" to version
        )
    }
}
