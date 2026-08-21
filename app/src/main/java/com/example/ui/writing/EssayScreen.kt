package com.example.ui.writing

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.EssayRecord
import com.example.service.GeminiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssayScreen(
    userId: String,
    savedEssays: List<EssayRecord>,
    onSaveEssayRecord: (EssayRecord) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Write Essay, 1: History Log

    val essayPrompts = remember {
        listOf(
            "Impact of Social Media" to "Write a short essay (80-120 words) discussing how social media influences how people form friendships today.",
            "My Favorite Hobby" to "Describe a hobby or activity you enjoy (60-100 words), explaining why it brings you fulfillment.",
            "Living in Big Cities vs Small Towns" to "Compare the advantages of living in a busy metropolitan city versus a peaceful small town (80-120 words).",
            "Environmental Protection" to "What is one practical action individuals can take to protect the environment in their daily life? (60-100 words)."
        )
    }

    var selectedPromptIdx by remember { mutableIntStateOf(0) }
    var userEssayText by remember { mutableStateOf("") }
    var isEvaluating by remember { mutableStateOf(false) }
    var lastEvaluation by remember { mutableStateOf<EssayRecord?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val targetWordCount = 100

    val currentWordCount = remember(userEssayText) {
        userEssayText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Write Essay") },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.testTag("tab_essay_write")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Essay History (${savedEssays.size})") },
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                modifier = Modifier.testTag("tab_essay_history")
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Prompt Selector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    ScrollableTabRow(selectedTabIndex = selectedPromptIdx, edgePadding = 0.dp) {
                        essayPrompts.forEachIndexed { idx, pair ->
                            Tab(
                                selected = selectedPromptIdx == idx,
                                onClick = { selectedPromptIdx = idx },
                                text = { Text(pair.first) },
                                modifier = Modifier.testTag("essay_prompt_tab_$idx")
                            )
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                essayPrompts[selectedPromptIdx].first,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                essayPrompts[selectedPromptIdx].second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Essay Editor Area
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Essay", fontWeight = FontWeight.Bold)
                            Text(
                                "$currentWordCount / ~$targetWordCount words",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (currentWordCount >= 40) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userEssayText,
                            onValueChange = { userEssayText = it },
                            placeholder = { Text("Start typing your essay response here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("input_essay_text"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (userEssayText.isNotBlank()) {
                                isEvaluating = true
                                coroutineScope.launch {
                                    val promptTitle = essayPrompts[selectedPromptIdx].first
                                    val eval = GeminiService.evaluateEssay(promptTitle, userEssayText, targetWordCount)

                                    val record = EssayRecord(
                                        userId = userId,
                                        promptTitle = promptTitle,
                                        userEssay = userEssayText,
                                        wordCount = currentWordCount,
                                        feedbackGrammar = eval.grammarFeedback,
                                        feedbackSuggestions = eval.suggestions,
                                        modelAnswer = eval.modelAnswer,
                                        score = eval.score
                                    )

                                    lastEvaluation = record
                                    onSaveEssayRecord(record)
                                    isEvaluating = false
                                }
                            }
                        },
                        enabled = userEssayText.isNotBlank() && !isEvaluating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("button_submit_essay"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isEvaluating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(10.dp))
                            Text("Analyzing Grammar & Style...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Submit for AI Feedback")
                        }
                    }
                }

                lastEvaluation?.let { eval ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Essay Assessment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("${eval.score}/100", modifier = Modifier.padding(6.dp), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Text("Grammar & Structure:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    eval.feedbackGrammar,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(Modifier.height(16.dp))

                                Text("Style & Vocabulary Suggestions:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    eval.feedbackSuggestions,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(Modifier.height(16.dp))

                                HorizontalDivider()

                                Spacer(Modifier.height(16.dp))

                                Text("Polished Model Response:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    eval.modelAnswer,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // HISTORY LOG
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (savedEssays.isEmpty()) {
                    item {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            Text("No essays submitted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    itemsIndexed(savedEssays) { idx, essay ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(essay.promptTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("${essay.score} pts", modifier = Modifier.padding(4.dp))
                                    }
                                }

                                Text(
                                    SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(essay.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    essay.userEssay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Feedback: ${essay.feedbackGrammar}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
