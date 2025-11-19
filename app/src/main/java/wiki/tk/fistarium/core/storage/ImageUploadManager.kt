package wiki.tk.fistarium.core.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Manager for uploading images to Firebase Storage
 */
class ImageUploadManager(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val characterImagesRef: StorageReference = storage.reference.child("character_images")

    data class UploadProgress(
        val bytesTransferred: Long,
        val totalBytes: Long,
        val progress: Int = if (totalBytes > 0) ((bytesTransferred * 100) / totalBytes).toInt() else 0
    )

    /**
     * Upload an image and return the download URL
     * @param imageUri Local URI of the image to upload
     * @param characterId ID of the character (used for organizing storage)
     * @return Flow of upload progress and final URL
     */
    fun uploadCharacterImage(
        imageUri: Uri,
        characterId: String
    ): Flow<UploadResult> = callbackFlow {
        val fileName = "${UUID.randomUUID()}.jpg"
        val imageRef = characterImagesRef.child(characterId).child(fileName)

        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = UploadProgress(
                bytesTransferred = taskSnapshot.bytesTransferred,
                totalBytes = taskSnapshot.totalByteCount
            )
            trySend(UploadResult.Progress(progress))
        }.addOnSuccessListener {
            // Get download URL
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                trySend(UploadResult.Success(downloadUrl.toString()))
                close()
            }.addOnFailureListener { exception ->
                trySend(UploadResult.Error(exception.message ?: "Failed to get download URL"))
                close(exception)
            }
        }.addOnFailureListener { exception ->
            trySend(UploadResult.Error(exception.message ?: "Upload failed"))
            close(exception)
        }

        awaitClose {
            // Cancel upload if flow is cancelled
            uploadTask.cancel()
        }
    }

    /**
     * Upload multiple images
     */
    suspend fun uploadMultipleImages(
        imageUris: List<Uri>,
        characterId: String
    ): Result<List<String>> {
        return try {
            val uploadedUrls = mutableListOf<String>()
            
            imageUris.forEach { uri ->
                val fileName = "${UUID.randomUUID()}.jpg"
                val imageRef = characterImagesRef.child(characterId).child(fileName)
                
                imageRef.putFile(uri).await()
                val downloadUrl = imageRef.downloadUrl.await()
                uploadedUrls.add(downloadUrl.toString())
            }
            
            Result.success(uploadedUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an image from storage
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    sealed class UploadResult {
        data class Progress(val progress: UploadProgress) : UploadResult()
        data class Success(val downloadUrl: String) : UploadResult()
        data class Error(val message: String) : UploadResult()
    }
}