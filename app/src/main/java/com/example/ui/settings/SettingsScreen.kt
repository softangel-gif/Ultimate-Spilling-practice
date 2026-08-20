package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.service.GeminiService

@Composable
fun SettingsScreen(
    currentSpeechRate: Float,
    onSpeechRateChange: (Float) -> Unit
) {
    var speechRateState by remember { mutableFloatStateOf(currentSpeechRate) }
    val isAiConnected = remember { GeminiService.isApiKeyAvailable() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("App Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        // TTS Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Text-to-Speech (TTS) Default Speed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Adjust standard reading speed for audio practice sentences.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(16.dp))

                    Text("Speed: ${"%.2f".format(speechRateState)}x", fontWeight = FontWeight.Bold)

                    Slider(
                        value = speechRateState,
                        onValueChange = {
                            speechRateState = it
                            onSpeechRateChange(it)
                        },
                        valueRange = 0.5f..1.5f,
                        steps = 3,
                        modifier = Modifier.testTag("slider_tts_speed")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.5x (Slow)", style = MaterialTheme.typography.labelSmall)
                        Text("1.0f (Normal)", style = MaterialTheme.typography.labelSmall)
                        Text("1.5x (Fast)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // AI Engine Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Grading Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAiConnected) Icons.Default.CheckCircle else Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (isAiConnected) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isAiConnected) "Gemini 3.5 Flash Active" else "Rule-based Grammar Evaluator Active",
                            fontWeight = FontWeight.Bold,
                            color = if (isAiConnected) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        if (isAiConnected) "Real-time AI model evaluations enabled for essays and Q&A practice."
                        else "App is operating in offline-friendly mode with instant rule-based evaluations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // About / Storage
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About English Practice App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Version 1.0.0 • Offline-ready Room Persistence • Multi-skill English Practice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
