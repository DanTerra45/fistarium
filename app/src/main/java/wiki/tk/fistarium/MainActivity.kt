package wiki.tk.fistarium

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import timber.log.Timber
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel
import wiki.tk.fistarium.features.settings.presentation.SettingsViewModel
import wiki.tk.fistarium.features.versus.presentation.VersusViewModel
import wiki.tk.fistarium.presentation.navigation.AppNavGraph
import wiki.tk.fistarium.presentation.navigation.NavRoutes
import wiki.tk.fistarium.presentation.ui.components.LoadingOverlay
import wiki.tk.fistarium.ui.theme.FistariumTheme

class MainActivity : AppCompatActivity() {

    private val remoteConfigManager: RemoteConfigManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState(initial = 0)

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
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val uiState by characterViewModel.uiState.collectAsState()
    val isOnline by characterViewModel.isOnline.collectAsState()
    val appLanguage by settingsViewModel.appLanguage.collectAsState()
    
    // Track if remote config has been fetched
    var isConfigFetched by remember { mutableStateOf(false) }

    // Ready when auth state is determined AND remote config is fetched
    val isReady = remember(authState, isConfigFetched) {
        authState != AuthViewModel.AuthState.Loading && isConfigFetched
    }

    // Manage Notification Subscriptions based on Language
    LaunchedEffect(appLanguage) {
        val messaging = com.google.firebase.messaging.FirebaseMessaging.getInstance()
        
        // Subscribe to global news
        messaging.subscribeToTopic("news_all")
        
        // Subscribe to language specific news
        // Unsubscribe from other supported languages to avoid duplicate/wrong notifications
        val supportedLanguages = listOf("en", "es")
        supportedLanguages.forEach { lang ->
            if (lang == appLanguage) {
                messaging.subscribeToTopic("news_$lang")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("Subscribed to news_$lang")
                        }
                    }
            } else {
                messaging.unsubscribeFromTopic("news_$lang")
            }
        }
    }

    // Determine start destination based on CURRENT auth state (not remembered)
    val startDestination by remember(authState) {
        derivedStateOf {
            when (authState) {
                is AuthViewModel.AuthState.LoggedIn,
                is AuthViewModel.AuthState.Guest -> NavRoutes.MAIN_MENU
                else -> NavRoutes.WELCOME
            }
        }
    }

    // Check maintenance mode and update
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Trigger for config updates
    var configUpdateTrigger by remember { mutableIntStateOf(0) }

    // Notification Permission for Android 13+
    // Moved to NewsScreen as per user request
    /*
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                // Handle permission result if needed
            }
        )
        
        LaunchedEffect(Unit) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    */

    // Log FCM Token for testing
    LaunchedEffect(Unit) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            Timber.d("Token: $token")
        }
    }

    LaunchedEffect(Unit) {
        // Fetch remote config before checking flags
        remoteConfigManager.fetchAndActivate()
        isConfigFetched = true
        
        // Listen for real-time updates
        remoteConfigManager.observeUpdates().collect {
            configUpdateTrigger++
        }
    }

    LaunchedEffect(isConfigFetched, userRole, configUpdateTrigger) {
        if (isConfigFetched) {
            if (remoteConfigManager.isForceUpdateRequired() || 
                !remoteConfigManager.isVersionCompatible(BuildConfig.VERSION_NAME)) {
                showUpdateDialog = true
            } else if (remoteConfigManager.isMaintenanceMode() && userRole != "admin") {
                showMaintenanceDialog = true
            } else {
                showMaintenanceDialog = false
            }
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
                            data = "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                            setPackage("com.android.vending")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback to browser
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
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
        LoadingOverlay(
            isLoading = !isReady,
            blurRadius = 25f
        ) {
            // Handle Auth State Changes
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthViewModel.AuthState.LoggedIn -> {
                        // Sync characters after login (in case initial sync failed without auth)
                        characterViewModel.syncCharacters()
                        if (navController.currentDestination?.route == NavRoutes.AUTH || 
                            navController.currentDestination?.route == NavRoutes.LOGIN) {
                            navController.navigate(NavRoutes.MAIN_MENU) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    is AuthViewModel.AuthState.Registered -> {
                        characterViewModel.syncCharacters()
                        navController.navigate(NavRoutes.MAIN_MENU) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is AuthViewModel.AuthState.Guest -> {
                        // Sync characters after guest login (initial sync fails without auth)
                        characterViewModel.syncCharacters()
                        if (navController.currentDestination?.route == NavRoutes.AUTH) {
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
                    is CharacterViewModel.UiState.CharacterDeleted -> {
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

            // Handle Notification Deep Link
            LaunchedEffect(Unit) {
                val intent = (context as? android.app.Activity)?.intent
                if (intent?.getStringExtra("navigate_to") == "news") {
                    // Wait for auth state to settle if needed, or just navigate
                    // For now, we assume if they click a notification they want to see news regardless of auth state (if public)
                    // But since News is in Main Menu, we might need to wait for login.
                    // Simple approach:
                    if (authState is AuthViewModel.AuthState.LoggedIn || authState is AuthViewModel.AuthState.Guest) {
                        navController.navigate(NavRoutes.NEWS)
                    }
                }
            }
        } // End LoadingOverlay content
    }
}