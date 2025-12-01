package wiki.tk.fistarium.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wiki.tk.fistarium.features.auth.domain.AuthUseCase
import wiki.tk.fistarium.features.auth.domain.SyncFavoritesUseCase

class AuthViewModel(
    private val authUseCase: AuthUseCase,
    private val syncFavoritesUseCase: SyncFavoritesUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userRole = MutableStateFlow("user")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Reactive user info - avoids synchronous Firebase access on main thread
    private val _userInfo = MutableStateFlow(UserInfo())
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    init {
        checkIfLoggedIn()
    }

    private fun checkIfLoggedIn() {
        if (authUseCase.isLoggedIn()) {
            refreshUserInfo()
            if (authUseCase.isAnonymous()) {
                _authState.value = AuthState.Guest
            } else {
                _authState.value = AuthState.LoggedIn
                fetchUserRole()
            }
        }
    }

    private fun refreshUserInfo() {
        _userInfo.update {
            UserInfo(
                userId = authUseCase.getCurrentUserId(),
                email = authUseCase.getUserEmail(),
                displayName = authUseCase.getUserDisplayName(),
                creationTimestamp = authUseCase.getUserCreationTimestamp()
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.login(email, password)
            if (result.isSuccess) {
                refreshUserInfo()
                val userId = _userInfo.value.userId
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
            
            // If user is currently a guest, link the account instead of creating new
            if (authUseCase.isAnonymous()) {
                val result = authUseCase.linkAnonymousAccount(email, password)
                if (result.isSuccess) {
                    refreshUserInfo()
                    fetchUserRole()
                    _authState.value = AuthState.LoggedIn
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Account linking failed")
                }
            } else {
                // Normal registration for non-guests
                val result = authUseCase.register(email, password)
                if (result.isSuccess) {
                    refreshUserInfo()
                    _authState.value = AuthState.Registered
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (authUseCase.isAnonymous()) {
                authUseCase.deleteAccount()
            }
            authUseCase.logout()
            syncFavoritesUseCase.clearFavorites()
            _userInfo.value = UserInfo()
            _authState.value = AuthState.Idle
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.signInAnonymously()
            if (result.isSuccess) {
                refreshUserInfo()
                _authState.value = AuthState.Guest
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Guest login failed")
            }
        }
    }

    // Backward compatibility - delegate to userInfo StateFlow
    fun getCurrentUserId(): String? = _userInfo.value.userId
    fun getUserEmail(): String? = _userInfo.value.email
    fun getUserDisplayName(): String? = _userInfo.value.displayName
    fun getUserCreationTimestamp(): Long? = _userInfo.value.creationTimestamp

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.updateProfile(displayName)
            if (result.isSuccess) {
                refreshUserInfo()
                _authState.value = AuthState.LoggedIn
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Update failed")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.deleteAccount()
            if (result.isSuccess) {
                _userInfo.value = UserInfo()
                _authState.value = AuthState.Idle
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Delete account failed")
            }
        }
    }

    // User info data class
    data class UserInfo(
        val userId: String? = null,
        val email: String? = null,
        val displayName: String? = null,
        val creationTimestamp: Long? = null
    )

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data object LoggedIn : AuthState()
        data object Registered : AuthState()
        data object Guest : AuthState()
        data class Error(val message: String) : AuthState()
    }
}