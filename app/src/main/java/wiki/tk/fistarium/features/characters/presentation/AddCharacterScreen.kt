package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCharacterScreen(
    onBack: () -> Unit,
    onSave: (name: String, description: String, story: String, fightingStyle: String, country: String, difficulty: String, stats: Map<String, Int>, imageUrl: String, games: List<String>) -> Unit,
    initialCharacter: Character? = null
) {
    var name by remember { mutableStateOf(initialCharacter?.name ?: "") }
    var description by remember { mutableStateOf(initialCharacter?.description ?: "") }
    var story by remember { mutableStateOf(initialCharacter?.story ?: "") }
    var fightingStyle by remember { mutableStateOf(initialCharacter?.fightingStyle ?: "") }
    var country by remember { mutableStateOf(initialCharacter?.country ?: "") }
    var imageUrl by remember { mutableStateOf(initialCharacter?.imageUrl ?: "") }
    var difficulty by remember { mutableStateOf(initialCharacter?.difficulty ?: "Medium") }
    var selectedGames by remember { mutableStateOf(initialCharacter?.games ?: listOf("TK8")) }
    
    // Stats
    var power by remember { mutableIntStateOf(initialCharacter?.stats?.get("power") ?: 50) }
    var speed by remember { mutableIntStateOf(initialCharacter?.stats?.get("speed") ?: 50) }
    var range by remember { mutableIntStateOf(initialCharacter?.stats?.get("range") ?: 50) }
    var technique by remember { mutableIntStateOf(initialCharacter?.stats?.get("technique") ?: 50) }
    var easeOfUse by remember { mutableIntStateOf(initialCharacter?.stats?.get("ease_of_use") ?: 50) }

    var expanded by remember { mutableStateOf(false) }
    var gamesExpanded by remember { mutableStateOf(false) }

    val difficulties = listOf("Easy", "Medium", "Hard", "Very Hard")
    val difficultyResources = mapOf(
        "Easy" to R.string.difficulty_easy,
        "Medium" to R.string.difficulty_medium,
        "Hard" to R.string.difficulty_hard,
        "Very Hard" to R.string.difficulty_very_hard
    )
    
    val availableGames = listOf(
        "TK1" to "Tekken 1",
        "TK2" to "Tekken 2",
        "TK3" to "Tekken 3",
        "TK4" to "Tekken 4",
        "TK5" to "Tekken 5",
        "TK6" to "Tekken 6",
        "TK7" to "Tekken 7",
        "TK8" to "Tekken 8",
        "TAG" to "Tekken Tag",
        "TAG2" to "Tekken Tag 2"
    )
    
    val isEditMode = initialCharacter != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) stringResource(R.string.edit_character_title) else stringResource(R.string.add_character)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + navBarPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.basic_info_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.character_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.character_description)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = story,
                            onValueChange = { story = it },
                            label = { Text(stringResource(R.string.story_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 10,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.details_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = fightingStyle,
                            onValueChange = { fightingStyle = it },
                            label = { Text(stringResource(R.string.fighting_style)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(stringResource(R.string.country)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text(stringResource(R.string.image_url)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Game Selection
                        ExposedDropdownMenuBox(
                            expanded = gamesExpanded,
                            onExpandedChange = { gamesExpanded = !gamesExpanded }
                        ) {
                            val selectedGamesDisplay = if (selectedGames.isEmpty()) {
                                stringResource(R.string.select_games)
                            } else {
                                selectedGames.joinToString(", ")
                            }
                            OutlinedTextField(
                                value = selectedGamesDisplay,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.select_games)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gamesExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = gamesExpanded,
                                onDismissRequest = { gamesExpanded = false }
                            ) {
                                availableGames.forEach { (gameId, gameName) ->
                                    val isSelected = selectedGames.contains(gameId)
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(gameName) 
                                            }
                                        },
                                        onClick = {
                                            selectedGames = if (isSelected) {
                                                selectedGames - gameId
                                            } else {
                                                selectedGames + gameId
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            val displayDifficulty = difficultyResources[difficulty]?.let { stringResource(it) } ?: difficulty
                            OutlinedTextField(
                                value = displayDifficulty,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.difficulty)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                difficulties.forEach { diff ->
                                    DropdownMenuItem(
                                        text = { Text(difficultyResources[diff]?.let { stringResource(it) } ?: diff) },
                                        onClick = {
                                            difficulty = diff
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.stats),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        StatSlider(label = stringResource(R.string.stats_power), value = power, onValueChange = { power = it })
                        StatSlider(label = stringResource(R.string.stats_speed), value = speed, onValueChange = { speed = it })
                        StatSlider(label = stringResource(R.string.stats_range), value = range, onValueChange = { range = it })
                        StatSlider(label = stringResource(R.string.stats_technique), value = technique, onValueChange = { technique = it })
                        StatSlider(label = stringResource(R.string.stats_ease_of_use), value = easeOfUse, onValueChange = { easeOfUse = it })
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (name.isNotBlank() && description.isNotBlank() && selectedGames.isNotEmpty()) {
                            val stats = mapOf(
                                "power" to power,
                                "speed" to speed,
                                "range" to range,
                                "technique" to technique,
                                "ease_of_use" to easeOfUse
                            )
                            onSave(name, description, story, fightingStyle, country, difficulty, stats, imageUrl, selectedGames)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = name.isNotBlank() && description.isNotBlank() && selectedGames.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
private fun StatSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = value.toString(), style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
