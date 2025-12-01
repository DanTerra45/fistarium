package wiki.tk.fistarium.features.auth.domain

import android.util.Patterns

class AuthUseCase(private val authRepository: AuthRepository) {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_EMAIL_LENGTH = 254
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        // Input validation
        val validationError = validateCredentials(email, password)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        return authRepository.signIn(email.trim(), password)
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        // Input validation
        val validationError = validateCredentials(email, password)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        return authRepository.signUp(email.trim(), password)
    }

    private fun validateCredentials(email: String, password: String): String? {
        val trimmedEmail = email.trim()
        
        return when {
            trimmedEmail.isBlank() -> "Email cannot be empty"
            trimmedEmail.length > MAX_EMAIL_LENGTH -> "Email is too long"
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "Invalid email format"
            password.isBlank() -> "Password cannot be empty"
            password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
            else -> null
        }
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

    suspend fun linkAnonymousAccount(email: String, password: String): Result<Unit> {
        // Input validation
        val validationError = validateCredentials(email, password)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        return authRepository.linkAnonymousAccount(email.trim(), password)
    }
}