package wiki.tk.fistarium.features.auth.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authUseCase: AuthUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        authUseCase = AuthUseCase(authRepository)
    }

    // ==================== Login Validation Tests ====================

    @Test
    fun `login fails with empty email`() = runTest {
        val result = authUseCase.login("", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with blank email`() = runTest {
        val result = authUseCase.login("   ", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with invalid email format`() = runTest {
        val result = authUseCase.login("notanemail", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with email missing domain`() = runTest {
        val result = authUseCase.login("test@", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with empty password`() = runTest {
        val result = authUseCase.login("test@email.com", "")

        assertTrue(result.isFailure)
        assertEquals("Password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with short password`() = runTest {
        val result = authUseCase.login("test@email.com", "12345")

        assertTrue(result.isFailure)
        assertEquals("Password must be at least 6 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login succeeds with valid credentials`() = runTest {
        coEvery { authRepository.signIn("test@email.com", "password123") } returns Result.success(Unit)

        val result = authUseCase.login("test@email.com", "password123")

        assertTrue(result.isSuccess)
        coVerify { authRepository.signIn("test@email.com", "password123") }
    }

    @Test
    fun `login trims email whitespace`() = runTest {
        coEvery { authRepository.signIn("test@email.com", any()) } returns Result.success(Unit)

        authUseCase.login("  test@email.com  ", "password123")

        coVerify { authRepository.signIn("test@email.com", "password123") }
    }

    @Test
    fun `login propagates repository error`() = runTest {
        coEvery { authRepository.signIn(any(), any()) } returns Result.failure(Exception("Wrong password"))

        val result = authUseCase.login("test@email.com", "wrongpassword")

        assertTrue(result.isFailure)
        assertEquals("Wrong password", result.exceptionOrNull()?.message)
    }

    // ==================== Register Validation Tests ====================

    @Test
    fun `register fails with empty email`() = runTest {
        val result = authUseCase.register("", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register fails with invalid email`() = runTest {
        val result = authUseCase.register("invalid-email", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register fails with short password`() = runTest {
        val result = authUseCase.register("test@email.com", "123")

        assertTrue(result.isFailure)
        assertEquals("Password must be at least 6 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register succeeds with valid input`() = runTest {
        coEvery { authRepository.signUp("test@email.com", "password123") } returns Result.success(Unit)

        val result = authUseCase.register("test@email.com", "password123")

        assertTrue(result.isSuccess)
        coVerify { authRepository.signUp("test@email.com", "password123") }
    }

    @Test
    fun `register propagates email already exists error`() = runTest {
        coEvery { authRepository.signUp(any(), any()) } returns Result.failure(Exception("Email already in use"))

        val result = authUseCase.register("existing@email.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email already in use", result.exceptionOrNull()?.message)
    }

    // ==================== Anonymous Sign In Tests ====================

    @Test
    fun `signInAnonymously calls repository`() = runTest {
        coEvery { authRepository.signInAnonymously() } returns Result.success(Unit)

        val result = authUseCase.signInAnonymously()

        assertTrue(result.isSuccess)
        coVerify { authRepository.signInAnonymously() }
    }

    @Test
    fun `signInAnonymously propagates failure`() = runTest {
        coEvery { authRepository.signInAnonymously() } returns Result.failure(Exception("Anonymous sign in disabled"))

        val result = authUseCase.signInAnonymously()

        assertTrue(result.isFailure)
    }

    // ==================== Session State Tests ====================

    @Test
    fun `isLoggedIn returns true when user is logged in`() {
        every { authRepository.isUserLoggedIn() } returns true

        assertTrue(authUseCase.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false when user is not logged in`() {
        every { authRepository.isUserLoggedIn() } returns false

        assertFalse(authUseCase.isLoggedIn())
    }

    @Test
    fun `isAnonymous returns true for anonymous user`() {
        every { authRepository.isAnonymous() } returns true

        assertTrue(authUseCase.isAnonymous())
    }

    @Test
    fun `isAnonymous returns false for authenticated user`() {
        every { authRepository.isAnonymous() } returns false

        assertFalse(authUseCase.isAnonymous())
    }

    // ==================== User Info Tests ====================

    @Test
    fun `getCurrentUserId returns user id`() {
        every { authRepository.getCurrentUserId() } returns "user-123"

        assertEquals("user-123", authUseCase.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserId returns null when not logged in`() {
        every { authRepository.getCurrentUserId() } returns null

        assertNull(authUseCase.getCurrentUserId())
    }

    @Test
    fun `getUserEmail returns email`() {
        every { authRepository.getUserEmail() } returns "test@email.com"

        assertEquals("test@email.com", authUseCase.getUserEmail())
    }

    @Test
    fun `getUserDisplayName returns display name`() {
        every { authRepository.getUserDisplayName() } returns "TestUser"

        assertEquals("TestUser", authUseCase.getUserDisplayName())
    }

    @Test
    fun `getUserCreationTimestamp returns timestamp`() {
        every { authRepository.getUserCreationTimestamp() } returns 1234567890L

        assertEquals(1234567890L, authUseCase.getUserCreationTimestamp())
    }

    // ==================== User Role Tests ====================

    @Test
    fun `getUserRole returns user role`() = runTest {
        coEvery { authRepository.getUserRole() } returns Result.success("admin")

        val result = authUseCase.getUserRole()

        assertTrue(result.isSuccess)
        assertEquals("admin", result.getOrNull())
    }

    @Test
    fun `getUserRole returns default user role`() = runTest {
        coEvery { authRepository.getUserRole() } returns Result.success("user")

        val result = authUseCase.getUserRole()

        assertEquals("user", result.getOrNull())
    }

    // ==================== Profile Update Tests ====================

    @Test
    fun `updateProfile calls repository`() = runTest {
        coEvery { authRepository.updateProfile("NewName") } returns Result.success(Unit)

        val result = authUseCase.updateProfile("NewName")

        assertTrue(result.isSuccess)
        coVerify { authRepository.updateProfile("NewName") }
    }

    @Test
    fun `updateProfile propagates failure`() = runTest {
        coEvery { authRepository.updateProfile(any()) } returns Result.failure(Exception("Update failed"))

        val result = authUseCase.updateProfile("NewName")

        assertTrue(result.isFailure)
    }

    // ==================== Logout Tests ====================

    @Test
    fun `logout calls repository signOut`() = runTest {
        authUseCase.logout()

        coVerify { authRepository.signOut() }
    }

    // ==================== Delete Account Tests ====================

    @Test
    fun `deleteAccount calls repository`() = runTest {
        coEvery { authRepository.deleteAccount() } returns Result.success(Unit)

        val result = authUseCase.deleteAccount()

        assertTrue(result.isSuccess)
        coVerify { authRepository.deleteAccount() }
    }

    @Test
    fun `deleteAccount propagates failure`() = runTest {
        coEvery { authRepository.deleteAccount() } returns Result.failure(Exception("Requires recent login"))

        val result = authUseCase.deleteAccount()

        assertTrue(result.isFailure)
        assertEquals("Requires recent login", result.exceptionOrNull()?.message)
    }

    // ==================== Link Anonymous Account Tests ====================

    @Test
    fun `linkAnonymousAccount validates email`() = runTest {
        val result = authUseCase.linkAnonymousAccount("invalid", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `linkAnonymousAccount validates password`() = runTest {
        val result = authUseCase.linkAnonymousAccount("test@email.com", "123")

        assertTrue(result.isFailure)
        assertEquals("Password must be at least 6 characters", result.exceptionOrNull()?.message)
    }

    @Test
    fun `linkAnonymousAccount succeeds with valid input`() = runTest {
        coEvery { authRepository.linkAnonymousAccount("test@email.com", "password123") } returns Result.success(Unit)

        val result = authUseCase.linkAnonymousAccount("test@email.com", "password123")

        assertTrue(result.isSuccess)
        coVerify { authRepository.linkAnonymousAccount("test@email.com", "password123") }
    }

    @Test
    fun `linkAnonymousAccount trims email`() = runTest {
        coEvery { authRepository.linkAnonymousAccount(any(), any()) } returns Result.success(Unit)

        authUseCase.linkAnonymousAccount("  test@email.com  ", "password123")

        coVerify { authRepository.linkAnonymousAccount("test@email.com", "password123") }
    }
}
