package wiki.tk.fistarium.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel
import wiki.tk.fistarium.features.characters.presentation.CharacterCard
import wiki.tk.fistarium.features.characters.presentation.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToCharacter: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    characterViewModel: CharacterViewModel = koinViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val userInfo by authViewModel.userInfo.collectAsState()
    val favoriteCharacters by characterViewModel.favoriteCharacters.collectAsState()
    
    // Use reactive userInfo StateFlow instead of synchronous getters
    val userId = userInfo.userId
    val userEmail = userInfo.email
    val userDisplayName = userInfo.displayName
    val userCreationTimestamp = userInfo.creationTimestamp
    val isGuest = authState is AuthViewModel.AuthState.Guest

    var showEditDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_profile_title)) },
            text = {
                OutlinedTextField(
                    value = newDisplayName,
                    onValueChange = { newDisplayName = it },
                    label = { Text(stringResource(R.string.display_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.updateProfile(newDisplayName)
                        showEditDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
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
            // User Info Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isGuest) {
                            Text(
                                text = stringResource(R.string.guest_account_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.guest_account_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateToLogin) {
                                Text(stringResource(R.string.login_register_button))
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = userDisplayName ?: stringResource(R.string.fighter_default_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (userRole == "admin") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = stringResource(R.string.admin_badge),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    newDisplayName = userDisplayName ?: ""
                                    showEditDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.edit_profile_title),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (!userEmail.isNullOrBlank()) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "ID: ${userId?.take(8)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Remember date formatter to avoid recreation on every recomposition
                            val dateFormatter = remember {
                                java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
                            }
                            val unknownText = stringResource(R.string.unknown)
                            val formattedDate = remember(userCreationTimestamp) {
                                userCreationTimestamp?.let { dateFormatter.format(java.util.Date(it)) } ?: unknownText
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.member_since_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.favorites_count_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = favoriteCharacters.size.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Favorites Section
            item {
                Text(
                    text = stringResource(R.string.favorite_characters_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (favoriteCharacters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_favorites_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(favoriteCharacters) { character ->
                    CharacterCard(
                        character = character,
                        onClick = { onNavigateToCharacter(character.id) }
                    )
                }
            }
        }
    }
}
