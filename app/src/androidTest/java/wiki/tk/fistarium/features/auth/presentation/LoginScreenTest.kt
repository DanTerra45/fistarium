package wiki.tk.fistarium.features.auth.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Display Tests ====================

    @Test
    fun loginScreen_displaysEmailField() {
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = {},
                onBack = {},
                isLoading = false,
                errorMessage = null
            )
        }

        composeTestRule.onNodeWithTag("email_input").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysPasswordField() {
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = {},
                onBack = {},
                isLoading = false,
                errorMessage = null
            )
        }

        composeTestRule.onNodeWithTag("password_input").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysLoginButton() {
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = {},
                onBack = {},
                isLoading = false,
                errorMessage = null
            )
        }

        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysRegisterLink() {
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = {},
                onBack = {},
                isLoading = false,
                errorMessage = null
            )
        }

        composeTestRule.onNodeWithTag("register_button").assertIsDisplayed()
    }

    // ==================== Error Display Tests ====================

    @Test
    fun loginScreen_displaysErrorMessage() {
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = {},
                onBack = {},
                isLoading = false,
                errorMessage = "Invalid credentials"
            )
        }

        composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }

    // ==================== Loading State Tests ====================

    @Test
    fun loginScreen_disablesLoginButton_whenLoading() {
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = {},
                onBack = {},
                isLoading = true,
                errorMessage = null
            )
        }

        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }

    // ==================== Navigation Tests ====================

    @Test
    fun loginScreen_registerLink_callsOnNavigateToRegister() {
        var registerCalled = false
        
        composeTestRule.setContent {
            LoginContent(
                onLogin = { _, _ -> },
                onGoToRegister = { registerCalled = true },
                onBack = {},
                isLoading = false,
                errorMessage = null
            )
        }

        composeTestRule.onNodeWithTag("register_button").performClick()

        assertTrue(registerCalled)
    }
}
