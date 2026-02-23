package com.islamic.prayertimes.presentation.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.islamic.prayertimes.data.models.PrayerTime
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Auto-fetch location when permissions are granted
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted && settings?.location == null) {
            viewModel.fetchLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer Times") },
                actions = {
                    IconButton(onClick = { viewModel.loadPrayerTimes() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MainUiState.LocationNotSet -> {
                    LocationNotSetContent(
                        permissionsGranted = permissionsGranted,
                        onRequestLocation = { viewModel.fetchLocation() },
                        onRequestPermissions = onRequestPermissions,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
                is MainUiState.Success -> {
                    PrayerTimesContent(
                        prayerTimes = state.prayerTimes.getAllPrayers(),
                        nextPrayer = state.nextPrayer,
                        timeRemaining = state.timeRemaining,
                        location = settings?.location?.cityName ?: "Unknown"
                    )
                }
                is MainUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadPrayerTimes() },
                        onFetchLocation = { viewModel.fetchLocation() }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationNotSetContent(
    permissionsGranted: Boolean,
    onRequestLocation: () -> Unit,
    onRequestPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Location Not Set",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (permissionsGranted) {
                "Please enable location to get accurate prayer times for your area"
            } else {
                "Location permission is required to get accurate prayer times"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (permissionsGranted) {
            Button(
                onClick = onRequestLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MyLocation, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get My Location")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Location Manually")
            }
        } else {
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Permissions")
            }
        }
    }
}

@Composable
fun PrayerTimesContent(
    prayerTimes: List<PrayerTime>,
    nextPrayer: PrayerTime?,
    timeRemaining: String,
    location: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with location and date
        item {
            HeaderCard(location = location)
        }

        // Next Prayer Card
        if (nextPrayer != null) {
            item {
                NextPrayerCard(
                    prayerName = nextPrayer.name,
                    timeRemaining = timeRemaining
                )
            }
        }

        // All Prayer Times
        items(prayerTimes) { prayer ->
            PrayerTimeCard(
                prayer = prayer,
                isNext = prayer == nextPrayer
            )
        }
    }
}

@Composable
fun HeaderCard(location: String) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dateFormat.format(Date()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun NextPrayerCard(
    prayerName: String,
    timeRemaining: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Prayer",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prayerName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = timeRemaining,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PrayerTimeCard(
    prayer: PrayerTime,
    isNext: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isNext) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getPrayerIcon(prayer.name),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isNext) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = prayer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                    )

                    if (!prayer.isEnabled) {
                        Text(
                            text = "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = prayer.timeString,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                color = if (isNext) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onFetchLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFetchLocation,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MyLocation, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

fun getPrayerIcon(prayerName: String) = when (prayerName) {
    "Fajr" -> Icons.Default.WbTwilight
    "Dhuhr" -> Icons.Default.WbSunny
    "Asr" -> Icons.Default.Cloud
    "Maghrib" -> Icons.Default.NightsStay
    "Isha" -> Icons.Default.DarkMode
    else -> Icons.Default.AccessTime
}