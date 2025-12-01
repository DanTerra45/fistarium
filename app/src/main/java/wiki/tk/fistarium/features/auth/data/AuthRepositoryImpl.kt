package wiki.tk.fistarium.features.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import wiki.tk.fistarium.core.utils.RetryUtils
import wiki.tk.fistarium.features.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return RetryUtils.withRetryResult {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return RetryUtils.withRetryResult {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val userMap = hashMapOf(
                    "email" to email,
                    "role" to "user",
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                firestore.collection("users").document(user.uid)
                    .set(userMap, SetOptions.merge())
                    .await()
            }
        }
    }

    override suspend fun signInAnonymously(): Result<Unit> {
        return RetryUtils.withRetryResult {
            firebaseAuth.signInAnonymously().await()
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun isAnonymous(): Boolean {
        return firebaseAuth.currentUser?.isAnonymous == true
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    override fun getUserDisplayName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    override fun getUserCreationTimestamp(): Long? {
        return firebaseAuth.currentUser?.metadata?.creationTimestamp
    }

    override suspend fun getUserRole(): Result<String> {
        return RetryUtils.withRetryResult {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("No user logged in")
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.getString("role") ?: "user"
        }
    }

    override suspend fun updateProfile(displayName: String): Result<Unit> {
        return RetryUtils.withRetryResult {
            val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return RetryUtils.withRetryResult {
            firebaseAuth.currentUser?.delete()?.await()
                ?: throw Exception("No user logged in")
        }
    }

    override suspend fun linkAnonymousAccount(email: String, password: String): Result<Unit> {
        return RetryUtils.withRetryResult {
            val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
            if (!user.isAnonymous) throw Exception("User is not anonymous")
            
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.linkWithCredential(credential).await()
            
            // Create user document in Firestore
            val userMap = hashMapOf(
                "email" to email,
                "role" to "user",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "convertedFromGuest" to true
            )
            firestore.collection("users").document(user.uid)
                .set(userMap, SetOptions.merge())
                .await()
        }
    }
}