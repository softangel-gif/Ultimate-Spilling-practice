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
import com.example.service.GeminiService
import com.example.service.QAEvaluation
import kotlinx.coroutines.launch

data class QAPrompt(
    val title: String,
    val question: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionAnswerScreen() {
    val prompts = remember {
        listOf(
            QAPrompt("Daily Routine", "What is your morning routine before work or study?", "Speaking Practice"),
            QAPrompt("Future Goals", "Where do you see yourself in five years and what steps are you taking to get there?", "Expression"),
            QAPrompt("Favorite Travel", "Describe a memorable vacation or place you visited and why it stood out.", "Narrative"),
            QAPrompt("Opinion on Technology", "How do you think artificial intelligence will impact education in the next decade?", "Argumentative")
        )
    }

    var selectedPromptIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var isEvaluating by remember { mutableStateOf(false) }
    var evaluationResult by remember { mutableStateOf<QAEvaluation?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val currentPrompt = prompts[selectedPromptIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Question & Answer Practice",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Answer writing prompts to improve sentence construction and vocabulary.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Prompt Selector Horizontal Scroll/Chips
        item {
            ScrollableTabRow(selectedTabIndex = selectedPromptIndex, edgePadding = 0.dp) {
                prompts.forEachIndexed { idx, p ->
                    Tab(
                        selected = selectedPromptIndex == idx,
                        onClick = {
                            selectedPromptIndex = idx
                            userAnswer = ""
                            evaluationResult = null
                        },
                        text = { Text(p.title) },
                        modifier = Modifier.testTag("qa_prompt_tab_$idx")
                    )
                }
            }
        }

        // Active Question Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(currentPrompt.category, modifier = Modifier.padding(4.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        currentPrompt.question,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // User Input Box
        item {
            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("Type your response in English") },
                placeholder = { Text("e.g. Every morning I wake up at 7 AM, brew fresh coffee, and...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("input_qa_answer"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    if (userAnswer.isNotBlank()) {
                        isEvaluating = true
                        evaluationResult = null
                        coroutineScope.launch {
                            val eval = GeminiService.evaluateQA(currentPrompt.question, userAnswer)
                            evaluationResult = eval
                            isEvaluating = false
                        }
                    }
                },
                enabled = userAnswer.isNotBlank() && !isEvaluating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_submit_qa"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isEvaluating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("AI Evaluating...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Check & Compare Answer")
                }
            }
        }

        // Evaluation Result Box
        evaluationResult?.let { res ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isCorrectOrGood) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (res.isCorrectOrGood) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Evaluation Feedback", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            res.feedback,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(16.dp))

                        HorizontalDivider()

                        Spacer(Modifier.height(16.dp))

                        Text("Model Response Sample:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            res.modelAnswer,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
