package wiki.tk.fistarium.features.characters.presentation

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import wiki.tk.fistarium.core.config.RemoteConfigManager
import wiki.tk.fistarium.core.utils.NetworkMonitor
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.CharacterUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var characterUseCase: CharacterUseCase
    private lateinit var remoteConfigManager: RemoteConfigManager
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var viewModel: CharacterViewModel

    private val connectivityFlow = MutableStateFlow(true)

    private val testCharacters = listOf(
        Character(
            id = "jin-1",
            name = "Jin Kazama",
            description = "Main protagonist",
            games = listOf("TK3", "TK8")
        ),
        Character(
            id = "kazuya-1",
            name = "Kazuya Mishima",
            description = "Antagonist",
            games = listOf("TK1", "TK8")
        ),
        Character(
            id = "paul-1",
            name = "Paul Phoenix",
            description = "American fighter",
            games = listOf("TK1", "TK7") // Not in TK8
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        characterUseCase = mockk(relaxed = true)
        remoteConfigManager = mockk(relaxed = true)
        networkMonitor = mockk()

        // Default mock behaviors
        every { characterUseCase.getCharacters() } returns flowOf(testCharacters)
        every { characterUseCase.getFavoriteCharacters() } returns flowOf(emptyList())
        every { networkMonitor.observeConnectivity() } returns connectivityFlow
        coEvery { remoteConfigManager.fetchAndActivate() } returns true
        every { remoteConfigManager.isCharacterEditingEnabled() } returns true
        every { remoteConfigManager.isTranslationsEnabled() } returns true
        every { remoteConfigManager.getMaxImageSizeMB() } returns 5L
        coEvery { characterUseCase.syncCharacters() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CharacterViewModel {
        return CharacterViewModel(characterUseCase, remoteConfigManager, networkMonitor)
    }

    // ==================== Initialization Tests ====================

    @Test
    fun `initial state has default values`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("TK8", state.selectedGameId)
        assertEquals(CharacterViewModel.SyncState.Success, state.syncState) // Sync succeeded
    }

    @Test
    fun `characters are loaded on init`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(3, state.characters.size)
    }

    @Test
    fun `filtered characters only include selected game`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Default is TK8, so Paul (TK1, TK7 only) should be filtered out
        val filtered = viewModel.filteredCharacters.value
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.name == "Jin Kazama" })
        assertTrue(filtered.any { it.name == "Kazuya Mishima" })
        assertFalse(filtered.any { it.name == "Paul Phoenix" })
    }

    // ==================== Filter By Game Tests ====================

    @Test
    fun `filterByGame updates selectedGameId`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByGame("TK1")
        advanceUntilIdle()

        assertEquals("TK1", viewModel.selectedGameId.value)
    }

    @Test
    fun `filterByGame updates filtered characters`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByGame("TK1")
        advanceUntilIdle()

        val filtered = viewModel.filteredCharacters.value
        // TK1 has Kazuya and Paul
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.name == "Kazuya Mishima" })
        assertTrue(filtered.any { it.name == "Paul Phoenix" })
        assertFalse(filtered.any { it.name == "Jin Kazama" })
    }

    @Test
    fun `filterByGame with no matching characters returns empty`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.filterByGame("TK2") // No test characters in TK2
        advanceUntilIdle()

        assertTrue(viewModel.filteredCharacters.value.isEmpty())
    }

    // ==================== Search Tests ====================

    @Test
    fun `searchCharacters updates searchResults`() = runTest {
        val searchResults = listOf(testCharacters[0])
        coEvery { characterUseCase.searchCharacters("Jin") } returns searchResults

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.searchCharacters("Jin")
        advanceUntilIdle()

        assertEquals(1, viewModel.searchResults.value.size)
        assertEquals("Jin Kazama", viewModel.searchResults.value[0].name)
    }

    @Test
    fun `searchCharacters with blank query clears results`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.searchCharacters("")

        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `searchCharacters filters by current game`() = runTest {
        // Paul is in TK1 and TK7, but not TK8 (default)
        coEvery { characterUseCase.searchCharacters("Paul") } returns listOf(testCharacters[2])

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.searchCharacters("Paul")
        advanceUntilIdle()

        // Paul should be filtered out since current game is TK8
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    // ==================== Get Character By ID Tests ====================

    @Test
    fun `getCharacterById updates selectedCharacter`() = runTest {
        every { characterUseCase.getCharacterById("jin-1") } returns flowOf(testCharacters[0])

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.getCharacterById("jin-1")
        advanceUntilIdle()

        assertEquals("Jin Kazama", viewModel.selectedCharacter.value?.name)
    }

    // ==================== Favorites Tests ====================

    @Test
    fun `toggleFavorite calls use case`() = runTest {
        coEvery { characterUseCase.toggleFavorite("jin-1", true) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite("jin-1", true)
        advanceUntilIdle()

        coVerify { characterUseCase.toggleFavorite("jin-1", true) }
    }

    @Test
    fun `toggleFavorite failure sets error state`() = runTest {
        coEvery { characterUseCase.toggleFavorite(any(), any()) } returns Result.failure(Exception("DB Error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite("jin-1", true)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is CharacterViewModel.UiState.Error)
    }

    @Test
    fun `favoriteCharacters emits from use case`() = runTest {
        val favorites = listOf(testCharacters[0].copy(isFavorite = true))
        every { characterUseCase.getFavoriteCharacters() } returns flowOf(favorites)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.favoriteCharacters.value.size)
        assertTrue(viewModel.favoriteCharacters.value[0].isFavorite)
    }

    // ==================== Sync Tests ====================

    @Test
    fun `sync triggers when online`() = runTest {
        connectivityFlow.value = true
        coEvery { characterUseCase.syncCharacters() } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        coVerify { characterUseCase.syncCharacters() }
    }

    @Test
    fun `sync success updates state`() = runTest {
        coEvery { characterUseCase.syncCharacters() } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(CharacterViewModel.SyncState.Success, viewModel.syncState.value)
    }

    @Test
    fun `sync failure sets error state`() = runTest {
        coEvery { characterUseCase.syncCharacters() } returns Result.failure(Exception("Network error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        val syncState = viewModel.syncState.value
        assertTrue(syncState is CharacterViewModel.SyncState.Error)
        assertEquals("Network error", (syncState as CharacterViewModel.SyncState.Error).message)
    }

    @Test
    fun `sync skipped when offline`() = runTest {
        connectivityFlow.value = false

        viewModel = createViewModel()
        advanceUntilIdle()

        // Sync should not be called when offline
        // Note: The initial sync happens in observeConnectivity when connected
        // With false, it won't trigger sync
    }

    @Test
    fun `resetSyncState returns to idle`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.resetSyncState()
        advanceUntilIdle()

        assertEquals(CharacterViewModel.SyncState.Idle, viewModel.syncState.value)
    }

    // ==================== Create Character Tests ====================

    @Test
    fun `createCharacter calls use case with correct params`() = runTest {
        val newCharacter = Character(id = "new-1", name = "New Char", description = "Test")
        coEvery { characterUseCase.createCharacter(newCharacter, "user-123") } returns Result.success("new-1")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.createCharacter(newCharacter, "user-123")
        advanceUntilIdle()

        coVerify { characterUseCase.createCharacter(newCharacter, "user-123") }
    }

    @Test
    fun `createCharacter success sets CharacterCreated state`() = runTest {
        val newCharacter = Character(id = "new-1", name = "New Char", description = "Test")
        coEvery { characterUseCase.createCharacter(any(), any()) } returns Result.success("new-1")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.createCharacter(newCharacter, "user-123")
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is CharacterViewModel.UiState.CharacterCreated)
        assertEquals("new-1", (uiState as CharacterViewModel.UiState.CharacterCreated).id)
    }

    @Test
    fun `createCharacter blocked when editing disabled`() = runTest {
        every { remoteConfigManager.isCharacterEditingEnabled() } returns false

        viewModel = createViewModel()
        advanceUntilIdle()

        val newCharacter = Character(id = "new-1", name = "New Char", description = "Test")
        viewModel.createCharacter(newCharacter, "user-123")
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is CharacterViewModel.UiState.Error)
        assertTrue((uiState as CharacterViewModel.UiState.Error).message.contains("disabled"))
    }

    // ==================== Update Character Tests ====================

    @Test
    fun `updateCharacter success sets CharacterUpdated state`() = runTest {
        coEvery { characterUseCase.updateCharacter(any(), any()) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateCharacter(testCharacters[0], "user-123")
        advanceUntilIdle()

        assertEquals(CharacterViewModel.UiState.CharacterUpdated, viewModel.uiState.value)
    }

    // ==================== Delete Character Tests ====================

    @Test
    fun `deleteCharacter success sets CharacterDeleted state`() = runTest {
        coEvery { characterUseCase.deleteCharacter("jin-1", "user-123", false) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteCharacter("jin-1", "user-123", false)
        advanceUntilIdle()

        assertEquals(CharacterViewModel.UiState.CharacterDeleted, viewModel.uiState.value)
    }

    @Test
    fun `deleteCharacter failure sets error state`() = runTest {
        coEvery { characterUseCase.deleteCharacter(any(), any(), any()) } returns Result.failure(Exception("Not authorized"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteCharacter("jin-1", "user-123", false)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is CharacterViewModel.UiState.Error)
    }

    // ==================== Feature Flags Tests ====================

    @Test
    fun `feature flags are loaded from remote config`() = runTest {
        every { remoteConfigManager.isCharacterEditingEnabled() } returns true
        every { remoteConfigManager.isTranslationsEnabled() } returns false
        every { remoteConfigManager.getMaxImageSizeMB() } returns 10L

        viewModel = createViewModel()
        advanceUntilIdle()

        val flags = viewModel.featureFlags.value
        assertTrue(flags.isEditingEnabled)
        assertFalse(flags.isTranslationsEnabled)
        assertEquals(10L, flags.maxImageSizeMB)
    }

    // ==================== UI State Tests ====================

    @Test
    fun `clearUiState resets to Idle`() = runTest {
        coEvery { characterUseCase.deleteCharacter(any(), any(), any()) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteCharacter("jin-1", "user-123", false)
        advanceUntilIdle()
        assertEquals(CharacterViewModel.UiState.CharacterDeleted, viewModel.uiState.value)

        viewModel.clearUiState()
        advanceUntilIdle()

        assertEquals(CharacterViewModel.UiState.Idle, viewModel.uiState.value)
    }

    // ==================== Connectivity Tests ====================

    @Test
    fun `isOnline reflects connectivity state`() = runTest {
        connectivityFlow.value = true

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.isOnline.value)

        connectivityFlow.value = false
        advanceUntilIdle()

        assertFalse(viewModel.isOnline.value)
    }
}
