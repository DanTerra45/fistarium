package wiki.tk.fistarium.features.versus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.characters.domain.Character
import wiki.tk.fistarium.features.characters.domain.Move
import wiki.tk.fistarium.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersusResultScreen(
    onBack: () -> Unit,
    // State
    comparisonResult: ComparisonResult?,
    punisherResult: VersusViewModel.PunisherResult?,
    // Events
    onFindPunishers: (moveId: String, attacker: Character, defender: Character) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    if (comparisonResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val p1 = comparisonResult.p1
    val p2 = comparisonResult.p2

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.versus_analysis)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
        ) {
            // Header
            VersusHeader(p1, p2)

            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.stats)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.punishers)) })
            }

            when (selectedTab) {
                0 -> StatsComparisonView(p1, p2, comparisonResult.statsDiff)
                1 -> PunisherToolView(
                    p1 = p1,
                    p2 = p2,
                    punisherResult = punisherResult,
                    onMoveSelected = { move -> onFindPunishers(move.id, p1, p2) }
                )
            }
        }
    }
}

@Composable
fun VersusHeader(p1: Character, p2: Character) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CharacterHeaderItem(p1, Alignment.Start)
        Text(stringResource(R.string.vs_label), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        CharacterHeaderItem(p2, Alignment.End)
    }
}

@Composable
fun CharacterHeaderItem(character: Character, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (character.imageUrl != null) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(character.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatsComparisonView(p1: Character, p2: Character, statsDiff: Map<String, Int>) {
    // Remember locale to avoid recalculation on every recomposition
    val locale = remember { java.util.Locale.getDefault() }
    val unknownText = stringResource(R.string.unknown)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(stringResource(R.string.weakness_analysis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(p1.name, style = MaterialTheme.typography.labelMedium)
                            Text(stringResource(R.string.weak_to, p1.weakSide ?: unknownText), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(p2.name, style = MaterialTheme.typography.labelMedium)
                            Text(stringResource(R.string.weak_to, p2.weakSide ?: unknownText), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(statsDiff.toList()) { (stat, _) ->
            StatRow(stat, p1.stats[stat] ?: 0, p2.stats[stat] ?: 0, locale)
        }
    }
}

@Composable
fun StatRow(statName: String, val1: Int, val2: Int, locale: java.util.Locale) {
    val displayName = remember(statName) {
        statName.replace("_", " ").replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(locale) else it.toString() 
        }
    }
    
    Column {
        Text(displayName, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$val1", modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
            Spacer(modifier = Modifier.width(8.dp))
            
            // Simple visual bar
            Row(modifier = Modifier.weight(1f).height(10.dp).background(Color.Gray.copy(alpha = 0.2f))) {
                val total = (val1 + val2).coerceAtLeast(1).toFloat()
                val p1Weight = val1 / total
                
                Box(modifier = Modifier.fillMaxHeight().weight(p1Weight.coerceAtLeast(0.01f)).background(MaterialTheme.colorScheme.primary))
                Box(modifier = Modifier.fillMaxHeight().weight((1f - p1Weight).coerceAtLeast(0.01f)).background(MaterialTheme.colorScheme.secondary))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text("$val2", modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun PunisherToolView(
    p1: Character,
    p2: Character,
    punisherResult: VersusViewModel.PunisherResult?,
    onMoveSelected: (Move) -> Unit
) {
    var selectedMove by remember { mutableStateOf<Move?>(null) }
    val selectMoveText = stringResource(R.string.select_move)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.select_move_instruction, p1.name, p2.name), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Move Selector
        var expanded by remember { mutableStateOf(false) }
        
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedMove?.command ?: selectMoveText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                p1.moveList.forEach { move ->
                    DropdownMenuItem(
                        text = { Text("${move.command} (${move.name})") },
                        onClick = {
                            selectedMove = move
                            expanded = false
                            onMoveSelected(move)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedMove != null && punisherResult != null) {
            val onBlock = punisherResult.onBlock
            val unknownFrameText = stringResource(R.string.on_block_unknown)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.move_label, selectedMove!!.command), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.on_block_label, onBlock?.toString() ?: unknownFrameText))
                    if (onBlock != null && onBlock >= 0) {
                        Text(stringResource(R.string.safe_cannot_punish), color = SafeGreen)
                    } else if (onBlock != null) {
                        Text(stringResource(R.string.punishable_by_frames, -onBlock), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(stringResource(R.string.punishers_label), style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(punisherResult.punishers, key = { it.id }) { punisher ->
                    PunisherCard(punisher, punisherResult.defender)
                }
            }
        }
    }
}

@Composable
fun PunisherCard(move: Move, character: Character) {
    val frameData = character.frameData[move.id]
    val unknownText = stringResource(R.string.on_block_unknown)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(move.command, fontWeight = FontWeight.Bold)
                Text(move.name, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("i${frameData?.startup ?: unknownText}", color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.damage_short, move.damage ?: unknownText))
            }
        }
    }
}
