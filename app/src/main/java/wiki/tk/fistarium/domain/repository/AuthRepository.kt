package wiki.tk.fistarium.domain.repository

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}