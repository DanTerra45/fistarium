package wiki.tk.fistarium.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.presentation.*
import wiki.tk.fistarium.features.history.presentation.HistoryScreen
import wiki.tk.fistarium.features.menu.presentation.MainMenuScreen
import wiki.tk.fistarium.features.profile.presentation.ProfileScreen
import wiki.tk.fistarium.features.settings.presentation.SettingsScreen
import wiki.tk.fistarium.features.versus.presentation.VersusResultScreen
import wiki.tk.fistarium.features.versus.presentation.VersusSearchScreen
import wiki.tk.fistarium.features.news.presentation.NewsScreen
import wiki.tk.fistarium.features.news.presentation.NewsViewModel
import org.koin.androidx.compose.koinViewModel
import wiki.tk.fistarium.features.versus.presentation.VersusViewModel
import java.util.UUID

object NavRoutes {
    const val AUTH = "auth" // Auth flow with shared animated background
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN_MENU = "main_menu"
    const val NEWS = "news"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val GAME_SELECTION = "game_selection"
    const val HISTORY = "history"
    const val HOME = "home?gameId={gameId}"
    const val VERSUS_SEARCH = "versus_search"
    const val VERSUS_RESULT = "versus_result"
    const val DETAIL = "detail/{id}"
    const val ADD_CHARACTER = "add_character"
    const val EDIT_CHARACTER = "edit_character/{id}"

