package wiki.tk.fistarium.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wiki.tk.fistarium.domain.usecase.AuthUseCase

class AuthViewModel(private val authUseCase: AuthUseCase) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkIfLoggedIn()
    }

    private fun checkIfLoggedIn() {
        if (authUseCase.isLoggedIn()) {
            _authState.value = AuthState.LoggedIn
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authUseCase.login(email, password)
            _authState.value = if (result.isSuccess) AuthState.LoggedIn else AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
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
            authUseCase.logout()
            _authState.value = AuthState.Idle
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object LoggedIn : AuthState()
        object Registered : AuthState()
        data class Error(val message: String) : AuthState()
    }
}