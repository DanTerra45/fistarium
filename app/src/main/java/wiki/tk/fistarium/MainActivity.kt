package wiki.tk.fistarium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel
import wiki.tk.fistarium.features.settings.presentation.SettingsViewModel
import wiki.tk.fistarium.features.versus.presentation.VersusViewModel
import wiki.tk.fistarium.presentation.navigation.AppNavGraph
import wiki.tk.fistarium.presentation.navigation.NavRoutes
import wiki.tk.fistarium.ui.theme.FistariumTheme

class MainActivity : ComponentActivity() {

    private val remoteConfigManager: RemoteConfigManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState(initial = 0)
            val appLanguage by settingsViewModel.appLanguage.collectAsState(initial = "en-US")

            // Apply Language
            LaunchedEffect(appLanguage) {
                val appLocale = LocaleListCompat.forLanguageTags(appLanguage)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }

            val darkTheme = when (themeMode) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System
            }

            FistariumTheme(darkTheme = darkTheme) {
                MainContent(remoteConfigManager = remoteConfigManager)
            }
        }
    }
}

@Composable
private fun MainContent(remoteConfigManager: RemoteConfigManager) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val characterViewModel: CharacterViewModel = koinViewModel()
    val versusViewModel: VersusViewModel = koinViewModel()

    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val uiState by characterViewModel.uiState.collectAsState()
    val isOnline by characterViewModel.isOnline.collectAsState()

    // Determine start destination based on auth state
    val startDestination = remember {
        val state = authViewModel.authState.value
        if (state is AuthViewModel.AuthState.LoggedIn || state is AuthViewModel.AuthState.Guest) {
            NavRoutes.MAIN_MENU
        } else {
            NavRoutes.WELCOME
        }
    }

    // Check maintenance mode and update
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        if (remoteConfigManager.isForceUpdateRequired() || 
            !remoteConfigManager.isVersionCompatible(BuildConfig.VERSION_NAME)) {
            showUpdateDialog = true
        } else if (remoteConfigManager.isMaintenanceMode()) {
            showMaintenanceDialog = true
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { /* Blocking */ },
            title = { Text(stringResource(R.string.update_required_title)) },
            text = { Text(stringResource(R.string.update_required_message)) },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                            setPackage("com.android.vending")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback to browser
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }) {
                    Text(stringResource(R.string.update_button))
                }
            }
        )
    } else if (showMaintenanceDialog) {
        AlertDialog(
            onDismissRequest = { /* Blocking */ },
            title = { Text(stringResource(R.string.maintenance_mode)) },
            text = { Text(stringResource(R.string.maintenance_message_default)) },
            confirmButton = {
                TextButton(onClick = { 
                    (context as? android.app.Activity)?.finish()
                }) {
                    Text(stringResource(R.string.dialog_ok))
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Handle Auth State Changes
        LaunchedEffect(authState) {
            when (authState) {
                is AuthViewModel.AuthState.LoggedIn -> {
                    if (navController.currentDestination?.route == NavRoutes.WELCOME || 
                        navController.currentDestination?.route == NavRoutes.LOGIN) {
                        navController.navigate(NavRoutes.MAIN_MENU) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                is AuthViewModel.AuthState.Registered -> {
                    navController.navigate(NavRoutes.MAIN_MENU) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is AuthViewModel.AuthState.Guest -> {
                    if (navController.currentDestination?.route == NavRoutes.WELCOME) {
                        navController.navigate(NavRoutes.MAIN_MENU) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                is AuthViewModel.AuthState.Idle -> {
                    // Do nothing, let navigation handle it
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

        AppNavGraph(
            navController = navController,
            startDestination = startDestination,
            authViewModel = authViewModel,
            characterViewModel = characterViewModel,
            versusViewModel = versusViewModel,
            authState = authState,
            userRole = userRole,
            uiState = uiState,
            isOnline = isOnline
        )
    }
}