    fun home(gameId: String) = "home?gameId=$gameId"
    fun detail(id: String) = "detail/$id"
    fun editCharacter(id: String) = "edit_character/$id"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel,
    characterViewModel: CharacterViewModel,
    versusViewModel: VersusViewModel,
    authState: AuthViewModel.AuthState,
    userRole: String,
    uiState: CharacterViewModel.UiState,
    isOnline: Boolean
) {
    // Map old welcome route to new auth route for startDestination
    val actualStartDestination = if (startDestination == NavRoutes.WELCOME) NavRoutes.AUTH else startDestination
    
    NavHost(navController = navController, startDestination = actualStartDestination) {
        // Auth flow with shared AnimatedBackground
        composable(NavRoutes.AUTH) {
            AuthNavGraph(
                parentNavController = navController,
                authViewModel = authViewModel,
                authState = authState
            )
        }

        composable(NavRoutes.MAIN_MENU) {
            MainMenuScreen(
                onNavigateToCharacters = { navController.navigate(NavRoutes.GAME_SELECTION) },
                onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                onNavigateToVersus = { 
                    versusViewModel.clearSelection()
                    navController.navigate(NavRoutes.VERSUS_SEARCH) 
                },
                onNavigateToNews = { navController.navigate(NavRoutes.NEWS) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.MAIN_MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.NEWS) {
            val newsViewModel: NewsViewModel = koinViewModel()
            NewsScreen(
                viewModel = newsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCharacter = { id ->
                    characterViewModel.getCharacterById(id)
                    navController.navigate(NavRoutes.detail(id))
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.AUTH)
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.GAME_SELECTION) {
            GameSelectionScreen(
                onBack = { navController.popBackStack() },
                onGameSelected = { gameId ->
                    navController.navigate(NavRoutes.home(gameId))
                }
            )
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            NavRoutes.HOME,
            arguments = listOf(navArgument("gameId") { defaultValue = "TK8" })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "TK8"

            LaunchedEffect(gameId) {
                characterViewModel.filterByGame(gameId)
            }

            val characters by characterViewModel.filteredCharacters.collectAsState()
            val searchResults by characterViewModel.searchResults.collectAsState()
            
            // String resources - called in Composable context
            val syncingText = stringResource(R.string.syncing)
            val syncSuccessText = stringResource(R.string.sync_success)
            val syncFailedText = stringResource(R.string.sync_failed)
            
            val syncState = when (val state = characterViewModel.syncState.collectAsState().value) {
                is CharacterViewModel.SyncState.Loading -> syncingText
                is CharacterViewModel.SyncState.Success -> syncSuccessText
                is CharacterViewModel.SyncState.Error -> "$syncFailedText: ${state.message}"
                else -> ""
            }

            val isGuest = authState is AuthViewModel.AuthState.Guest

            val gameTitles = mapOf(
                "TK1" to stringResource(R.string.game_tk1),
                "TK2" to stringResource(R.string.game_tk2),
                "TK3" to stringResource(R.string.game_tk3),
                "TK4" to stringResource(R.string.game_tk4),
                "TK5" to stringResource(R.string.game_tk5),
                "TK6" to stringResource(R.string.game_tk6),
                "TK7" to stringResource(R.string.game_tk7),
                "TK8" to stringResource(R.string.game_tk8)
            )

            val displayTitle = gameTitles[gameId] ?: stringResource(R.string.characters_game_fallback, gameId)
            val searchHint = stringResource(R.string.searching_in_game, gameTitles[gameId] ?: gameId)

            HomeScreen(
                characters = characters,
                searchResults = searchResults,
                onCharacterClick = { id ->
                    characterViewModel.getCharacterById(id)
                    navController.navigate(NavRoutes.detail(id))
                },
                onBack = { navController.popBackStack() },
                onAddCharacter = {
                    if (characterViewModel.isEditingEnabled && !isGuest) {
                        navController.navigate(NavRoutes.ADD_CHARACTER)
                    }
                },
                onSearch = { query ->
                    characterViewModel.searchCharacters(query)
                },
                isOnline = isOnline,
                syncState = syncState,
                isLoading = uiState is CharacterViewModel.UiState.Loading,
                isGuest = isGuest,
                userRole = userRole,
                onSyncMessageShown = { characterViewModel.resetSyncState() },
                title = displayTitle,
                searchHint = searchHint
            )
        }

        composable(NavRoutes.VERSUS_SEARCH) {
            val versusState by versusViewModel.state.collectAsState()
            
            VersusSearchScreen(
                onBack = { navController.popBackStack() },
                onCompare = { navController.navigate(NavRoutes.VERSUS_RESULT) },
                allCharacters = versusState.allCharacters,
                player1 = versusState.player1,
                player2 = versusState.player2,
                onSelectPlayer1 = { versusViewModel.selectPlayer1(it) },
                onSelectPlayer2 = { versusViewModel.selectPlayer2(it) },
                onClearPlayer1 = { versusViewModel.selectPlayer1(null) },
                onClearPlayer2 = { versusViewModel.selectPlayer2(null) }
            )
        }

        composable(NavRoutes.VERSUS_RESULT) {
            val versusState by versusViewModel.state.collectAsState()
            val punisherResult by versusViewModel.punisherResult.collectAsState()
            
            VersusResultScreen(
                onBack = { 
                    versusViewModel.clearSelection()
                    navController.popBackStack() 
                },
                comparisonResult = versusState.comparisonResult,
                punisherResult = punisherResult,
                onFindPunishers = { moveId, attacker, defender ->
                    versusViewModel.findPunishersAsync(moveId, attacker, defender)
                }
            )
        }

        composable(
            NavRoutes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val character by characterViewModel.selectedCharacter.collectAsState()
            val isGuest = authState is AuthViewModel.AuthState.Guest

            DetailScreen(
                character = character,
                onBack = { navController.popBackStack() },
                onToggleFavorite = { characterId, isFavorite ->
                    characterViewModel.toggleFavorite(characterId, isFavorite)
                },
                onEdit = if (characterViewModel.isEditingEnabled && !isGuest) {
                    { characterId ->
                        navController.navigate(NavRoutes.editCharacter(characterId))
                    }
                } else null,
                onDelete = if (characterViewModel.isEditingEnabled && !isGuest) {
                    { characterId ->
                        val userId = authViewModel.getCurrentUserId() ?: ""
                        val isAdmin = userRole == "admin"
                        characterViewModel.deleteCharacter(characterId, userId, isAdmin)
                    }
                } else null
            )
        }

        composable(NavRoutes.ADD_CHARACTER) {
            AddCharacterScreen(
                onBack = { navController.popBackStack() },
                onSave = { name, description, story, fightingStyle, country, difficulty, stats, imageUrl, games ->
                    val userId = authViewModel.getCurrentUserId() ?: "anonymous"
                    val newCharacter = Character(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        story = story,
                        fightingStyle = fightingStyle,
                        country = country,
                        difficulty = difficulty,
                        stats = stats,
                        imageUrl = imageUrl,
                        games = games
                    )
                    characterViewModel.createCharacter(newCharacter, userId)
                }
            )
        }

        composable(
            NavRoutes.EDIT_CHARACTER,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val character by characterViewModel.selectedCharacter.collectAsState()

            character?.let { char ->
                AddCharacterScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { name, description, story, fightingStyle, country, difficulty, stats, imageUrl, games ->
                        val userId = authViewModel.getCurrentUserId() ?: "anonymous"
                        val updatedCharacter = char.copy(
                            name = name,
                            description = description,
                            story = story,
                            fightingStyle = fightingStyle,
                            country = country,
                            difficulty = difficulty,
                            stats = stats,
                            imageUrl = imageUrl,
                            games = games
                        )
                        characterViewModel.updateCharacter(updatedCharacter, userId)
                    },
                    initialCharacter = char
                )
            }
        }
    }
}
