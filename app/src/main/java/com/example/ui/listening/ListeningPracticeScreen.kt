package com.example.ui.listening

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.ListeningResult
import com.example.service.TextToSpeechHelper

data class ListeningMCQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ListeningAudioSet(
    val id: String,
    val title: String,
    val level: String, // e.g. "A2 Pre-Intermediate", "B1 Intermediate", "B2 Upper-Intermediate"
    val transcriptDialogue: String,
    val questions: List<ListeningMCQuestion>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningPracticeScreen(
    userId: String,
    ttsHelper: TextToSpeechHelper,
    listeningHistory: List<ListeningResult>,
    onSaveResult: (ListeningResult) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Practice Sets, 1: History

    val setsList = remember {
        listOf(
            ListeningAudioSet(
                id = "set_1",
                title = "Ordering Coffee at a Cafe",
                level = "A2 Elementary",
                transcriptDialogue = """
                    Barista: Good morning! Welcome to Central Perk. What can I get started for you today?
                    Customer: Hi! I would like a medium iced latte with oat milk, please.
                    Barista: Sure thing! Would you like any flavor syrup in that, like vanilla or caramel?
                    Customer: Vanilla, please. Also, do you have fresh blueberry muffins left?
                    Barista: Yes, we just baked a fresh batch ten minutes ago!
                    Customer: Great, I will take one muffin as well.
                    Barista: That comes to eight dollars and fifty cents total.
                """.trimIndent(),
                questions = listOf(
                    ListeningMCQuestion(
                        id = 1,
                        questionText = "What drink did the customer order?",
                        options = listOf("Hot Espresso", "Medium Iced Latte with Oat Milk", "Green Tea", "Cappuccino with Almond Milk"),
                        correctIndex = 1,
                        explanation = "The customer explicitly asks for a medium iced latte with oat milk."
                    ),
                    ListeningMCQuestion(
                        id = 2,
                        questionText = "Which syrup flavor was selected?",
                        options = listOf("Caramel", "Hazelnut", "Vanilla", "No syrup"),
                        correctIndex = 2,
                        explanation = "The customer requested vanilla syrup."
                    ),
                    ListeningMCQuestion(
                        id = 3,
                        questionText = "What baked good did the customer buy?",
                        options = listOf("Croissant", "Blueberry Muffin", "Chocolate Donut", "Bagel"),
                        correctIndex = 1,
                        explanation = "The customer ordered a freshly baked blueberry muffin."
                    )
                )
            ),
            ListeningAudioSet(
                id = "set_2",
                title = "Airport Gate Announcement",
                level = "B1 Intermediate",
                transcriptDialogue = """
                    Announcement: Attention passengers on flight BA 249 to London Heathrow.
                    Due to a minor mechanical check, boarding for gate 14 has been delayed by twenty minutes.
                    We expect to begin priority boarding at 3:45 PM.
                    Please have your passport and boarding pass ready at hand.
                    We apologize for the brief inconvenience and thank you for your patience.
                """.trimIndent(),
                questions = listOf(
                    ListeningMCQuestion(
                        id = 1,
                        questionText = "Where is flight BA 249 flying to?",
                        options = listOf("Paris", "London Heathrow", "New York", "Tokyo"),
                        correctIndex = 1,
                        explanation = "The announcement states flight BA 249 is bound for London Heathrow."
                    ),
                    ListeningMCQuestion(
                        id = 2,
                        questionText = "What is the reason for the flight delay?",
                        options = listOf("Bad weather", "Minor mechanical check", "Crew shortage", "Lost luggage"),
                        correctIndex = 1,
                        explanation = "The delay is caused by a minor mechanical check."
                    ),
                    ListeningMCQuestion(
                        id = 3,
                        questionText = "What time is priority boarding expected to start?",
                        options = listOf("3:15 PM", "3:30 PM", "3:45 PM", "4:00 PM"),
                        correctIndex = 2,
                        explanation = "Priority boarding begins at 3:45 PM."
                    )
                )
            )
        )
    }

    var activeSetIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Practice Sets") },
                icon = { Icon(Icons.Default.Headphones, contentDescription = null) },
                modifier = Modifier.testTag("tab_listening_sets")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("History (${listeningHistory.size})") },
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                modifier = Modifier.testTag("tab_listening_history")
            )
        }

        if (selectedTab == 0) {
            if (activeSetIndex == null) {
                // List of Listening Sets
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "British Council Style Listening Tests",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Listen to realistic conversations and answer multiple-choice comprehension questions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    itemsIndexed(setsList) { idx, set ->
                        Card(
                            onClick = { activeSetIndex = idx },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("listening_set_card_$idx")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                        Text(set.level, modifier = Modifier.padding(4.dp))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(set.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${set.questions.size} Multiple Choice Questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            } else {
                // ACTIVE LISTENING SESSION
                val currentSet = setsList[activeSetIndex!!]
                var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
                var isSubmitted by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                activeSetIndex = null
                                ttsHelper.stop()
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }

                            Text(currentSet.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            IconButton(onClick = { ttsHelper.speak(currentSet.transcriptDialogue) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Audio Player Controller Box
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { ttsHelper.speak(currentSet.transcriptDialogue) },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("button_play_listening_audio"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Recording", modifier = Modifier.size(32.dp))
                                    }

                                    Column {
                                        Text("Play Recording", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Listen to the conversation carefully before answering.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    // Questions
                    itemsIndexed(currentSet.questions) { qIdx, q ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Q${qIdx + 1}. ${q.questionText}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                                Spacer(Modifier.height(12.dp))

                                q.options.forEachIndexed { optIdx, optionText ->
                                    val isSelected = (selectedAnswers[q.id] == optIdx)
                                    val isCorrectOption = (q.correctIndex == optIdx)

                                    val optionColor = when {
                                        !isSubmitted -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        isCorrectOption -> Color(0xFFDCFCE7)
                                        isSelected && !isCorrectOption -> Color(0xFFFEE2E2)
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    Surface(
                                        color = optionColor,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable(enabled = !isSubmitted) {
                                                val newMap = HashMap(selectedAnswers)
                                                newMap[q.id] = optIdx
                                                selectedAnswers = newMap
                                            }
                                            .testTag("question_${q.id}_option_$optIdx")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    if (!isSubmitted) {
                                                        val newMap = HashMap(selectedAnswers)
                                                        newMap[q.id] = optIdx
                                                        selectedAnswers = newMap
                                                    }
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(optionText, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }

                                if (isSubmitted) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Explanation: ${q.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        if (!isSubmitted) {
                            Button(
                                onClick = {
                                    isSubmitted = true
                                    val correctCount = currentSet.questions.count { selectedAnswers[it.id] == it.correctIndex }
                                    onSaveResult(
                                        ListeningResult(
                                            userId = userId,
                                            setTitle = currentSet.title,
                                            score = correctCount,
                                            totalQuestions = currentSet.questions.size
                                        )
                                    )
                                },
                                enabled = selectedAnswers.size == currentSet.questions.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("button_submit_listening"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Submit Listening Answers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            val correctCount = currentSet.questions.count { selectedAnswers[it.id] == it.correctIndex }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Set Complete! Score: $correctCount / ${currentSet.questions.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = { activeSetIndex = null },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Back to Sets List")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // HISTORY TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (listeningHistory.isEmpty()) {
                    item {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            Text("No listening tests completed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    itemsIndexed(listeningHistory) { idx, res ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(res.setTitle, fontWeight = FontWeight.Bold)
                                    Text("Score: ${res.score} / ${res.totalQuestions} correct", style = MaterialTheme.typography.bodySmall)
                                }
                                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                    Text("${((res.score.toFloat() / res.totalQuestions) * 100).toInt()}%", modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
