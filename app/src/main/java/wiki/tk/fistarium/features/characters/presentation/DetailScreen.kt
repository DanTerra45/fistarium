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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    character: Character?,
    onBack: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit = { _, _ -> },
    onEdit: ((String) -> Unit)? = null,
    onDelete: ((String) -> Unit)? = null
) {
    // Remember locale to avoid recalculation on every recomposition
    val currentLanguage = remember { java.util.Locale.getDefault().language }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (character == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_character_title)) },
            text = { Text(stringResource(R.string.delete_character_message, character.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete?.invoke(character.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(character.getLocalizedName(currentLanguage)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onToggleFavorite(character.id, !character.isFavorite) },
                        modifier = Modifier.testTag("favorite_button")
                    ) {
                        Icon(
                            if (character.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (character.isFavorite) 
                                stringResource(R.string.remove_from_favorites) 
                            else 
                                stringResource(R.string.add_to_favorites)
                        )
                    }
                    if (onEdit != null) {
                        IconButton(
                            onClick = { onEdit(character.id) },
                            modifier = Modifier.testTag("edit_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                    }
                    if (onDelete != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_button")
                        ) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("detail_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + navBarPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Character Image
            item(key = "image") {
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
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter
                        )
                    }
                }
            }

            /* Story
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = character.getLocalizedStory(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            */

            // Basic Info
            item(key = "basic_info") {
                Card(modifier = Modifier.fillMaxWidth().testTag("basic_info_card")) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        character.getLocalizedFightingStyle(currentLanguage)?.let {
                            InfoRow(stringResource(R.string.fighting_style), it)
                        }
                        character.getLocalizedCountry(currentLanguage)?.let {
                            InfoRow(stringResource(R.string.country), it)
                        }
                        character.getLocalizedDifficulty(currentLanguage)?.let { diff ->
                            val localizedDiff = when(diff) {
                                "Easy" -> stringResource(R.string.difficulty_easy)
                                "Medium" -> stringResource(R.string.difficulty_medium)
                                "Hard" -> stringResource(R.string.difficulty_hard)
                                "Very Hard" -> stringResource(R.string.difficulty_very_hard)
                                else -> diff
                            }
                            InfoRow(stringResource(R.string.difficulty), localizedDiff)
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
                item(key = "stats") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.stats),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            character.stats.forEach { (key, value) ->
                                val label = when(key) {
                                    "power" -> stringResource(R.string.stats_power)
                                    "speed" -> stringResource(R.string.stats_speed)
                                    "range" -> stringResource(R.string.stats_range)
                                    "technique" -> stringResource(R.string.stats_technique)
                                    "ease_of_use" -> stringResource(R.string.stats_ease_of_use)
                                    else -> key.replaceFirstChar { it.uppercase() }
                                }
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Text(
                                            text = value.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { value / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Moves
            if (character.moveList.isNotEmpty()) {
                item(key = "moves_header") {
                    Text(
                        text = stringResource(R.string.moves),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(character.moveList, key = { it.id }) { move ->
                    val frameData = character.frameData[move.id]
                    Card(modifier = Modifier.fillMaxWidth().testTag("move_card_${move.id}")) {
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

                            if (frameData != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(R.string.frame_startup), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        Text(frameData.startup?.toString() ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(R.string.frame_block), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        val blockVal = frameData.onBlock ?: 0
                                        val blockColor = if (blockVal <= -10) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                        Text(frameData.onBlock?.toString() ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = blockColor)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(R.string.frame_hit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        Text(frameData.onHit?.toString() ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(R.string.frame_counter_hit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        Text(frameData.onCounterHit?.toString() ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                }
                            }

                            move.notes?.let { 
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = it, style = MaterialTheme.typography.bodySmall) 
                            }
                        }
                    }
                }
            }

            // Combos
            if (character.combos.isNotEmpty()) {
                item(key = "combos_header") {
                    Text(
                        text = stringResource(R.string.combos),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(character.combos, key = { it.id }) { combo ->
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}