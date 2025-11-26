package wiki.tk.fistarium.features.characters.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wiki.tk.fistarium.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectionScreen(
    onBack: () -> Unit,
    onGameSelected: (String) -> Unit
) {
    val games = listOf(
        GameOption("TK1", "Tekken 1", Color(0xFFD32F2F)),
        GameOption("TK2", "Tekken 2", Color(0xFFC2185B)),
        GameOption("TK3", "Tekken 3", Color(0xFF7B1FA2)),
        GameOption("TK4", "Tekken 4", Color(0xFF512DA8)),
        GameOption("TK5", "Tekken 5", Color(0xFF303F9F)),
        GameOption("TK6", "Tekken 6", Color(0xFF1976D2)),
        GameOption("TK7", "Tekken 7", Color(0xFF0288D1)),
        GameOption("TK8", "Tekken 8", Color(0xFF0097A7)),
        GameOption("TAG", "Tekken Tag", Color(0xFF00796B)),
        GameOption("TAG2", "Tekken Tag 2", Color(0xFF388E3C))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_game)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(games) { game ->
                GameCard(game = game, onClick = { onGameSelected(game.id) })
            }
        }
    }
}

@Composable
fun GameCard(game: GameOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(game.color, game.color.copy(alpha = 0.6f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = game.id,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

data class GameOption(
    val id: String,
    val name: String,
    val color: Color
)
