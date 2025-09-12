package wiki.tk.fistarium.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import wiki.tk.fistarium.data.local.entity.CharacterEntity

class CharacterRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val gson: Gson = Gson()
) {

    suspend fun fetchCharacters(): Result<List<CharacterEntity>> {
        return try {
            val snapshot = firestore.collection("characters").get().await()
            val characters = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    val stats = when (val raw = data["stats"]) {
                        is Map<*, *> -> raw.mapKeys { it.key.toString() }.mapValues { it.value }
                        else -> emptyMap<String, Any>()
                    }
                    CharacterEntity(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String,
                        statsJson = gson.toJson(stats)
                    )
                } else null
            }
            Result.success(characters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}