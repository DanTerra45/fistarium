package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import wiki.tk.fistarium.features.characters.domain.Character

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCharacters = listOf(
        Character(
            id = "jin-1",
            name = "Jin Kazama",
            description = "Main protagonist",
            fightingStyle = "Traditional Karate",
            difficulty = "Hard",
            games = listOf("TK8")
        ),
        Character(
            id = "kazuya-1",
            name = "Kazuya Mishima",
            description = "Antagonist",
            fightingStyle = "Mishima Style Karate",
            difficulty = "Very Hard",
            games = listOf("TK8")
        ),
        Character(
            id = "paul-1",
            name = "Paul Phoenix",
            description = "American fighter",
            fightingStyle = "Judo",
            difficulty = "Easy",
            games = listOf("TK8"),
            isFavorite = true
        )
    )

    // ==================== Display Tests ====================

    @Test
    fun homeScreen_displaysCharacterList() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Jin Kazama").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kazuya Mishima").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paul Phoenix").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysEmptyState() {
        composeTestRule.setContent {
            HomeScreen(
                characters = emptyList(),
                onCharacterClick = {},
                onBack = {}
            )
        }

        // Should show empty message
        composeTestRule.onNodeWithText("No characters found", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysFightingStyle() {
        composeTestRule.setContent {
            HomeScreen(
                characters = listOf(testCharacters[0]),
                onCharacterClick = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Traditional Karate").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysDifficulty() {
        composeTestRule.setContent {
            HomeScreen(
                characters = listOf(testCharacters[0]),
                onCharacterClick = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Hard").assertIsDisplayed()
    }

    // ==================== Navigation Tests ====================

    @Test
    fun homeScreen_backButton_callsOnBack() {
        var backCalled = false
        
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = { backCalled = true }
            )
        }

        composeTestRule.onNodeWithTag("back_button").performClick()

        assertTrue(backCalled)
    }

    @Test
    fun homeScreen_characterClick_callsOnCharacterClick() {
        var clickedId: String? = null
        
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = { id -> clickedId = id },
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Jin Kazama").performClick()

        assertEquals("jin-1", clickedId)
    }

    // ==================== Online/Offline Tests ====================

    @Test
    fun homeScreen_showsOfflineIndicator_whenOffline() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                isOnline = false
            )
        }

        // Should show offline indicator in action bar
        composeTestRule.onNodeWithTag("offline_indicator").assertIsDisplayed()
    }

    @Test
    fun homeScreen_hidesOfflineIndicator_whenOnline() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                isOnline = true
            )
        }

        // Should NOT show offline indicator
        composeTestRule.onNodeWithTag("offline_indicator").assertDoesNotExist()
    }

    // ==================== Admin Tests ====================

    @Test
    fun homeScreen_showsAddButton_forAdmin() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                isGuest = false,
                userRole = "admin"
            )
        }

        composeTestRule.onNodeWithTag("add_character_button").assertIsDisplayed()
    }

    @Test
    fun homeScreen_hidesAddButton_forGuest() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                isGuest = true,
                userRole = "user"
            )
        }

        composeTestRule.onNodeWithTag("add_character_button").assertDoesNotExist()
    }

    @Test
    fun homeScreen_hidesAddButton_forRegularUser() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                isGuest = false,
                userRole = "user"
            )
        }

        composeTestRule.onNodeWithTag("add_character_button").assertDoesNotExist()
    }

    // ==================== Title Tests ====================

    @Test
    fun homeScreen_displaysCustomTitle() {
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                title = "Tekken 8 Characters"
            )
        }

        composeTestRule.onNodeWithText("Tekken 8 Characters").assertIsDisplayed()
    }

    @Test
    fun homeScreen_addButton_callsOnAddCharacter() {
        var addCalled = false
        
        composeTestRule.setContent {
            HomeScreen(
                characters = testCharacters,
                onCharacterClick = {},
                onBack = {},
                onAddCharacter = { addCalled = true },
                isGuest = false,
                userRole = "admin"
            )
        }

        composeTestRule.onNodeWithTag("add_character_button").performClick()

        assertTrue(addCalled)
    }
}
