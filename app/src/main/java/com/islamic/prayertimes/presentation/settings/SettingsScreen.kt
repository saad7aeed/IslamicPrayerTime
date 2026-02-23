package com.islamic.prayertimes.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.islamic.prayertimes.data.models.AzanSound
import com.islamic.prayertimes.data.models.PrayerCalculationMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    var showCalculationMethodDialog by remember { mutableStateOf(false) }
    var showAzanSoundDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Location Section
            item {
                SectionHeader("Location")
            }

            item {
                SettingsItem(
                    title = "Current Location",
                    subtitle = settings?.location?.let {
                        "${it.cityName}, ${it.countryName}"
                    } ?: "Not set",
                    icon = Icons.Default.LocationOn,
                    onClick = { /* Navigate to location picker */ }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Calculation Method Section
            item {
                SectionHeader("Prayer Time Calculation")
            }

            item {
                SettingsItem(
                    title = "Calculation Method",
                    subtitle = settings?.calculationMethod?.displayName ?: "Muslim World League",
                    icon = Icons.Default.Calculate,
                    onClick = { showCalculationMethodDialog = true }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Azan Settings Section
            item {
                SectionHeader("Azan Settings")
            }

            item {
                SettingsItem(
                    title = "Azan Sound",
                    subtitle = settings?.azanSound?.name ?: "Makkah",
                    icon = Icons.Default.VolumeUp,
                    onClick = { showAzanSoundDialog = true }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Respect Silent Mode",
                    subtitle = "Don't play Azan when phone is on silent",
                    checked = settings?.respectSilentMode ?: true,
                    onCheckedChange = { viewModel.updateRespectSilentMode(it) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Prayer Notifications Section
            item {
                SectionHeader("Prayer Notifications")
            }

            item {
                SwitchSettingsItem(
                    title = "Fajr",
                    checked = settings?.fajrEnabled ?: true,
                    onCheckedChange = { viewModel.updatePrayerEnabled("Fajr", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Dhuhr",
                    checked = settings?.dhuhrEnabled ?: true,
                    onCheckedChange = { viewModel.updatePrayerEnabled("Dhuhr", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Asr",
                    checked = settings?.asrEnabled ?: true,
                    onCheckedChange = { viewModel.updatePrayerEnabled("Asr", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Maghrib",
                    checked = settings?.maghribEnabled ?: true,
                    onCheckedChange = { viewModel.updatePrayerEnabled("Maghrib", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Isha",
                    checked = settings?.ishaEnabled ?: true,
                    onCheckedChange = { viewModel.updatePrayerEnabled("Isha", it) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Reminders Section
            item {
                SectionHeader("Prayer Reminders")
            }

            item {
                SettingsItem(
                    title = "Reminder Time",
                    subtitle = "${settings?.reminderMinutes ?: 10} minutes before",
                    icon = Icons.Default.Alarm,
                    onClick = { showReminderDialog = true }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Fajr Reminder",
                    checked = settings?.fajrReminderEnabled ?: false,
                    onCheckedChange = { viewModel.updateReminderEnabled("Fajr", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Dhuhr Reminder",
                    checked = settings?.dhuhrReminderEnabled ?: false,
                    onCheckedChange = { viewModel.updateReminderEnabled("Dhuhr", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Asr Reminder",
                    checked = settings?.asrReminderEnabled ?: false,
                    onCheckedChange = { viewModel.updateReminderEnabled("Asr", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Maghrib Reminder",
                    checked = settings?.maghribReminderEnabled ?: false,
                    onCheckedChange = { viewModel.updateReminderEnabled("Maghrib", it) }
                )
            }

            item {
                SwitchSettingsItem(
                    title = "Isha Reminder",
                    checked = settings?.ishaReminderEnabled ?: false,
                    onCheckedChange = { viewModel.updateReminderEnabled("Isha", it) }
                )
            }
        }
    }

    // Dialogs
    if (showCalculationMethodDialog) {
        CalculationMethodDialog(
            currentMethod = settings?.calculationMethod ?: PrayerCalculationMethod.MWL,
            onDismiss = { showCalculationMethodDialog = false },
            onSelect = { method ->
                viewModel.updateCalculationMethod(method)
                showCalculationMethodDialog = false
            }
        )
    }

    if (showAzanSoundDialog) {
        AzanSoundDialog(
            currentSound = settings?.azanSound ?: AzanSound.MAKKAH,
            onDismiss = { showAzanSoundDialog = false },
            onSelect = { sound ->
                viewModel.updateAzanSound(sound)
                showAzanSoundDialog = false
            }
        )
    }

    if (showReminderDialog) {
        ReminderTimeDialog(
            currentMinutes = settings?.reminderMinutes ?: 10,
            onDismiss = { showReminderDialog = false },
            onSelect = { minutes ->
                viewModel.updateReminderMinutes(minutes)
                showReminderDialog = false
            }
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun CalculationMethodDialog(
    currentMethod: PrayerCalculationMethod,
    onDismiss: () -> Unit,
    onSelect: (PrayerCalculationMethod) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calculation Method") },
        text = {
            LazyColumn {
                items(PrayerCalculationMethod.entries.size) { index ->
                    val method = PrayerCalculationMethod.entries[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(method) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = method == currentMethod,
                            onClick = { onSelect(method) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(method.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AzanSoundDialog(
    currentSound: AzanSound,
    onDismiss: () -> Unit,
    onSelect: (AzanSound) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Azan Sound") },
        text = {
            Column {
                AzanSound.entries.forEach { sound ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(sound) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sound == currentSound,
                            onClick = { onSelect(sound) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sound.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReminderTimeDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(5, 10, 15, 20, 30)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder Time") },
        text = {
            Column {
                options.forEach { minutes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(minutes) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = minutes == currentMinutes,
                            onClick = { onSelect(minutes) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$minutes minutes before")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}