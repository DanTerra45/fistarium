package wiki.tk.fistarium.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.auth.presentation.LoginContent
import wiki.tk.fistarium.features.auth.presentation.RegisterContent
import wiki.tk.fistarium.presentation.ui.components.AnimatedBackground
import wiki.tk.fistarium.presentation.ui.screens.WelcomeContent

/**
 * Nested navigation graph for authentication screens.
 * Shares a single AnimatedBackground instance to prevent animation restarts
 * when navigating between Welcome, Login, and Register screens.
 */
@Composable
fun AuthNavGraph(
    parentNavController: NavHostController,
    authViewModel: AuthViewModel,
    authState: AuthViewModel.AuthState,
    startDestination: String = AuthRoutes.WELCOME
) {
    val authNavController = rememberNavController()
    val currentBackStackEntry by authNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Handle system back button for nested navigation
    // When on Login/Register, go back to Welcome; when on Welcome, let system handle (close app)
    BackHandler(enabled = currentRoute == AuthRoutes.LOGIN || currentRoute == AuthRoutes.REGISTER) {
        authNavController.popBackStack(AuthRoutes.WELCOME, inclusive = false)
    }

    // Single AnimatedBackground instance shared across all auth screens
    AnimatedBackground {
        NavHost(
            navController = authNavController,
            startDestination = startDestination
        ) {
            composable(AuthRoutes.WELCOME) {
                WelcomeContent(
                    onLoginClick = { authNavController.navigate(AuthRoutes.LOGIN) },
                    onRegisterClick = { authNavController.navigate(AuthRoutes.REGISTER) },
                    onGuestClick = { authViewModel.continueAsGuest() }
                )
            }

            composable(AuthRoutes.LOGIN) {
                val isLoading = authState is AuthViewModel.AuthState.Loading
                val errorMessage = (authState as? AuthViewModel.AuthState.Error)?.message
                LoginContent(
                    onLogin = { email, password -> authViewModel.login(email, password) },
                    onGoToRegister = {
                        authNavController.navigate(AuthRoutes.REGISTER) {
                            popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { 
                        authNavController.popBackStack(AuthRoutes.WELCOME, inclusive = false)
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }

            composable(AuthRoutes.REGISTER) {
                val isLoading = authState is AuthViewModel.AuthState.Loading
                val errorMessage = (authState as? AuthViewModel.AuthState.Error)?.message
                RegisterContent(
                    onRegister = { email, password -> authViewModel.register(email, password) },
                    onGoToLogin = {
                        authNavController.navigate(AuthRoutes.LOGIN) {
                            popUpTo(AuthRoutes.REGISTER) { inclusive = true }
                        }
                    },
                    onBack = { 
                        authNavController.popBackStack(AuthRoutes.WELCOME, inclusive = false)
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }
        }
    }
}

object AuthRoutes {
    const val WELCOME = "auth_welcome"
    const val LOGIN = "auth_login"
    const val REGISTER = "auth_register"
}
