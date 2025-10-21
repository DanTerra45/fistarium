package wiki.tk.fistarium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.features.notification.domain.NotificationManager
import wiki.tk.fistarium.presentation.ui.screens.WelcomeScreen
import wiki.tk.fistarium.features.auth.presentation.LoginScreen
import wiki.tk.fistarium.features.auth.presentation.RegisterScreen
import wiki.tk.fistarium.features.characters.presentation.HomeScreen
import wiki.tk.fistarium.features.characters.presentation.DetailScreen
import wiki.tk.fistarium.features.characters.presentation.AddCharacterScreen
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.ui.theme.FistariumTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    
    private val remoteConfigManager: RemoteConfigManager by inject()
    private val notificationManager: NotificationManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Subscribe to notifications
        lifecycleScope.launch {
            notificationManager.subscribeToAllTopics()
        }
        
        setContent {
            FistariumTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = koinViewModel()
                val characterViewModel: CharacterViewModel = koinViewModel()

                val authState by authViewModel.authState.collectAsState()
                val uiState by characterViewModel.uiState.collectAsState()
                val isOnline by characterViewModel.isOnline.collectAsState()

                // Check maintenance mode
                var showMaintenanceDialog by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    if (remoteConfigManager.isMaintenanceMode()) {
                        showMaintenanceDialog = true
                    }
                }

                if (showMaintenanceDialog) {
                    AlertDialog(
                        onDismissRequest = { /* Can't dismiss */ },
                        title = { Text(stringResource(R.string.maintenance_mode)) },
                        text = { Text(remoteConfigManager.getMaintenanceMessage()) },
                        confirmButton = {
                            TextButton(onClick = { showMaintenanceDialog = false }) {
                                Text(stringResource(R.string.dialog_ok))
                            }
                        }
                    )
                }

                // Handle auth state changes
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthViewModel.AuthState.LoggedIn -> {
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                        is AuthViewModel.AuthState.Registered -> {
                            // Auto-login after registration
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                        is AuthViewModel.AuthState.Idle -> {
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        else -> {}
                    }
                }

                // Handle UI state changes
                LaunchedEffect(uiState) {
                    when (uiState) {
                        is CharacterViewModel.UiState.CharacterCreated -> {
                            navController.popBackStack()
                            characterViewModel.clearUiState()
                        }
                        is CharacterViewModel.UiState.CharacterUpdated -> {
                            navController.popBackStack()
                            characterViewModel.clearUiState()
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
                        val searchResults by characterViewModel.searchResults.collectAsState()
                        val syncState = when (val state = characterViewModel.syncState.collectAsState().value) {
                            is CharacterViewModel.SyncState.Loading -> getString(R.string.syncing)
                            is CharacterViewModel.SyncState.Success -> getString(R.string.sync_success)
                            is CharacterViewModel.SyncState.Error -> "${getString(R.string.sync_failed)}: ${state.message}"
                            else -> ""
                        }
                        
                        HomeScreen(
                            characters = characters,
                            searchResults = searchResults,
                            onCharacterClick = { id ->
                                characterViewModel.getCharacterById(id)
                                navController.navigate("detail/$id")
                            },
                            onLogout = {
                                authViewModel.logout()
                            },
                            onAddCharacter = {
                                if (characterViewModel.isEditingEnabled) {
                                    navController.navigate("add_character")
                                }
                            },
                            onSearch = { query ->
                                characterViewModel.searchCharacters(query)
                            },
                            onSeedData = {
                                characterViewModel.seedSampleCharacters()
                            },
                            isOnline = isOnline,
                            syncState = syncState
                        )
                    }
                    
                    composable(
                        "detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val character by characterViewModel.selectedCharacter.collectAsState()
                        
                        DetailScreen(
                            character = character,
                            onBack = { navController.popBackStack() },
                            onToggleFavorite = { characterId, isFavorite ->
                                characterViewModel.toggleFavorite(characterId, isFavorite)
                            },
                            onEdit = if (characterViewModel.isEditingEnabled) {
                                { characterId -> 
                                    navController.navigate("edit_character/$characterId")
                                }
                            } else null
                        )
                    }
                    
                    composable("add_character") {
                        AddCharacterScreen(
                            onBack = { navController.popBackStack() },
                            onSave = { name, description, fightingStyle, country, difficulty ->
                                val userId = authViewModel.getCurrentUserId() ?: "anonymous"
                                val newCharacter = Character(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    description = description,
                                    fightingStyle = fightingStyle,
                                    country = country,
                                    difficulty = difficulty,
                                    stats = emptyMap()
                                )
                                characterViewModel.createCharacter(newCharacter, userId)
                            }
                        )
                    }
                    
                    composable(
                        "edit_character/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val character by characterViewModel.selectedCharacter.collectAsState()
                        
                        // Reuse AddCharacterScreen for editing (pre-populated with existing data)
                        character?.let { char ->
                            AddCharacterScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { name, description, fightingStyle, country, difficulty ->
                                    val userId = authViewModel.getCurrentUserId() ?: "anonymous"
                                    val updatedCharacter = char.copy(
                                        name = name,
                                        description = description,
                                        fightingStyle = fightingStyle,
                                        country = country,
                                        difficulty = difficulty
                                    )
                                    characterViewModel.updateCharacter(updatedCharacter, userId)
                                },
                                initialCharacter = char
                            )
                        }
                    }
                }
            }
        }
    }
}