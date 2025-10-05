package wiki.tk.fistarium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import wiki.tk.fistarium.presentation.ui.screens.WelcomeScreen
import wiki.tk.fistarium.features.auth.presentation.LoginScreen
import wiki.tk.fistarium.features.auth.presentation.RegisterScreen
import wiki.tk.fistarium.features.characters.presentation.HomeScreen
import wiki.tk.fistarium.features.characters.presentation.DetailScreen
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel
import wiki.tk.fistarium.ui.theme.FistariumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FistariumTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = koinViewModel()
                val characterViewModel: CharacterViewModel = koinViewModel()

                val authState by authViewModel.authState.collectAsState()

                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthViewModel.AuthState.LoggedIn -> navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                        is AuthViewModel.AuthState.Idle -> navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                        else -> {}
                    }
                }

                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(
                            onLoginClick = { navController.navigate("login") },
                            onRegisterClick = { navController.navigate("register") }
                        )
                    }
                    composable("login") {
                        val isLoading = authState is AuthViewModel.AuthState.Loading
                        val errorMessage = (authState as? AuthViewModel.AuthState.Error)?.message
                        LoginScreen(
                            onLogin = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onGoToRegister = { navController.navigate("register") },
                            isLoading = isLoading,
                            errorMessage = errorMessage
                        )
                    }
                    composable("register") {
                        val isLoading = authState is AuthViewModel.AuthState.Loading
                        val errorMessage = (authState as? AuthViewModel.AuthState.Error)?.message
                        RegisterScreen(
                            onRegister = { email, password ->
                                authViewModel.register(email, password)
                            },
                            onGoToLogin = { navController.navigate("login") },
                            isLoading = isLoading,
                            errorMessage = errorMessage
                        )
                    }
                    composable("home") {
                        val characters by characterViewModel.characters.collectAsState()
                        val syncState = when (val state = characterViewModel.syncState.collectAsState().value) {
                            is CharacterViewModel.SyncState.Loading -> "Syncing..."
                            is CharacterViewModel.SyncState.Success -> "Synced"
                            is CharacterViewModel.SyncState.Error -> "Sync failed: ${state.message}"
                            else -> "Idle"
                        }
                        HomeScreen(
                            characters = characters,
                            onCharacterClick = { id ->
                                characterViewModel.getCharacterById(id)
                                navController.navigate("detail/$id")
                            },
                            onLogout = {
                                authViewModel.logout()
                            },
                            syncState = syncState
                        )
                    }
                    composable("detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val character by characterViewModel.selectedCharacter.collectAsState()
                        DetailScreen(
                            character = character,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}