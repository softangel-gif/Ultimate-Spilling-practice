package com.example.ui.writing

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MistakeItem
import com.example.service.TextToSpeechHelper

@Composable
fun SpellingQuizScreen(
    mistakesList: List<MistakeItem>,
    ttsHelper: TextToSpeechHelper,
    onMarkMastered: (Long, Boolean) -> Unit
) {
    // Default fallback quiz words if zero mistakes saved yet
    val activeWords = remember(mistakesList) {
        if (mistakesList.isNotEmpty()) {
            mistakesList.map { it.expectedWord.ifBlank { it.word } to it.fullSentence }
        } else {
            listOf(
                "pronunciation" to "Good pronunciation helps clear understanding.",
                "vocabulary" to "Expanding your vocabulary builds confidence.",
                "grammar" to "Grammar forms the structure of sentences.",
                "listening" to "Active listening requires focus.",
                "comprehension" to "Reading builds strong text comprehension.",
                "fluency" to "Daily speaking practice develops natural fluency."
            )
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userTypedInput by remember { mutableStateOf("") }

    var currentScore by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }

    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    val currentTargetWord = activeWords.getOrElse(currentIndex) { "practice" to "Practice makes perfect." }.first
    val currentSentence = activeWords.getOrElse(currentIndex) { "practice" to "Practice makes perfect." }.second

    LaunchedEffect(currentIndex) {
        // Automatically trigger TTS for word on turn start
        ttsHelper.speak(currentTargetWord)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFEAB308))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Score: $currentScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Streak: $currentStreak",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / activeWords.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Text(
                "Word ${currentIndex + 1} of ${activeWords.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Quiz Central Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Listen to the Secret Word",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Listen Audio Button
                Button(
                    onClick = { ttsHelper.speak(currentTargetWord) },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("button_quiz_audio"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Replay Word Audio",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = { ttsHelper.speak("Context sentence: $currentSentence") },
                    modifier = Modifier.testTag("button_quiz_context")
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hear Context Sentence Hint")
                }

                Spacer(Modifier.height(16.dp))

                // Input Box
                OutlinedTextField(
                    value = userTypedInput,
                    onValueChange = {
                        userTypedInput = it
                        isAnswerSubmitted = false
                    },
                    label = { Text("Type word spelling") },
                    singleLine = true,
                    enabled = !isAnswerSubmitted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quiz_spelling"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isAnswerSubmitted) {
                    Spacer(Modifier.height(16.dp))

                    if (isCorrect) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Correct! 🎉", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    Text("Spelling: $currentTargetWord", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF166534))
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Not quite!", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                    Text("Correct spelling: $currentTargetWord", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!isAnswerSubmitted) {
                Button(
                    onClick = {
                        val cleanTyped = userTypedInput.trim().lowercase()
                        val cleanTarget = currentTargetWord.trim().lowercase()
                        isCorrect = (cleanTyped == cleanTarget)
                        isAnswerSubmitted = true

                        if (isCorrect) {
                            currentScore += 10
                            currentStreak++
                            if (currentStreak > bestStreak) bestStreak = currentStreak

                            // If this was from saved mistakes, mark it mastered
                            mistakesList.find { it.expectedWord.equals(currentTargetWord, ignoreCase = true) }?.let {
                                onMarkMastered(it.id, true)
                            }
                        } else {
                            currentStreak = 0
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_submit_quiz"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Submit Answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (currentIndex < activeWords.size - 1) {
                            currentIndex++
                        } else {
                            currentIndex = 0 // loop or restart
                        }
                        userTypedInput = ""
                        isAnswerSubmitted = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_next_quiz_word"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (currentIndex < activeWords.size - 1) "Next Word →" else "Play Again 🔄",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
