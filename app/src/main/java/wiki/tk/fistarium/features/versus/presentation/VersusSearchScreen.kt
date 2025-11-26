package wiki.tk.fistarium.features.versus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersusSearchScreen(
    onBack: () -> Unit,
    onCompare: () -> Unit,
    viewModel: VersusViewModel
) {
    val allCharacters by viewModel.allCharacters.collectAsState()
    val player1 by viewModel.player1.collectAsState()
    val player2 by viewModel.player2.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.versus_mode)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = {
            if (player1 != null && player2 != null) {
                Button(
                    onClick = onCompare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.fight_button), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Selection Slots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CharacterSlot(
                    character = player1,
                    label = "Player 1",
                    isSelected = player1 == null, // Highlight if waiting for selection
                    onClick = { /* Maybe allow re-selecting specifically P1? For now auto-fill */ },
                    onClear = { viewModel.selectPlayer1(it) } // Hack: passing dummy to clear? No, need clear method
                )
                
                Text(
                    "VS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                CharacterSlot(
                    character = player2,
                    label = "Player 2",
                    isSelected = player1 != null && player2 == null,
                    onClick = { },
                    onClear = { }
                )
            }

            HorizontalDivider()

            // Character List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allCharacters) { char ->
                    CharacterListItem(
                        character = char,
                        onClick = {
                            if (player1 == null) {
                                viewModel.selectPlayer1(char)
                            } else if (player2 == null) {
                                viewModel.selectPlayer2(char)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterSlot(
    character: Character?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onClear: (Character) -> Unit // Not used properly yet
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (character != null) {
                if (character.imageUrl != null) {
                    AsyncImage(
                        model = character.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = character.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = character?.name ?: label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CharacterListItem(
    character: Character,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (character.imageUrl != null) {
                    AsyncImage(
                        model = character.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = character.name.take(1),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
