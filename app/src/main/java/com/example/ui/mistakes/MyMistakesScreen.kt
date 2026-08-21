package com.example.ui.mistakes

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MistakeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMistakesScreen(
    mistakesList: List<MistakeItem>,
    onDeleteMistake: (Long) -> Unit,
    onStartSpellingQuiz: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val filteredMistakes = remember(mistakesList, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "WRONG" -> mistakesList.filter { it.mistakeType == "Wrong Word" }
            "MISSING" -> mistakesList.filter { it.mistakeType == "Missing Word" }
            "EXTRA" -> mistakesList.filter { it.mistakeType == "Extra Word" }
            else -> mistakesList
        }
    }

    val wrongCount = remember(mistakesList) { mistakesList.count { it.mistakeType == "Wrong Word" } }
    val missingCount = remember(mistakesList) { mistakesList.count { it.mistakeType == "Missing Word" } }
    val extraCount = remember(mistakesList) { mistakesList.count { it.mistakeType == "Extra Word" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("My Mistakes Log", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("${mistakesList.size} Saved Mistakes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (mistakesList.isNotEmpty()) {
                Button(
                    onClick = onStartSpellingQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("button_quiz_from_mistakes")
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Quiz Me")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == "ALL",
                onClick = { selectedCategoryFilter = "ALL" },
                label = { Text("All (${mistakesList.size})") },
                modifier = Modifier.testTag("filter_mistakes_all")
            )
            FilterChip(
                selected = selectedCategoryFilter == "WRONG",
                onClick = { selectedCategoryFilter = "WRONG" },
                label = { Text("Wrong ($wrongCount)") },
                modifier = Modifier.testTag("filter_mistakes_wrong")
            )
            FilterChip(
                selected = selectedCategoryFilter == "MISSING",
                onClick = { selectedCategoryFilter = "MISSING" },
                label = { Text("Missing ($missingCount)") },
                modifier = Modifier.testTag("filter_mistakes_missing")
            )
            FilterChip(
                selected = selectedCategoryFilter == "EXTRA",
                onClick = { selectedCategoryFilter = "EXTRA" },
                label = { Text("Extra ($extraCount)") },
                modifier = Modifier.testTag("filter_mistakes_extra")
            )
        }

        Spacer(Modifier.height(12.dp))

        // Mistakes List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredMistakes.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No mistakes in this category!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Keep up the great spelling practice.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(filteredMistakes) { idx, mistake ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val badgeColor = when (mistake.mistakeType) {
                                        "Wrong Word" -> Color(0xFFDC2626)
                                        "Missing Word" -> Color(0xFFEA580C)
                                        else -> Color(0xFF9333EA)
                                    }

                                    Badge(containerColor = badgeColor) {
                                        Text(mistake.mistakeType, color = Color.White, modifier = Modifier.padding(4.dp))
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        mistake.sourceTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Word: '${mistake.word}' → Expected: '${mistake.expectedWord}'",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    "Sentence: \"${mistake.fullSentence}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onDeleteMistake(mistake.id) },
                                modifier = Modifier.testTag("delete_mistake_$idx")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Mistake", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
