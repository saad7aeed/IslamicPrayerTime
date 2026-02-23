package com.islamic.prayertimes.presentation.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.islamic.prayertimes.presentation.qibla.QiblaScreen
import com.islamic.prayertimes.presentation.settings.SettingsScreen
import com.islamic.prayertimes.presentation.theme.IslamicPrayerTimesTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IslamicPrayerTimesTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
                }

                val permissionsState = rememberMultiplePermissionsState(permissions)

                // Track a refresh trigger that fires on every resume
                var refreshPermissions by remember { mutableIntStateOf(0) }

                // Re-check permissions when returning from system settings
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            refreshPermissions++
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Check actual permission status directly (not relying on Accompanist state)
                val actualPermissionsGranted by remember(refreshPermissions) {
                    derivedStateOf {
                        permissions.filter { it != Manifest.permission.SCHEDULE_EXACT_ALARM }
                            .all { permission ->
                                checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            }
                    }
                }

                LaunchedEffect(Unit) {
                    permissionsState.launchMultiplePermissionRequest()
                }

                Scaffold(
                    bottomBar = {
                        if (currentRoute != null) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "qibla",
                                    onClick = { navController.navigate("qibla") },
                                    icon = { Icon(Icons.Default.Explore, "Qibla") },
                                    label = { Text("Qibla") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = { navController.navigate("settings") },
                                    icon = { Icon(Icons.Default.Settings, "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            MainScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                permissionsGranted = actualPermissionsGranted, // Use this instead
                                onRequestPermissions = {
                                    if (permissionsState.shouldShowRationale) {
                                        // Can still request directly
                                        permissionsState.launchMultiplePermissionRequest()
                                    } else {
                                        // Already denied twice, go to settings
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", packageName, null)
                                        }
                                        startActivity(intent)
                                    }
                                }
                            )
                        }
                        composable("qibla") { QiblaScreen() }
                        composable("settings") {
                            SettingsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}