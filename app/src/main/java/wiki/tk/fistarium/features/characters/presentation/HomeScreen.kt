package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    characters: List<Character>,
    searchResults: List<Character> = emptyList(),
    onCharacterClick: (String) -> Unit,
    onLogout: () -> Unit,
    onAddCharacter: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    isOnline: Boolean = true,
    syncState: String = "",
    isLoading: Boolean = false,
    isGuest: Boolean = false,
    onSyncMessageShown: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSyncSuccess by remember { mutableStateOf(false) }

    // Handle transient success message
    LaunchedEffect(syncState) {
        if (syncState == "Sync successful") { // Assuming this is the string from MainActivity
            showSyncSuccess = true
            delay(3000)
            showSyncSuccess = false
            onSyncMessageShown()
        } else {
            showSyncSuccess = false
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirmation_title)) },
            text = { Text(stringResource(R.string.logout_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.dialog_no))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                onSearch(it)
                            },
                            onSearch = { onSearch(it) },
                            expanded = true,
                            onExpandedChange = { },
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            leadingIcon = {
                                IconButton(onClick = {
                                    showSearch = false
                                    searchQuery = ""
                                    onSearch("")
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        onSearch("")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.content_desc_clear_search))
                                    }
                                }
                            }
                        )
                    },
                    expanded = true,
                    onExpandedChange = { }
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        CharacterListContent(
                            characters = searchResults,
                            onCharacterClick = onCharacterClick,
                            isOnline = isOnline,
                            emptyMessage = stringResource(R.string.no_search_results)
                        )
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.home_title)) },
                    actions = {
                        // Online/Offline indicator - Only show if offline
                        if (!isOnline) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = stringResource(R.string.offline_mode),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_hint))
                        }
                        if (!isGuest) {
                            IconButton(onClick = onAddCharacter) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_character))
                            }
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.logout))
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sync status bar
            AnimatedVisibility(
                visible = showSyncSuccess || (!isOnline) || (syncState.isNotBlank() && syncState != "Sync successful"),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (!isOnline || (syncState.isNotBlank() && syncState != "Sync successful" && syncState != "Syncing...")) 
                        MaterialTheme.colorScheme.errorContainer 
                    else if (showSyncSuccess)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (!isOnline) "Offline - Reconnecting..." else syncState,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!isOnline || (syncState.isNotBlank() && syncState != "Sync successful" && syncState != "Syncing..."))
                                MaterialTheme.colorScheme.onErrorContainer
                            else if (showSyncSuccess)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            CharacterListContent(
                characters = characters,
                onCharacterClick = onCharacterClick,
                isOnline = isOnline,
                emptyMessage = stringResource(R.string.no_characters)
            )
        }
    }
}

@Composable
fun CharacterListContent(
    characters: List<Character>,
    onCharacterClick: (String) -> Unit,
    isOnline: Boolean,
    emptyMessage: String
) {
    if (characters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isOnline) {
                    Text(
                        text = "Add your own character using the + button above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Connect to the internet to load characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(characters, key = { it.id }) { character ->
                CharacterCard(
                    character = character,
                    onClick = { onCharacterClick(character.id) }
                )
            }
        }
    }
}


@Composable
fun CharacterCard(character: Character, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            character.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f // Dim the image slightly so text pops
                )
            }

            // Gradient Overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (character.isFavorite) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    character.fightingStyle?.let {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(it, color = androidx.compose.ui.graphics.Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            ),
                            border = null
                        )
                    }
                    character.difficulty?.let {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(it, color = androidx.compose.ui.graphics.Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            ),
                            border = null
                        )
                    }
                }
            }
        }
    }
}