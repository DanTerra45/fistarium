package wiki.tk.fistarium.features.auth.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import wiki.tk.fistarium.features.auth.domain.AuthUseCase
import wiki.tk.fistarium.features.auth.domain.SyncFavoritesUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authUseCase: AuthUseCase
    private lateinit var syncFavoritesUseCase: SyncFavoritesUseCase
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        authUseCase = mockk(relaxed = true)
        syncFavoritesUseCase = mockk(relaxed = true)

        // Default: user is not logged in
        every { authUseCase.isLoggedIn() } returns false
        every { authUseCase.isAnonymous() } returns false
        every { authUseCase.getCurrentUserId() } returns null
        every { authUseCase.getUserEmail() } returns null
        every { authUseCase.getUserDisplayName() } returns null
        every { authUseCase.getUserCreationTimestamp() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AuthViewModel {
        return AuthViewModel(authUseCase, syncFavoritesUseCase)
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state is Idle when not logged in`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun `initial state is LoggedIn when user is logged in`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns false
        every { authUseCase.getCurrentUserId() } returns "user-123"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.LoggedIn, viewModel.authState.value)
    }

    @Test
    fun `initial state is Guest when anonymous user`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Guest, viewModel.authState.value)
    }

    // ==================== Login Tests ====================

    @Test
    fun `login success sets LoggedIn state`() = runTest {
        coEvery { authUseCase.login("test@email.com", "password123") } returns Result.success(Unit)
        every { authUseCase.getCurrentUserId() } returns "user-123"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("test@email.com", "password123")
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.LoggedIn, viewModel.authState.value)
    }

    @Test
    fun `login syncs favorites after success`() = runTest {
        coEvery { authUseCase.login(any(), any()) } returns Result.success(Unit)
        every { authUseCase.getCurrentUserId() } returns "user-123"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("test@email.com", "password123")
        advanceUntilIdle()

        coVerify { syncFavoritesUseCase.syncUserFavorites("user-123") }
    }

    @Test
    fun `login failure sets Error state`() = runTest {
        coEvery { authUseCase.login(any(), any()) } returns Result.failure(Exception("Wrong password"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("test@email.com", "wrongpassword")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthViewModel.AuthState.Error)
        assertEquals("Wrong password", (state as AuthViewModel.AuthState.Error).message)
    }

    @Test
    fun `login sets Loading state during process`() = runTest {
        coEvery { authUseCase.login(any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("test@email.com", "password123")
        
        advanceTimeBy(50)
        assertEquals(AuthViewModel.AuthState.Loading, viewModel.authState.value)
        
        advanceUntilIdle()
    }

    // ==================== Register Tests ====================

    @Test
    fun `register success sets Registered state`() = runTest {
        coEvery { authUseCase.register("new@email.com", "password123") } returns Result.success(Unit)
        every { authUseCase.getCurrentUserId() } returns "new-user-id"

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.register("new@email.com", "password123")
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Registered, viewModel.authState.value)
    }

    @Test
    fun `register failure sets Error state`() = runTest {
        coEvery { authUseCase.register(any(), any()) } returns Result.failure(Exception("Email already exists"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.register("existing@email.com", "password123")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthViewModel.AuthState.Error)
        assertEquals("Email already exists", (state as AuthViewModel.AuthState.Error).message)
    }

    @Test
    fun `register from guest links account`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns true
        coEvery { authUseCase.linkAnonymousAccount("new@email.com", "password123") } returns Result.success(Unit)
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Guest, viewModel.authState.value)

        viewModel.register("new@email.com", "password123")
        advanceUntilIdle()

        // After linking, should be LoggedIn (not Registered)
        assertEquals(AuthViewModel.AuthState.LoggedIn, viewModel.authState.value)
        coVerify { authUseCase.linkAnonymousAccount("new@email.com", "password123") }
    }

    // ==================== Guest Login Tests ====================

    @Test
    fun `continueAsGuest success sets Guest state`() = runTest {
        coEvery { authUseCase.signInAnonymously() } returns Result.success(Unit)
        every { authUseCase.getCurrentUserId() } returns "anon-user"

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Guest, viewModel.authState.value)
    }

    @Test
    fun `continueAsGuest failure sets Error state`() = runTest {
        coEvery { authUseCase.signInAnonymously() } returns Result.failure(Exception("Guest login disabled"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.continueAsGuest()
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthViewModel.AuthState.Error)
    }

    // ==================== Logout Tests ====================

    @Test
    fun `logout resets state to Idle`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns false
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.LoggedIn, viewModel.authState.value)

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun `logout clears favorites`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns false
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        coVerify { syncFavoritesUseCase.clearFavorites() }
    }

    @Test
    fun `logout deletes anonymous account`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        coVerify { authUseCase.deleteAccount() }
    }

    // ==================== User Info Tests ====================

    @Test
    fun `userInfo is updated on login`() = runTest {
        coEvery { authUseCase.login(any(), any()) } returns Result.success(Unit)
        every { authUseCase.getCurrentUserId() } returns "user-123"
        every { authUseCase.getUserEmail() } returns "test@email.com"
        every { authUseCase.getUserDisplayName() } returns "TestUser"
        every { authUseCase.getUserCreationTimestamp() } returns 1234567890L
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("test@email.com", "password123")
        advanceUntilIdle()

        val userInfo = viewModel.userInfo.value
        assertEquals("user-123", userInfo.userId)
        assertEquals("test@email.com", userInfo.email)
        assertEquals("TestUser", userInfo.displayName)
        assertEquals(1234567890L, userInfo.creationTimestamp)
    }

    @Test
    fun `userInfo is cleared on logout`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.getCurrentUserId() } returns "user-123"
        every { authUseCase.getUserEmail() } returns "test@email.com"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.userInfo.value.userId)

        viewModel.logout()
        advanceUntilIdle()

        assertNull(viewModel.userInfo.value.userId)
        assertNull(viewModel.userInfo.value.email)
    }

    // ==================== User Role Tests ====================

    @Test
    fun `userRole is fetched on login`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns false
        coEvery { authUseCase.getUserRole() } returns Result.success("admin")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("admin", viewModel.userRole.value)
    }

    @Test
    fun `userRole defaults to user`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("user", viewModel.userRole.value)
    }

    // ==================== Profile Update Tests ====================

    @Test
    fun `updateProfile success refreshes user info`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.isAnonymous() } returns false
        every { authUseCase.getUserDisplayName() } returns "OldName"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { authUseCase.updateProfile("NewName") } returns Result.success(Unit)
        every { authUseCase.getUserDisplayName() } returns "NewName"

        viewModel.updateProfile("NewName")
        advanceUntilIdle()

        assertEquals("NewName", viewModel.userInfo.value.displayName)
        assertEquals(AuthViewModel.AuthState.LoggedIn, viewModel.authState.value)
    }

    @Test
    fun `updateProfile failure sets Error state`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { authUseCase.updateProfile(any()) } returns Result.failure(Exception("Update failed"))

        viewModel.updateProfile("NewName")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthViewModel.AuthState.Error)
    }

    // ==================== Delete Account Tests ====================

    @Test
    fun `deleteAccount success resets to Idle`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        coEvery { authUseCase.getUserRole() } returns Result.success("user")
        coEvery { authUseCase.deleteAccount() } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthState.Idle, viewModel.authState.value)
        assertNull(viewModel.userInfo.value.userId)
    }

    @Test
    fun `deleteAccount failure sets Error state`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        coEvery { authUseCase.getUserRole() } returns Result.success("user")
        coEvery { authUseCase.deleteAccount() } returns Result.failure(Exception("Requires recent login"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthViewModel.AuthState.Error)
        assertEquals("Requires recent login", (state as AuthViewModel.AuthState.Error).message)
    }

    // ==================== Backward Compatibility Tests ====================

    @Test
    fun `getCurrentUserId returns userInfo userId`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.getCurrentUserId() } returns "user-123"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("user-123", viewModel.getCurrentUserId())
    }

    @Test
    fun `getUserEmail returns userInfo email`() = runTest {
        every { authUseCase.isLoggedIn() } returns true
        every { authUseCase.getUserEmail() } returns "test@email.com"
        coEvery { authUseCase.getUserRole() } returns Result.success("user")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("test@email.com", viewModel.getUserEmail())
    }
}
