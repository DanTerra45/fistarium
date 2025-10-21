package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    character: Character?,
    onBack: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit = { _, _ -> },
    onEdit: ((String) -> Unit)? = null
) {
    if (character == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(character.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite(character.id, !character.isFavorite) }) {
                        Icon(
                            if (character.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (character.isFavorite) 
                                stringResource(R.string.remove_from_favorites) 
                            else 
                                stringResource(R.string.add_to_favorites)
                        )
                    }
                    if (onEdit != null) {
                        IconButton(onClick = { onEdit(character.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Character Image
            item {
                character.imageUrl?.let { url ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = character.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Description
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = character.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Basic Info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        character.fightingStyle?.let {
                            InfoRow(stringResource(R.string.fighting_style), it)
                        }
                        character.country?.let {
                            InfoRow(stringResource(R.string.country), it)
                        }
                        character.difficulty?.let {
                            InfoRow(stringResource(R.string.difficulty), it)
                        }
                        if (!character.isOfficial) {
                            Text(
                                text = stringResource(R.string.user_created),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Stats
            if (character.stats.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.stats),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            character.stats.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = key.replaceFirstChar { it.uppercase() })
                                    LinearProgressIndicator(
                                        progress = { value / 100f },
                                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                    )
                                    Text(text = value.toString())
                                }
                            }
                        }
                    }
                }
            }

            // Moves
            if (character.moveList.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.moves),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(character.moveList) { move ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = move.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(R.string.command_label, move.command),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            move.damage?.let { 
                                Text(text = stringResource(R.string.damage_label, it)) 
                            }
                            move.hitLevel?.let { 
                                Text(text = stringResource(R.string.hit_level_label, it)) 
                            }
                            move.notes?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }

            // Combos
            if (character.combos.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.combos),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(character.combos) { combo ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = combo.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = combo.commands, style = MaterialTheme.typography.bodyMedium)
                            combo.damage?.let { 
                                Text(text = stringResource(R.string.damage_label, it)) 
                            }
                            combo.difficulty?.let { 
                                Text(
                                    text = stringResource(R.string.combo_difficulty_label, it),
                                    color = MaterialTheme.colorScheme.secondary
                                ) 
                            }
                            combo.situation?.let { 
                                Text(text = stringResource(R.string.situation_label, it)) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}