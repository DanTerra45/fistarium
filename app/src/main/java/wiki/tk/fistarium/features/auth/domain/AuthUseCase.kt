package wiki.tk.fistarium.features.auth.domain

class AuthUseCase(private val authRepository: AuthRepository) {

    suspend fun login(email: String, password: String): Result<Unit> {
        return authRepository.signIn(email, password)
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return authRepository.signUp(email, password)
    }

    suspend fun signInAnonymously(): Result<Unit> {
        return authRepository.signInAnonymously()
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun isAnonymous(): Boolean {
        return authRepository.isAnonymous()
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun getUserEmail(): String? {
        return authRepository.getUserEmail()
    }

    fun getUserDisplayName(): String? {
        return authRepository.getUserDisplayName()
    }

    fun getUserCreationTimestamp(): Long? {
        return authRepository.getUserCreationTimestamp()
    }

    suspend fun getUserRole(): Result<String> {
        return authRepository.getUserRole()
    }

    suspend fun updateProfile(displayName: String): Result<Unit> {
        return authRepository.updateProfile(displayName)
    }

    suspend fun logout() {
        authRepository.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        return authRepository.deleteAccount()
    }
}