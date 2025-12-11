package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.presentation.ui.components.PingPongMarqueeText
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    characters: List<Character>,
    searchResults: List<Character> = emptyList(),
    onCharacterClick: (String) -> Unit,
    onBack: () -> Unit,
    onAddCharacter: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    isOnline: Boolean = true,
    syncState: String = "",
    isLoading: Boolean = false,
    isGuest: Boolean = false,
    userRole: String = "user",
    onSyncMessageShown: () -> Unit = {},
    title: String = stringResource(R.string.home_title),
    searchHint: String = stringResource(R.string.search_hint)
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showSyncSuccess by remember { mutableStateOf(false) }

    // Handle transient success message with proper lifecycle-aware timeout
    LaunchedEffect(syncState) {
        showSyncSuccess = syncState.contains("success", ignoreCase = true)
    }
    
    // Auto-hide success message after a timeout
    LaunchedEffect(showSyncSuccess) {
        if (showSyncSuccess) {
            kotlinx.coroutines.delay(3000L) // UI feedback delay is acceptable
            showSyncSuccess = false
            onSyncMessageShown()
        }
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
                            placeholder = { Text(searchHint) },
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
                    title = { 
                        PingPongMarqueeText(
                            text = title
                        ) 
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        // Online/Offline indicator - Only show if offline
                        if (!isOnline) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = stringResource(R.string.offline_mode),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.testTag("offline_indicator")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_hint))
                        }
                        if (!isGuest && userRole == "admin") {
                            IconButton(
                                onClick = onAddCharacter,
                                modifier = Modifier.testTag("add_character_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_character))
                            }
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
            // Sync status bar - Only show one state at a time, prioritizing success message
            AnimatedVisibility(
                visible = showSyncSuccess || !isOnline || (isLoading && !showSyncSuccess),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (!isOnline) 
                        MaterialTheme.colorScheme.errorContainer 
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                !isOnline -> stringResource(R.string.offline_reconnecting)
                                showSyncSuccess -> stringResource(R.string.sync_success)
                                isLoading -> stringResource(R.string.syncing)
                                else -> "" // Should not reach here
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!isOnline)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                CharacterListContent(
                    characters = characters,
                    onCharacterClick = onCharacterClick,
                    isOnline = isOnline,
                    emptyMessage = stringResource(R.string.no_characters)
                )
            }
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
                        text = stringResource(R.string.add_character_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.connect_to_load),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    } else {
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
        
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 340.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + navBarPadding.calculateBottomPadding()
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    // Remember locale to avoid recalculation on every recomposition
    val currentLanguage = remember { java.util.Locale.getDefault().language }
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
                    alignment = Alignment.TopCenter,
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
                        text = character.getLocalizedName(currentLanguage),
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
                            label = { 
                                Text(
                                    text = it, 
                                    color = androidx.compose.ui.graphics.Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            ),
                            border = null
                        )
                    }
                    character.difficulty?.let {
                        SuggestionChip(
                            onClick = {},
                            label = { 
                                Text(
                                    text = it, 
                                    color = androidx.compose.ui.graphics.Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
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