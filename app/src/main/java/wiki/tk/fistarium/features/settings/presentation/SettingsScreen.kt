package wiki.tk.fistarium.features.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import org.koin.androidx.compose.koinViewModel
import wiki.tk.fistarium.BuildConfig
import wiki.tk.fistarium.R
import wiki.tk.fistarium.features.auth.presentation.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState(initial = 0)
    val appLanguage by viewModel.appLanguage.collectAsState(initial = "en")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings_title), color = MaterialTheme.colorScheme.onBackground) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Selector
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_dark_mode),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOption(
                                text = stringResource(R.string.settings_theme_system),
                                selected = themeMode == 0,
                                onClick = { viewModel.setThemeMode(0) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOption(
                                text = stringResource(R.string.settings_theme_light),
                                selected = themeMode == 1,
                                onClick = { viewModel.setThemeMode(1) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOption(
                                text = stringResource(R.string.settings_theme_dark),
                                selected = themeMode == 2,
                                onClick = { viewModel.setThemeMode(2) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Language Selector
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_language),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var expanded by remember { mutableStateOf(false) }
                        val languages = listOf(
                            "en-US" to "English (US)",
                            "es-419" to "Español (Latinoamérica)",
                            "es-ES" to "Español (España)"
                        )
                        val selectedLanguageName = languages.find { it.first == appLanguage }?.second ?: "English (US)"

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedLanguageName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                languages.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = { Text(text = name) },
                                        onClick = {
                                            viewModel.setAppLanguage(code)
                                            expanded = false
                                            // Apply language - AppCompatDelegate handles activity recreation
                                            val appLocale = LocaleListCompat.forLanguageTags(code)
                                            AppCompatDelegate.setApplicationLocales(appLocale)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsItem(title = stringResource(R.string.settings_version), subtitle = BuildConfig.VERSION_NAME)
                
                Spacer(modifier = Modifier.height(16.dp))

                var showDeleteDialog by remember { mutableStateOf(false) }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text(stringResource(R.string.delete_account_title)) },
                        text = { Text(stringResource(R.string.delete_account_confirmation)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    authViewModel.deleteAccount()
                                    onAccountDeleted()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
                
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_account_button))
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    ) {
        Text(text, maxLines = 1)
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    hasSwitch: Boolean = false,
    isChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            if (hasSwitch && onCheckedChange != null) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFD32F2F),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Black
                    )
                )
            }
        }
    }
}
