package wiki.tk.fistarium.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wiki.tk.fistarium.R
import wiki.tk.fistarium.domain.model.Character

@Composable
fun DetailScreen(
    character: Character?,
    onBack: () -> Unit
) {
    if (character == null) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = character.name, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = character.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.stats), style = MaterialTheme.typography.titleMedium)
        character.stats.forEach { (key, value) ->
            Text(text = "$key: $value")
        }
    }
}