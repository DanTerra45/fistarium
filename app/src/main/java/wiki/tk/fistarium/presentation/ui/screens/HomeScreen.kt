package wiki.tk.fistarium.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wiki.tk.fistarium.R
import wiki.tk.fistarium.domain.model.Character

@Composable
fun HomeScreen(
    characters: List<Character>,
    onCharacterClick: (String) -> Unit,
    onLogout: () -> Unit,
    syncState: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onLogout) {
                Text(stringResource(R.string.logout))
            }
        }

        Text(text = syncState, modifier = Modifier.padding(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(characters) { character ->
                CharacterItem(character = character, onClick = { onCharacterClick(character.id) })
            }
        }
    }
}

@Composable
fun CharacterItem(character: Character, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = character.name, style = MaterialTheme.typography.titleMedium)
            Text(text = character.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}