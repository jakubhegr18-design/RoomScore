package com.roomanalyzer.roomscore.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roomanalyzer.roomscore.analysis.IkeaRecommendation
import com.roomanalyzer.roomscore.analysis.RoomAnalysis
import com.roomanalyzer.roomscore.data.HomeAssistantSensorData
import com.roomanalyzer.roomscore.ui.theme.AccentCoral
import com.roomanalyzer.roomscore.ui.theme.AccentGold
import com.roomanalyzer.roomscore.ui.theme.AccentTeal
import com.roomanalyzer.roomscore.ui.theme.DarkCard

@Composable
fun ResultScreen(
    analysis: RoomAnalysis,
    onScanAgain: () -> Unit
) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }

    val animatedScore by animateFloatAsState(
        targetValue = if (animate) analysis.score.toFloat() else 0f,
        animationSpec = tween(1500),
        label = "score"
    )

    val scoreColor = when {
        analysis.score >= 80 -> AccentTeal
        analysis.score >= 60 -> AccentGold
        else -> AccentCoral
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    drawArc(
                        color = DarkCard,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = (animatedScore / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "out of 100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grade: ${analysis.grade}",
                style = MaterialTheme.typography.headlineMedium,
                color = scoreColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Detected: ${analysis.roomType}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionHeader(
                icon = Icons.Default.AutoAwesome,
                title = "Strengths",
                color = AccentTeal
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(analysis.strengths) { strength ->
            TipCard(text = strength, color = AccentTeal)
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                icon = Icons.Default.Lightbulb,
                title = "Ways to Improve",
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(analysis.improvements) { improvement ->
            TipCard(text = improvement, color = AccentGold)
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (analysis.ikeaRecommendations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    icon = Icons.Default.ShoppingCart,
                    title = "IKEA Recommendations",
                    color = AccentTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(analysis.ikeaRecommendations) { rec ->
                IkeaCard(rec)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        analysis.environmentalData?.let { env ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    icon = Icons.Default.Hub,
                    title = "Room Environment",
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                EnvironmentCard(env)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                icon = Icons.Default.Brush,
                title = "Recommended Chores",
                color = AccentCoral
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(analysis.chores) { chore ->
            ChoreCard(text = chore)
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                icon = Icons.Default.Warning,
                title = "Detected Items (${analysis.detectedItems.size})",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (analysis.detectedItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        analysis.detectedItems.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { item ->
                                    Text(
                                        text = item.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            } else {
                Text(
                    text = "No items were confidently detected. Try better lighting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onScanAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Scan Again",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun IkeaCard(rec: IkeaRecommendation) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(rec.productUrl) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rec.productName,
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rec.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rec.price,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\u2022 ${rec.improvementContext}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open IKEA page",
                    tint = AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EnvironmentCard(data: HomeAssistantSensorData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            data.temperature?.let {
                EnvRow(label = "Temperature", value = "${it.toInt()}\u00b0C")
            }
            data.humidity?.let {
                EnvRow(label = "Humidity", value = "${it.toInt()}%")
            }
            data.illuminance?.let {
                EnvRow(label = "Light", value = "${it.toInt()} lux")
            }
            data.co2?.let {
                EnvRow(label = "CO\u2082", value = "${it.toInt()} ppm")
            }
            data.pressure?.let {
                EnvRow(label = "Pressure", value = "${it.toInt()} hPa")
            }
            data.noise?.let {
                EnvRow(label = "Noise", value = "${it.toInt()} dB")
            }
            data.airQuality?.let {
                EnvRow(label = "Air Quality", value = it.replaceFirstChar { c -> c.uppercase() })
            }
            data.isWindowOpen?.let {
                EnvRow(label = "Window", value = if (it) "Open" else "Closed")
            }
            data.hasMotion?.let {
                EnvRow(label = "Motion", value = if (it) "Detected" else "None")
            }
            if (data.temperature == null && data.humidity == null && data.illuminance == null) {
                Text(
                    text = "Connected to Home Assistant but no room sensors found.\nAdd sensors to get environmental insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EnvRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TipCard(text: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(top = 6.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ChoreCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AccentCoral,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
