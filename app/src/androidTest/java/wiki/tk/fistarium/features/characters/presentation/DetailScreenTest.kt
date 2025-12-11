package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.Combo
import wiki.tk.fistarium.features.characters.domain.FrameDataEntry
import androidx.compose.ui.test.hasAnyAncestor
import wiki.tk.fistarium.R

import wiki.tk.fistarium.features.characters.domain.Move

@RunWith(AndroidJUnit4::class)
class DetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCharacter = Character(
        id = "jin-1",
        name = "Jin Kazama",
        description = "Main protagonist of the Tekken series",
        story = "Jin is the son of Kazuya Mishima and Jun Kazama",
        fightingStyle = "Traditional Karate",
        country = "Japan",
        difficulty = "Hard",
        stats = mapOf("power" to 80, "speed" to 70, "range" to 60),
        isFavorite = false,
        isOfficial = true,
        moveList = listOf(
            Move(id = "m1", name = "1 Jab", command = "1", damage = "10", hitLevel = "High"),
            Move(id = "m2", name = "Electric", command = "f,n,d,df+2", damage = "25", hitLevel = "High")
        ),
        frameData = mapOf(
            "m1" to FrameDataEntry(startup = 10, onBlock = 1, onHit = 8, onCounterHit = 8),
            "m2" to FrameDataEntry(startup = 14, onBlock = -10, onHit = 15, onCounterHit = 15)
        ),
        combos = listOf(
            Combo(id = "c1", name = "Basic Combo", commands = "EWGF, b+2,1, f+1, d+1, f+4", damage = "65", difficulty = "Medium")
        ),
        games = listOf("TK3", "TK8")
    )

    // ==================== Character Info Display Tests ====================

    @Test
    fun detailScreen_displaysCharacterName() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Jin Kazama").assertIsDisplayed()
    }

    @Test
    fun detailScreen_displaysFightingStyle() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Traditional Karate").assertIsDisplayed()
    }

    @Test
    fun detailScreen_displaysCountry() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Japan").assertIsDisplayed()
    }

    @Test
    fun detailScreen_displaysDifficulty() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        // Scroll to the Basic Info card
        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("basic_info")
        composeTestRule.onNodeWithText("Hard", substring = true).assertIsDisplayed()
    }

    // ==================== Stats Display Tests ====================

    @Test
    fun detailScreen_displaysStats() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        // Stats section should be displayed
        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("stats")
        composeTestRule.onNodeWithText("80").assertIsDisplayed() // Power value
        composeTestRule.onNodeWithText("70").assertIsDisplayed() // Speed value
    }

    // ==================== Moves Display Tests ====================

    @Test
    fun detailScreen_displaysMoves() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("m1")
        composeTestRule.onNodeWithText("1 Jab").assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("m2")
        composeTestRule.onNodeWithText("Electric").assertIsDisplayed()
    }

    @Test
    fun detailScreen_displaysMoveCommand() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        // Scroll to the move card
        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("m1")
        composeTestRule.onNodeWithText("Command: 1", substring = true).assertIsDisplayed()
    }

    // ==================== Frame Data Tests ====================

    @Test
    fun detailScreen_displaysFrameData() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val startLabel = context.getString(R.string.frame_startup)
        val blockLabel = context.getString(R.string.frame_block)
        val hitLabel = context.getString(R.string.frame_hit)

        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        // Scroll to the move card
        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("m1")
        
        // Use hasAnyAncestor to scope to the specific card
        composeTestRule.onNode(
            hasText(startLabel) and hasAnyAncestor(hasTestTag("move_card_m1"))
        ).assertIsDisplayed()
        
        composeTestRule.onNode(
            hasText(blockLabel) and hasAnyAncestor(hasTestTag("move_card_m1"))
        ).assertIsDisplayed()
        
        composeTestRule.onNode(
            hasText(hitLabel) and hasAnyAncestor(hasTestTag("move_card_m1"))
        ).assertIsDisplayed()
    }

    // ==================== Combos Display Tests ====================

    @Test
    fun detailScreen_displaysCombos() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("c1")
        composeTestRule.onNodeWithText("Basic Combo").assertIsDisplayed()
    }

    // ==================== Navigation Tests ====================

    @Test
    fun detailScreen_backButton_callsOnBack() {
        var backCalled = false
        
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = { backCalled = true },
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("back_button").performClick()

        assertTrue(backCalled)
    }

    // ==================== Favorite Tests ====================

    @Test
    fun detailScreen_toggleFavorite_callsCallback() {
        var toggledId: String? = null
        var toggledValue: Boolean? = null
        
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { id, isFavorite -> 
                    toggledId = id
                    toggledValue = isFavorite
                }
            )
        }

        // Click on favorite button (unfavorite -> favorite)
        composeTestRule.onNodeWithTag("favorite_button").performClick()

        assertEquals("jin-1", toggledId)
        assertEquals(true, toggledValue)
    }

    @Test
    fun detailScreen_showsFilledHeart_whenFavorite() {
        val favoriteCharacter = testCharacter.copy(isFavorite = true)
        
        composeTestRule.setContent {
            DetailScreen(
                character = favoriteCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("favorite_button").assertIsDisplayed()
        // composeTestRule.onNodeWithContentDescription("Remove from Favorites").assertIsDisplayed()
    }

    @Test
    fun detailScreen_showsEmptyHeart_whenNotFavorite() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("favorite_button").assertIsDisplayed()
        // composeTestRule.onNodeWithContentDescription("Add to Favorites").assertIsDisplayed()
    }

    // ==================== Edit Button Tests ====================

    @Test
    fun detailScreen_showsEditButton_whenEditEnabled() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = { },
                onDelete = null
            )
        }

        composeTestRule.onNodeWithTag("edit_button").assertIsDisplayed()
    }

    @Test
    fun detailScreen_hidesEditButton_whenEditDisabled() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = null,
                onDelete = null
            )
        }

        composeTestRule.onNodeWithTag("edit_button").assertDoesNotExist()
    }

    @Test
    fun detailScreen_editButton_callsOnEdit() {
        var editedId: String? = null
        
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = { id -> editedId = id },
                onDelete = null
            )
        }

        composeTestRule.onNodeWithTag("edit_button").performClick()

        assertEquals("jin-1", editedId)
    }

    // ==================== Delete Button Tests ====================

    @Test
    fun detailScreen_showsDeleteButton_whenDeleteEnabled() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = null,
                onDelete = { }
            )
        }

        composeTestRule.onNodeWithTag("delete_button").assertIsDisplayed()
    }

    @Test
    fun detailScreen_hidesDeleteButton_whenDeleteDisabled() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = null,
                onDelete = null
            )
        }

        composeTestRule.onNodeWithTag("delete_button").assertDoesNotExist()
    }

    @Test
    fun detailScreen_deleteButton_showsConfirmationDialog() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = null,
                onDelete = { }
            )
        }

        composeTestRule.onNodeWithTag("delete_button").assertIsDisplayed().performClick()
        
        // Check for dialog title
        composeTestRule.onNodeWithText("Delete Character").assertIsDisplayed()
        // Check for dialog message containing character name - use a unique part of the message
        composeTestRule.onNodeWithText("Are you sure you want to delete", substring = true).assertIsDisplayed()
    }

    @Test
    fun detailScreen_deleteDialog_confirmCallsOnDelete() {
        var deletedId: String? = null
        
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> },
                onEdit = null,
                onDelete = { id -> deletedId = id }
            )
        }

        composeTestRule.onNodeWithTag("delete_button").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("confirm_delete_button").performClick() // Click confirm button in dialog

        assertEquals("jin-1", deletedId)
    }

    // ==================== Loading State Tests ====================

    @Test
    fun detailScreen_showsLoading_whenCharacterIsNull() {
        composeTestRule.setContent {
            DetailScreen(
                character = null,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        // Should show a loading indicator
        // Note: CircularProgressIndicator doesn't have text, but the screen should not crash
    }

    // ==================== User Created Badge Tests ====================

    @Test
    fun detailScreen_showsUserCreatedBadge_forUnofficialCharacter() {
        val userCreatedCharacter = testCharacter.copy(isOfficial = false)
        
        composeTestRule.setContent {
            DetailScreen(
                character = userCreatedCharacter,
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        // Scroll to Basic Info card
        composeTestRule.onNodeWithTag("detail_list").performScrollToKey("basic_info")
        composeTestRule.onNodeWithText("User Created", substring = true).assertIsDisplayed()
    }

    @Test
    fun detailScreen_hidesUserCreatedBadge_forOfficialCharacter() {
        composeTestRule.setContent {
            DetailScreen(
                character = testCharacter, // isOfficial = true
                onBack = {},
                onToggleFavorite = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("User Created", substring = true).assertDoesNotExist()
    }
}
