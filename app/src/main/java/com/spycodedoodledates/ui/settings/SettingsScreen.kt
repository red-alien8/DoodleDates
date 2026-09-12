package com.spycodedoodledates.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spycodedoodledates.ui.theme.settings.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        viewModel.updateRingtone(uri?.toString())
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Theming") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Beautiful Themes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            ThemePresetGallery(
                onThemeSelected = { viewModel.updateTheme(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Customize Colors", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ColorSetting(
                label = "Background Color",
                color = currentTheme.background,
                onColorSelected = { viewModel.updateTheme(currentTheme.copy(background = it)) }
            )
            
            ColorSetting(
                label = "Paper Color",
                color = currentTheme.paper,
                onColorSelected = { viewModel.updateTheme(currentTheme.copy(paper = it)) }
            )
            
            ColorSetting(
                label = "Accent Color",
                color = currentTheme.accent,
                onColorSelected = { viewModel.updateTheme(currentTheme.copy(accent = it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Notifications & Alarms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("Vibration") },
                supportingContent = { Text("Vibrate when alarm goes off") },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = appSettings.vibrationEnabled,
                        onCheckedChange = { viewModel.updateVibration(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Ringtone") },
                supportingContent = { 
                    val ringtoneName = remember(appSettings.ringtoneUri) {
                        if (appSettings.ringtoneUri.isNullOrEmpty()) "Default"
                        else RingtoneManager.getRingtone(context, Uri.parse(appSettings.ringtoneUri)).getTitle(context)
                    }
                    Text(ringtoneName)
                },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) },
                modifier = Modifier.clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone")
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, appSettings.ringtoneUri?.let { Uri.parse(it) })
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    }
                    ringtoneLauncher.launch(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Data Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val exportLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportBackup(it) }
            }

            val importLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importBackup(it) }
            }

            Button(
                onClick = { exportLauncher.launch("doodledates_backup.json") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Export Backup")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Import Backup")
            }

            Spacer(modifier = Modifier.height(48.dp))

            TextButton(
                onClick = { viewModel.resetToDefault() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Reset to Default Theme")
            }
        }
    }
}

@Composable
fun ThemePresetGallery(onThemeSelected: (CustomTheme) -> Unit) {
    val themes = listOf(
        "Default" to DefaultTheme,
        "Cyberpunk" to CyberpunkTheme,
        "Vintage" to VintageTheme,
        "Forest" to ForestTheme,
        "Minimalist" to MinimalistTheme,
        "Sakura" to SakuraTheme,
        "Deep Sea" to DeepSeaTheme,
        "Sunset" to SunsetTheme,
        "Midnight" to MidnightTheme,
        "Lavender" to LavenderTheme
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(themes) { (name, theme) ->
            ThemeCard(name = name, theme = theme, onClick = { onThemeSelected(theme) })
        }
    }
}

@Composable
fun ThemeCard(name: String, theme: CustomTheme, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(120.dp, 160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.paper)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(theme.accent)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = if (theme.background.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private fun Color.luminance(): Float {
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

private val Color.r get() = red
private val Color.g get() = green
private val Color.b get() = blue

@Composable
fun ColorSetting(label: String, color: Color, onColorSelected: (Color) -> Unit) {
    val colors = listOf(
        Color(0xFF1B2430), Color(0xFFF6F1E7), Color(0xFFC9A227),
        Color.Black, Color.White, Color.Red, Color.Blue, Color.Green, Color.Yellow, 
        Color.Cyan, Color.Magenta, Color.Gray, Color.DarkGray, Color.LightGray,
        Color(0xFFFF8A80), Color(0xFF80CBC4), Color(0xFFB39DDB), Color(0xFFFFF59D)
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(colors) { presetColor ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(presetColor)
                        .border(
                            width = if (color == presetColor) 3.dp else 0.dp,
                            color = if (color == presetColor) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(presetColor) }
                )
            }
        }
    }
}
