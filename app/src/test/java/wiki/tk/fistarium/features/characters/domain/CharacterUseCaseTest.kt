package wiki.tk.fistarium.features.characters.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CharacterUseCaseTest {

    private lateinit var characterRepository: CharacterRepository
    private lateinit var characterUseCase: CharacterUseCase

    // Test data
    private val testCharacter = Character(
        id = "test-id-1",
        name = "Jin Kazama",
        description = "Main protagonist",
        fightingStyle = "Traditional Karate",
        country = "Japan",
        difficulty = "Hard",
        games = listOf("TK3", "TK4", "TK5", "TK6", "TK7", "TK8")
    )

    private val testCharacterList = listOf(
        testCharacter,
        Character(
            id = "test-id-2",
            name = "Kazuya Mishima",
            description = "Antagonist",
            fightingStyle = "Mishima Style Karate",
            country = "Japan",
            difficulty = "Very Hard",
            games = listOf("TK1", "TK2", "TK4", "TK5", "TK6", "TK7", "TK8")
        ),
        Character(
            id = "test-id-3",
            name = "Paul Phoenix",
            description = "American fighter",
            fightingStyle = "Judo",
            country = "USA",
            difficulty = "Easy",
            games = listOf("TK1", "TK2", "TK3", "TK4", "TK5", "TK6", "TK7", "TK8")
        )
    )

    @Before
    fun setup() {
        characterRepository = mockk()
        characterUseCase = CharacterUseCase(characterRepository)
    }

    // ==================== ID Generation Tests ====================

    @Test
    fun `generateCharacterId returns unique UUIDs`() {
        val id1 = characterUseCase.generateCharacterId()
        val id2 = characterUseCase.generateCharacterId()

        assertNotNull(id1)
        assertNotNull(id2)
        assertNotEquals(id1, id2)
        assertTrue(id1.matches(Regex("[a-f0-9-]{36}")))
    }

    // ==================== Get Characters Tests ====================

    @Test
    fun `getCharacters returns flow from repository`() = runTest {
        every { characterRepository.getCharacters() } returns flowOf(testCharacterList)

        val result = characterUseCase.getCharacters().first()

        assertEquals(3, result.size)
        assertEquals("Jin Kazama", result[0].name)
        assertEquals("Kazuya Mishima", result[1].name)
    }

    @Test
    fun `getCharacters returns empty list when no characters`() = runTest {
        every { characterRepository.getCharacters() } returns flowOf(emptyList())

        val result = characterUseCase.getCharacters().first()

        assertTrue(result.isEmpty())
    }

    // ==================== Get Character By ID Tests ====================

    @Test
    fun `getCharacterById returns character when found`() = runTest {
        every { characterRepository.getCharacterById("test-id-1") } returns flowOf(testCharacter)

        val result = characterUseCase.getCharacterById("test-id-1").first()

        assertNotNull(result)
        assertEquals("Jin Kazama", result?.name)
        assertEquals("Traditional Karate", result?.fightingStyle)
    }

    @Test
    fun `getCharacterById returns null when not found`() = runTest {
        every { characterRepository.getCharacterById("nonexistent") } returns flowOf(null)

        val result = characterUseCase.getCharacterById("nonexistent").first()

        assertNull(result)
    }

    // ==================== Favorites Tests ====================

    @Test
    fun `getFavoriteCharacters returns only favorites`() = runTest {
        val favorites = listOf(testCharacter.copy(isFavorite = true))
        every { characterRepository.getFavoriteCharacters() } returns flowOf(favorites)

        val result = characterUseCase.getFavoriteCharacters().first()

        assertEquals(1, result.size)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `toggleFavorite calls repository with correct parameters`() = runTest {
        coEvery { characterRepository.toggleFavorite("test-id-1", true) } returns Result.success(Unit)

        val result = characterUseCase.toggleFavorite("test-id-1", true)

        assertTrue(result.isSuccess)
        coVerify { characterRepository.toggleFavorite("test-id-1", true) }
    }

    @Test
    fun `toggleFavorite propagates repository failure`() = runTest {
        val exception = Exception("Database error")
        coEvery { characterRepository.toggleFavorite(any(), any()) } returns Result.failure(exception)

        val result = characterUseCase.toggleFavorite("test-id-1", true)

        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    // ==================== Search Tests ====================

    @Test
    fun `searchCharacters returns matching characters`() = runTest {
        val searchResults = listOf(testCharacter)
        coEvery { characterRepository.searchCharacters("Jin") } returns searchResults

        val result = characterUseCase.searchCharacters("Jin")

        assertEquals(1, result.size)
        assertEquals("Jin Kazama", result[0].name)
    }

    @Test
    fun `searchCharacters returns empty list for no matches`() = runTest {
        coEvery { characterRepository.searchCharacters("Unknown") } returns emptyList()

        val result = characterUseCase.searchCharacters("Unknown")

        assertTrue(result.isEmpty())
    }

    // ==================== Sync Tests ====================

    @Test
    fun `syncCharacters calls repository sync`() = runTest {
        coEvery { characterRepository.syncCharactersFromRemote() } returns Result.success(Unit)

        val result = characterUseCase.syncCharacters()

        assertTrue(result.isSuccess)
        coVerify { characterRepository.syncCharactersFromRemote() }
    }

    @Test
    fun `syncCharacters propagates network error`() = runTest {
        val exception = Exception("Network unavailable")
        coEvery { characterRepository.syncCharactersFromRemote() } returns Result.failure(exception)

        val result = characterUseCase.syncCharacters()

        assertTrue(result.isFailure)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
    }

    // ==================== Create Character Tests ====================

    @Test
    fun `createCharacter sets metadata correctly`() = runTest {
        val characterSlot = slot<Character>()
        coEvery { characterRepository.createCharacter(capture(characterSlot)) } returns Result.success("new-id")

        val result = characterUseCase.createCharacter(testCharacter, "user-123")

        assertTrue(result.isSuccess)
        
        val capturedCharacter = characterSlot.captured
        assertEquals("user-123", capturedCharacter.createdBy)
        assertEquals("user-123", capturedCharacter.updatedBy)
        assertFalse(capturedCharacter.isOfficial)
        assertEquals(1, capturedCharacter.version)
        assertTrue(capturedCharacter.createdAt > 0)
        assertTrue(capturedCharacter.updatedAt > 0)
    }

    @Test
    fun `createCharacter returns character id on success`() = runTest {
        coEvery { characterRepository.createCharacter(any()) } returns Result.success("created-id")

        val result = characterUseCase.createCharacter(testCharacter, "user-123")

        assertTrue(result.isSuccess)
        assertEquals("created-id", result.getOrNull())
    }

    // ==================== Update Character Tests ====================

    @Test
    fun `updateCharacter increments version`() = runTest {
        val characterSlot = slot<Character>()
        val characterWithVersion = testCharacter.copy(version = 5)
        
        // Mock getCharacterById to return the existing character
        every { characterRepository.getCharacterById(characterWithVersion.id) } returns flowOf(characterWithVersion)
        coEvery { characterRepository.updateCharacter(capture(characterSlot)) } returns Result.success(Unit)

        characterUseCase.updateCharacter(characterWithVersion, "user-456")

        val capturedCharacter = characterSlot.captured
        assertEquals(6, capturedCharacter.version)
        assertEquals("user-456", capturedCharacter.updatedBy)
    }

    @Test
    fun `updateCharacter updates timestamp`() = runTest {
        val characterSlot = slot<Character>()
        val oldTimestamp = 1000L
        val characterWithOldTimestamp = testCharacter.copy(updatedAt = oldTimestamp)
        
        // Mock getCharacterById
        every { characterRepository.getCharacterById(characterWithOldTimestamp.id) } returns flowOf(characterWithOldTimestamp)
        coEvery { characterRepository.updateCharacter(capture(characterSlot)) } returns Result.success(Unit)

        characterUseCase.updateCharacter(characterWithOldTimestamp, "user-789")

        assertTrue(characterSlot.captured.updatedAt > oldTimestamp)
    }

    // ==================== Delete Character Tests ====================

    @Test
    fun `deleteCharacter calls repository delete when owner`() = runTest {
        val character = testCharacter.copy(createdBy = "user-123")
        every { characterRepository.getCharacterById("test-id-1") } returns flowOf(character)
        coEvery { characterRepository.deleteCharacter("test-id-1") } returns Result.success(Unit)

        val result = characterUseCase.deleteCharacter("test-id-1", "user-123", false)

        assertTrue(result.isSuccess)
        coVerify { characterRepository.deleteCharacter("test-id-1") }
    }

    @Test
    fun `deleteCharacter calls repository delete when admin`() = runTest {
        val character = testCharacter.copy(createdBy = "other-user")
        every { characterRepository.getCharacterById("test-id-1") } returns flowOf(character)
        coEvery { characterRepository.deleteCharacter("test-id-1") } returns Result.success(Unit)

        val result = characterUseCase.deleteCharacter("test-id-1", "admin-user", true)

        assertTrue(result.isSuccess)
        coVerify { characterRepository.deleteCharacter("test-id-1") }
    }

    @Test
    fun `deleteCharacter fails when not owner and not admin`() = runTest {
        val character = testCharacter.copy(createdBy = "other-user")
        every { characterRepository.getCharacterById("test-id-1") } returns flowOf(character)

        val result = characterUseCase.deleteCharacter("test-id-1", "user-123", false)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Permission denied") == true)
        coVerify(exactly = 0) { characterRepository.deleteCharacter(any()) }
    }

    @Test
    fun `deleteCharacter propagates failure from repository`() = runTest {
        val character = testCharacter.copy(createdBy = "user-123")
        every { characterRepository.getCharacterById("test-id-1") } returns flowOf(character)
        coEvery { characterRepository.deleteCharacter(any()) } returns Result.failure(Exception("DB Error"))

        val result = characterUseCase.deleteCharacter("test-id-1", "user-123", false)

        assertTrue(result.isFailure)
        assertEquals("DB Error", result.exceptionOrNull()?.message)
    }
}
