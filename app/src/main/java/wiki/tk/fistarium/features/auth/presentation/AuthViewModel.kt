package wiki.tk.fistarium.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wiki.tk.fistarium.features.auth.domain.AuthUseCase
import wiki.tk.fistarium.features.auth.domain.SyncFavoritesUseCase

class AuthViewModel(
    private val authUseCase: AuthUseCase,
    private val syncFavoritesUseCase: SyncFavoritesUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userRole = MutableStateFlow("user")
    val userRole: StateFlow<String> = _userRole

    init {
        checkIfLoggedIn()
    }

    private fun checkIfLoggedIn() {
        if (authUseCase.isLoggedIn()) {
            if (authUseCase.isAnonymous()) {
                _authState.value = AuthState.Guest
            } else {
                _authState.value = AuthState.LoggedIn
                fetchUserRole()
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.login(email, password)
            if (result.isSuccess) {
                val userId = authUseCase.getCurrentUserId()
                if (userId != null) {
                    syncFavoritesUseCase.syncUserFavorites(userId)
                    fetchUserRole()
                }
                _authState.value = AuthState.LoggedIn
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    private fun fetchUserRole() {
        viewModelScope.launch {
            val result = authUseCase.getUserRole()
            if (result.isSuccess) {
                _userRole.value = result.getOrDefault("user")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.register(email, password)
            _authState.value = if (result.isSuccess) AuthState.Registered else AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (authUseCase.isAnonymous()) {
                authUseCase.deleteAccount()
            }
            authUseCase.logout()
            syncFavoritesUseCase.clearFavorites()
            _authState.value = AuthState.Idle
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.signInAnonymously()
            _authState.value = if (result.isSuccess) AuthState.Guest else AuthState.Error(result.exceptionOrNull()?.message ?: "Guest login failed")
        }
    }

    fun getCurrentUserId(): String? {
        return authUseCase.getCurrentUserId()
    }

    fun getUserEmail(): String? {
        return authUseCase.getUserEmail()
    }

    fun getUserDisplayName(): String? {
        return authUseCase.getUserDisplayName()
    }

    fun getUserCreationTimestamp(): Long? {
        return authUseCase.getUserCreationTimestamp()
    }

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.updateProfile(displayName)
            // If successful, we might want to refresh the state or just stay LoggedIn
            // But we need to trigger a UI update. Since getUserDisplayName() is not a flow,
            // the UI might not update automatically unless we force it.
            // For now, let's just set state to LoggedIn (which it already is) but maybe emit a side effect?
            // Or we can just rely on the fact that the UI will recompose if we change a state variable.
            // Let's add a ProfileUpdated state or similar if needed, but for now:
            _authState.value = if (result.isSuccess) AuthState.LoggedIn else AuthState.Error(result.exceptionOrNull()?.message ?: "Update failed")
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.deleteAccount()
            _authState.value = if (result.isSuccess) AuthState.Idle else AuthState.Error(result.exceptionOrNull()?.message ?: "Delete account failed")
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object LoggedIn : AuthState()
        object Registered : AuthState()
        object Guest : AuthState()
        data class Error(val message: String) : AuthState()
    }
}