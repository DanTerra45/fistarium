package wiki.tk.fistarium.features.characters.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import wiki.tk.fistarium.features.characters.data.local.dao.CharacterDao
import wiki.tk.fistarium.features.characters.data.local.entity.CharacterEntity

@RunWith(AndroidJUnit4::class)
class CharacterDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var characterDao: CharacterDao

    private val testCharacter1 = CharacterEntity(
        id = "jin-1",
        name = "Jin Kazama",
        description = "Main protagonist of the series",
        imageUrl = "https://example.com/jin.png",
        statsJson = """{"power":80,"speed":70}""",
        fightingStyle = "Traditional Karate",
        country = "Japan",
        difficulty = "Hard",
        isFavorite = false,
        gamesJson = """["TK3","TK4","TK5","TK6","TK7","TK8"]"""
    )

    private val testCharacter2 = CharacterEntity(
        id = "kazuya-1",
        name = "Kazuya Mishima",
        description = "Devil Gene carrier",
        imageUrl = "https://example.com/kazuya.png",
        statsJson = """{"power":90,"speed":60}""",
        fightingStyle = "Mishima Style Karate",
        country = "Japan",
        difficulty = "Very Hard",
        isFavorite = true,
        gamesJson = """["TK1","TK2","TK4","TK5","TK6","TK7","TK8"]"""
    )

    private val testCharacter3 = CharacterEntity(
        id = "paul-1",
        name = "Paul Phoenix",
        description = "American martial artist",
        imageUrl = "https://example.com/paul.png",
        statsJson = """{"power":85,"speed":65}""",
        fightingStyle = "Judo",
        country = "USA",
        difficulty = "Easy",
        isFavorite = false,
        gamesJson = """["TK1","TK2","TK3","TK4","TK5","TK6","TK7","TK8"]"""
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        characterDao = database.characterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== Insert Tests ====================

    @Test
    fun insertCharacter_and_retrieve() = runTest {
        characterDao.insertCharacter(testCharacter1)

        val characters = characterDao.getAllCharacters().first()

        assertEquals(1, characters.size)
        assertEquals("Jin Kazama", characters[0].name)
        assertEquals("Traditional Karate", characters[0].fightingStyle)
    }

    @Test
    fun insertMultipleCharacters_and_retrieve() = runTest {
        val characters = listOf(testCharacter1, testCharacter2, testCharacter3)
        characterDao.insertCharacters(characters)

        val result = characterDao.getAllCharacters().first()

        assertEquals(3, result.size)
    }

    @Test
    fun insertCharacter_replacesOnConflict() = runTest {
        characterDao.insertCharacter(testCharacter1)

        val updatedCharacter = testCharacter1.copy(name = "Devil Jin")
        characterDao.insertCharacter(updatedCharacter)

        val result = characterDao.getAllCharacters().first()

        assertEquals(1, result.size)
        assertEquals("Devil Jin", result[0].name)
    }

    // ==================== Query Tests ====================

    @Test
    fun getAllCharacters_orderedByName() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.getAllCharacters().first()

        assertEquals("Jin Kazama", result[0].name)
        assertEquals("Kazuya Mishima", result[1].name)
        assertEquals("Paul Phoenix", result[2].name)
    }

    @Test
    fun getCharacterById_returnsCorrectCharacter() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2))

        val result = characterDao.getCharacterById("kazuya-1").first()

        assertNotNull(result)
        assertEquals("Kazuya Mishima", result?.name)
        assertEquals("Mishima Style Karate", result?.fightingStyle)
    }

    @Test
    fun getCharacterById_returnsNullForNonexistent() = runTest {
        characterDao.insertCharacter(testCharacter1)

        val result = characterDao.getCharacterById("nonexistent").first()

        assertNull(result)
    }

    // ==================== Favorites Tests ====================

    @Test
    fun getFavoriteCharacters_returnsOnlyFavorites() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.getFavoriteCharacters().first()

        assertEquals(1, result.size)
        assertEquals("Kazuya Mishima", result[0].name)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun getFavoriteCharactersSync_returnsOnlyFavorites() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.getFavoriteCharactersSync()

        assertEquals(1, result.size)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun updateFavoriteStatus_addsToFavorites() = runTest {
        characterDao.insertCharacter(testCharacter1)
        assertFalse(characterDao.getCharacterById("jin-1").first()?.isFavorite ?: true)

        characterDao.updateFavoriteStatus("jin-1", true)

        val result = characterDao.getCharacterById("jin-1").first()
        assertTrue(result?.isFavorite ?: false)
    }

    @Test
    fun updateFavoriteStatus_removesFromFavorites() = runTest {
        characterDao.insertCharacter(testCharacter2) // Already favorite
        assertTrue(characterDao.getCharacterById("kazuya-1").first()?.isFavorite ?: false)

        characterDao.updateFavoriteStatus("kazuya-1", false)

        val result = characterDao.getCharacterById("kazuya-1").first()
        assertFalse(result?.isFavorite ?: true)
    }

    @Test
    fun clearAllFavorites_unfavoritesAll() = runTest {
        val characters = listOf(
            testCharacter1.copy(isFavorite = true),
            testCharacter2.copy(isFavorite = true),
            testCharacter3.copy(isFavorite = true)
        )
        characterDao.insertCharacters(characters)

        assertEquals(3, characterDao.getFavoriteCharacters().first().size)

        characterDao.clearAllFavorites()

        assertEquals(0, characterDao.getFavoriteCharacters().first().size)
    }

    // ==================== Search Tests ====================

    @Test
    fun searchCharacters_byName() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.searchCharacters("%Jin%")

        assertEquals(1, result.size)
        assertEquals("Jin Kazama", result[0].name)
    }

    @Test
    fun searchCharacters_byDescription() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.searchCharacters("%Devil%")

        assertEquals(1, result.size)
        assertEquals("Kazuya Mishima", result[0].name)
    }

    @Test
    fun searchCharacters_byFightingStyle() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.searchCharacters("%Judo%")

        assertEquals(1, result.size)
        assertEquals("Paul Phoenix", result[0].name)
    }

    @Test
    fun searchCharacters_multipleResults() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.searchCharacters("%Karate%")

        assertEquals(2, result.size)
    }

    @Test
    fun searchCharacters_noResults() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        val result = characterDao.searchCharacters("%Unknown%")

        assertTrue(result.isEmpty())
    }

    @Test
    fun searchCharacters_caseInsensitive() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1))

        val result = characterDao.searchCharacters("%jin%")

        assertEquals(1, result.size)
    }

    // ==================== Delete Tests ====================

    @Test
    fun deleteCharacter_removesFromDatabase() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2))

        characterDao.deleteCharacter("jin-1")

        val result = characterDao.getAllCharacters().first()
        assertEquals(1, result.size)
        assertNull(characterDao.getCharacterById("jin-1").first())
    }

    @Test
    fun deleteAllCharacters_clearsDatabase() = runTest {
        characterDao.insertCharacters(listOf(testCharacter1, testCharacter2, testCharacter3))

        characterDao.deleteAllCharacters()

        val result = characterDao.getAllCharacters().first()
        assertTrue(result.isEmpty())
    }

    // ==================== Flow Emission Tests ====================

    @Test
    fun getAllCharacters_emitsUpdates() = runTest {
        characterDao.insertCharacter(testCharacter1)
        assertEquals(1, characterDao.getAllCharacters().first().size)

        characterDao.insertCharacter(testCharacter2)
        assertEquals(2, characterDao.getAllCharacters().first().size)

        characterDao.deleteCharacter("jin-1")
        assertEquals(1, characterDao.getAllCharacters().first().size)
    }

    @Test
    fun getCharacterById_emitsUpdates() = runTest {
        characterDao.insertCharacter(testCharacter1)
        assertEquals("Jin Kazama", characterDao.getCharacterById("jin-1").first()?.name)

        characterDao.insertCharacter(testCharacter1.copy(name = "Devil Jin"))
        assertEquals("Devil Jin", characterDao.getCharacterById("jin-1").first()?.name)
    }

    @Test
    fun getFavoriteCharacters_emitsUpdates() = runTest {
        characterDao.insertCharacter(testCharacter1)
        assertEquals(0, characterDao.getFavoriteCharacters().first().size)

        characterDao.updateFavoriteStatus("jin-1", true)
        assertEquals(1, characterDao.getFavoriteCharacters().first().size)

        characterDao.updateFavoriteStatus("jin-1", false)
        assertEquals(0, characterDao.getFavoriteCharacters().first().size)
    }

    // ==================== Data Integrity Tests ====================

    @Test
    fun characterEntity_preservesAllFields() = runTest {
        val fullCharacter = CharacterEntity(
            id = "test-full",
            name = "Test Character",
            description = "Full test",
            imageUrl = "https://example.com/test.png",
            imageUrlsJson = """["url1","url2"]""",
            statsJson = """{"power":100}""",
            fightingStyle = "Test Style",
            country = "Test Country",
            difficulty = "Medium",
            moveListJson = """[{"id":"m1","name":"Move 1"}]""",
            combosJson = """[{"id":"c1","name":"Combo 1"}]""",
            frameDataJson = """{"m1":{"startup":10}}""",
            translationsJson = """{"es":{"name":"Personaje de Prueba"}}""",
            createdBy = "user-123",
            createdAt = 1234567890L,
            updatedBy = "user-456",
            updatedAt = 9876543210L,
            isOfficial = false,
            version = 5,
            isFavorite = true,
            gamesJson = """["TK8"]"""
        )

        characterDao.insertCharacter(fullCharacter)
        val result = characterDao.getCharacterById("test-full").first()

        assertNotNull(result)
        assertEquals(fullCharacter.id, result?.id)
        assertEquals(fullCharacter.name, result?.name)
        assertEquals(fullCharacter.description, result?.description)
        assertEquals(fullCharacter.imageUrl, result?.imageUrl)
        assertEquals(fullCharacter.imageUrlsJson, result?.imageUrlsJson)
        assertEquals(fullCharacter.statsJson, result?.statsJson)
        assertEquals(fullCharacter.fightingStyle, result?.fightingStyle)
        assertEquals(fullCharacter.country, result?.country)
        assertEquals(fullCharacter.difficulty, result?.difficulty)
        assertEquals(fullCharacter.moveListJson, result?.moveListJson)
        assertEquals(fullCharacter.combosJson, result?.combosJson)
        assertEquals(fullCharacter.frameDataJson, result?.frameDataJson)
        assertEquals(fullCharacter.translationsJson, result?.translationsJson)
        assertEquals(fullCharacter.createdBy, result?.createdBy)
        assertEquals(fullCharacter.createdAt, result?.createdAt)
        assertEquals(fullCharacter.updatedBy, result?.updatedBy)
        assertEquals(fullCharacter.updatedAt, result?.updatedAt)
        assertEquals(fullCharacter.isOfficial, result?.isOfficial)
        assertEquals(fullCharacter.version, result?.version)
        assertEquals(fullCharacter.isFavorite, result?.isFavorite)
        assertEquals(fullCharacter.gamesJson, result?.gamesJson)
    }
}
