package com.islamic.prayertimes.presentation.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val qiblaDirection by viewModel.qiblaDirection.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val context = LocalContext.current
    var currentAzimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val accelerometerReading = FloatArray(3)
        val magnetometerReading = FloatArray(3)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                    }
                }

                updateOrientation(accelerometerReading, magnetometerReading) { azimuth ->
                    currentAzimuth = azimuth
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            sensorEventListener,
            magnetometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qibla Direction") }
            )
        }
    ) { paddingValues ->
        if (userLocation == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Location not set",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please set your location in Settings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Location info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                                text = "${userLocation!!.cityName}, ${userLocation!!.countryName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (qiblaDirection != null) {
                            Text(
                                text = "Qibla: ${qiblaDirection!!.toInt()}°",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Compass
                if (qiblaDirection != null) {
                    QiblaCompass(
                        qiblaDirection = qiblaDirection!!.toFloat(),
                        currentAzimuth = currentAzimuth
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Instructions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How to use",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "1. Hold your phone flat",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Rotate yourself until the green arrow points to the Kaaba icon",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. You are now facing the Qibla direction",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QiblaCompass(
    qiblaDirection: Float,
    currentAzimuth: Float
) {
    val qiblaAngle = (qiblaDirection - currentAzimuth + 360) % 360

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            // Draw outer circle
            drawCircle(
                color = Color(0xFF006C4C),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw inner circle
            drawCircle(
                color = Color(0xFF006C4C).copy(alpha = 0.1f),
                radius = radius - 20.dp.toPx()
            )

            // Draw cardinal directions
            rotate(-currentAzimuth, pivot = Offset(centerX, centerY)) {
                // N
                drawLine(
                    color = Color.Red,
                    start = Offset(centerX, centerY - radius + 10.dp.toPx()),
                    end = Offset(centerX, centerY - radius + 30.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )

                // E
                drawLine(
                    color = Color.Gray,
                    start = Offset(centerX + radius - 30.dp.toPx(), centerY),
                    end = Offset(centerX + radius - 10.dp.toPx(), centerY),
                    strokeWidth = 2.dp.toPx()
                )

                // S
                drawLine(
                    color = Color.Gray,
                    start = Offset(centerX, centerY + radius - 30.dp.toPx()),
                    end = Offset(centerX, centerY + radius - 10.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // W
                drawLine(
                    color = Color.Gray,
                    start = Offset(centerX - radius + 10.dp.toPx(), centerY),
                    end = Offset(centerX - radius + 30.dp.toPx(), centerY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Draw Qibla arrow
            rotate(qiblaAngle, pivot = Offset(centerX, centerY)) {
                // Arrow line
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - radius + 40.dp.toPx()),
                    strokeWidth = 6.dp.toPx()
                )

                // Arrow head
                val arrowSize = 20.dp.toPx()
                val arrowY = centerY - radius + 40.dp.toPx()

                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(centerX, arrowY),
                    end = Offset(centerX - arrowSize / 2, arrowY + arrowSize),
                    strokeWidth = 6.dp.toPx()
                )

                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(centerX, arrowY),
                    end = Offset(centerX + arrowSize / 2, arrowY + arrowSize),
                    strokeWidth = 6.dp.toPx()
                )
            }

            // Draw center circle
            drawCircle(
                color = Color(0xFF006C4C),
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Direction labels - FIXED: Use Box instead of rotate modifier
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // North
            Text(
                text = "N",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )

            // South
            Text(
                text = "S",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )

            // West
            Text(
                text = "W",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )

            // East
            Text(
                text = "E",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }

        // Qibla angle display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "${qiblaAngle.toInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Kaaba",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFD4AF37)
            )
        }
    }
}

private fun updateOrientation(
    accelerometerReading: FloatArray,
    magnetometerReading: FloatArray,
    onAzimuthChanged: (Float) -> Unit
) {
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)

    if (SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
    ) {
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        val normalizedAzimuth = (azimuth + 360) % 360
        onAzimuthChanged(normalizedAzimuth)
    }
}