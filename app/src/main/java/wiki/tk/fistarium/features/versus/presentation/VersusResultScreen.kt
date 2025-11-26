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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersusResultScreen(
    onBack: () -> Unit,
    viewModel: VersusViewModel
) {
    val result by viewModel.comparisonResult.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    if (result == null) {
        // Should not happen if navigated correctly
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val p1 = result!!.p1
    val p2 = result!!.p2

    Scaffold(
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
        ) {
            // Header
            VersusHeader(p1, p2)

            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.stats)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.punishers)) })
            }

            when (selectedTab) {
                0 -> StatsComparisonView(p1, p2, result!!.statsDiff)
                1 -> PunisherToolView(p1, p2, viewModel)
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
        Text("VS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                            Text("Weak to: ${p1.weakSide ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(p2.name, style = MaterialTheme.typography.labelMedium)
                            Text("Weak to: ${p2.weakSide ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(statsDiff.toList()) { (stat, diff) ->
            StatRow(stat, p1.stats[stat] ?: 0, p2.stats[stat] ?: 0)
        }
    }
}

@Composable
fun StatRow(statName: String, val1: Int, val2: Int) {
    Column {
        Text(statName.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }, style = MaterialTheme.typography.labelMedium)
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
                
                Box(modifier = Modifier.fillMaxHeight().weight(p1Weight).background(MaterialTheme.colorScheme.primary))
                Box(modifier = Modifier.fillMaxHeight().weight(1f - p1Weight).background(MaterialTheme.colorScheme.secondary))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text("$val2", modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun PunisherToolView(p1: Character, p2: Character, viewModel: VersusViewModel) {
    var selectedMove by remember { mutableStateOf<Move?>(null) }
    var punishers by remember { mutableStateOf<List<Move>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select a move from ${p1.name} to see how ${p2.name} can punish it.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Move Selector (Simplified dropdown/list)
        var expanded by remember { mutableStateOf(false) }
        
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedMove?.command ?: "Select Move",
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
                            punishers = viewModel.findPunishers(move.id, p1, p2)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedMove != null) {
            val frameData = p1.frameData[selectedMove!!.id]
            val onBlock = frameData?.onBlock
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Move: ${selectedMove!!.command}", fontWeight = FontWeight.Bold)
                    Text("On Block: ${onBlock ?: "?"}")
                    if (onBlock != null && onBlock >= 0) {
                        Text("SAFE! Cannot be punished.", color = Color.Green)
                    } else if (onBlock != null) {
                        Text("Punishable by ${-onBlock} frames or faster.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Punishers:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(punishers) { punisher ->
                    PunisherCard(punisher, p2)
                }
            }
        }
    }
}

@Composable
fun PunisherCard(move: Move, character: Character) {
    val frameData = character.frameData[move.id]
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
                Text("i${frameData?.startup ?: "?"}", color = MaterialTheme.colorScheme.primary)
                Text("Dmg: ${move.damage ?: "?"}")
            }
        }
    }
}
