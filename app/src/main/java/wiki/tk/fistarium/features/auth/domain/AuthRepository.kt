package wiki.tk.fistarium.features.auth.domain

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
    fun isAnonymous(): Boolean
    fun getCurrentUserId(): String?
    fun getUserEmail(): String?
    fun getUserDisplayName(): String?
    fun getUserCreationTimestamp(): Long?
    suspend fun getUserRole(): Result<String>
    suspend fun updateProfile(displayName: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun linkAnonymousAccount(email: String, password: String): Result<Unit>
}