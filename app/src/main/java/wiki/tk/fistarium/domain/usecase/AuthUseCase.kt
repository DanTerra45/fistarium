package wiki.tk.fistarium.domain.usecase

import wiki.tk.fistarium.domain.repository.AuthRepository

class AuthUseCase(private val authRepository: AuthRepository) {

    suspend fun login(email: String, password: String): Result<Unit> {
        return authRepository.signIn(email, password)
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return authRepository.signUp(email, password)
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    suspend fun logout() {
        authRepository.signOut()
    }
}