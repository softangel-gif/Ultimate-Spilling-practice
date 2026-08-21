package com.example.ui.writing

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import com.example.service.GeminiService
import com.example.service.TextToSpeechHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SoundToWriteScreen(
    userId: String,
    ttsHelper: TextToSpeechHelper,
    savedPassages: List<SavedPracticeContent>,
    onSavePassage: (SavedPracticeContent) -> Unit,
    onDeletePassage: (Long) -> Unit,
    onSaveMistakes: (List<MistakeItem>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Practice Session, 1: Library/Saved
    var mode by remember { mutableStateOf("TEXT") } // "TEXT", "GENERATED", or "YOUTUBE"

    var inputContent by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }

    // AI Generation states
    val coroutineScope = rememberCoroutineScope()
    var aiTopic by remember { mutableStateOf("Daily Life & Learning") }
    var generatedText by remember { mutableStateOf("Developing strong English communication skills unlocks exciting possibilities worldwide. Consistent daily practice in listening, typing, and speaking builds confidence step by step.") }
    var generatedTitle by remember { mutableStateOf("Daily Life & Learning Passage") }
    var isGenerating by remember { mutableStateOf(false) }

    // Active session states
    var isPracticing by remember { mutableStateOf(false) }
    var currentTitle by remember { mutableStateOf("Custom Practice") }
    var sentenceList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSentenceIndex by remember { mutableIntStateOf(0) }

    var userTypedInput by remember { mutableStateOf("") }
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }
    var currentDiffResult by remember { mutableStateOf<SentenceDiffResult?>(null) }
    var totalMistakesRecorded by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    // Sample Curated Passages for quick start
    val curatedSamples = remember {
        listOf(
            "The quick brown fox jumps over the lazy dog." to "Basic Alphabet Sentence",
            "Effective communication requires active listening, clear pronunciation, and consistent vocabulary review." to "Communication Practice",
            "Technology changes the way we connect, learn, and collaborate across global communities." to "Modern Technology",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "YouTube English Conversation Sample"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab selector
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Practice Session") },
                icon = { Icon(Icons.Default.Hearing, contentDescription = null) },
                modifier = Modifier.testTag("tab_sound_practice")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Saved Library (${savedPassages.size})") },
                icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                modifier = Modifier.testTag("tab_sound_library")
            )
        }

        if (selectedTab == 0) {
            if (!isPracticing) {
                // Setup Mode Selection & Inputs
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Sound-to-Write Practice",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Paste text or a YouTube video link. The app will read sentences aloud so you can type what you hear.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = mode == "TEXT",
                                        onClick = { mode = "TEXT" },
                                        label = { Text("Custom Text") },
                                        leadingIcon = { Icon(Icons.Default.TextFields, null) },
                                        modifier = Modifier.testTag("chip_mode_text")
                                    )
                                    FilterChip(
                                        selected = mode == "GENERATED",
                                        onClick = { mode = "GENERATED" },
                                        label = { Text("Generated Text") },
                                        leadingIcon = { Icon(Icons.Default.AutoAwesome, null) },
                                        modifier = Modifier.testTag("chip_mode_generated")
                                    )
                                    FilterChip(
                                        selected = mode == "YOUTUBE",
                                        onClick = { mode = "YOUTUBE" },
                                        label = { Text("YouTube Video") },
                                        leadingIcon = { Icon(Icons.Default.PlayCircle, null) },
                                        modifier = Modifier.testTag("chip_mode_youtube")
                                    )
                                }
                            }
                        }
                    }

                    if (mode == "TEXT") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = inputContent,
                                    onValueChange = { inputContent = it },
                                    label = { Text("Enter or paste practice text") },
                                    placeholder = { Text("e.g. English is spoken around the world...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .testTag("input_sound_text"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                AssistChip(
                                    onClick = { mode = "GENERATED" },
                                    label = { Text("Switch to AI Generated Text") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.testTag("btn_quick_generate")
                                )
                            }
                        }
                    } else if (mode == "GENERATED") {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "AI Text Generator",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        "Select a topic or prompt to generate custom practice sentences using Gemini AI.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Quick topic preset chips
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val presets = listOf("Daily Life", "Travel & Vacation", "Job Interview", "Technology", "Business English", "Storytelling")
                                        presets.forEach { preset ->
                                            FilterChip(
                                                selected = aiTopic == preset,
                                                onClick = { aiTopic = preset },
                                                label = { Text(preset, style = MaterialTheme.typography.bodyMedium) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = aiTopic,
                                        onValueChange = { aiTopic = it },
                                        label = { Text("Topic or Custom Prompt") },
                                        placeholder = { Text("e.g. Job Interview, Travel to London, Tech...") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_ai_topic"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isGenerating = true
                                                val result = GeminiService.generatePracticePassage(aiTopic)
                                                generatedTitle = result.title
                                                generatedText = result.text
                                                inputContent = result.text
                                                isGenerating = false
                                            }
                                        },
                                        enabled = !isGenerating,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("button_generate_text"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (isGenerating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Generating Passage...", style = MaterialTheme.typography.labelLarge)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Generate AI Text", style = MaterialTheme.typography.titleMedium)
                                        }
                                    }

                                    if (generatedText.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    generatedTitle,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    generatedText,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = youtubeUrl,
                                    onValueChange = { youtubeUrl = it },
                                    label = { Text("YouTube Video URL") },
                                    placeholder = { Text("https://www.youtube.com/watch?v=...") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Link, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_youtube_url"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Embedded YouTube Player preview
                                val videoId = extractYouTubeId(youtubeUrl)
                                if (videoId.isNotBlank()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        AndroidView(
                                            factory = { context ->
                                                WebView(context).apply {
                                                    webViewClient = WebViewClient()
                                                    settings.javaScriptEnabled = true
                                                    loadUrl("https://www.youtube.com/embed/$videoId")
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val textToProcess = when (mode) {
                                    "TEXT" -> inputContent.ifBlank { "English practice strengthens listening and writing skills together." }
                                    "GENERATED" -> generatedText.ifBlank { "Developing strong English communication skills unlocks exciting possibilities worldwide." }
                                    else -> "Learning English with video listening builds natural speed comprehension. Practice typing sentence by sentence."
                                }

                                val parsedSentences = textToProcess
                                    .split(Regex("(?<=[.!?])\\s+"))
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }

                                sentenceList = parsedSentences.ifEmpty { listOf("Practice typing what you hear clearly.") }
                                currentTitle = when (mode) {
                                    "YOUTUBE" -> "YouTube Video Practice"
                                    "GENERATED" -> generatedTitle.ifBlank { "AI Generated Passage" }
                                    else -> "Text Passage Practice"
                                }
                                currentSentenceIndex = 0
                                userTypedInput = ""
                                currentDiffResult = null
                                isPracticing = true

                                // Save to library
                                onSavePassage(
                                    SavedPracticeContent(
                                        userId = userId,
                                        title = currentTitle,
                                        contentUrlOrText = when (mode) {
                                            "YOUTUBE" -> youtubeUrl
                                            "GENERATED" -> generatedText
                                            else -> inputContent
                                        },
                                        contentType = mode,
                                        sentencesJson = sentenceList.joinToString("|||")
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("button_start_practice"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Practicing", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    item {
                        Text(
                            "Or pick from Curated Sample Passages:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    itemsIndexed(curatedSamples) { index, sample ->
                        OutlinedCard(
                            onClick = {
                                if (sample.first.startsWith("http")) {
                                    mode = "YOUTUBE"
                                    youtubeUrl = sample.first
                                } else {
                                    mode = "TEXT"
                                    inputContent = sample.first
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sample_passage_$index"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (sample.first.startsWith("http")) Icons.Default.PlayCircle else Icons.Default.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(sample.second, fontWeight = FontWeight.Bold)
                                    Text(
                                        sample.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE PRACTICE SCREEN
                val currentSentence = sentenceList.getOrElse(currentSentenceIndex) { "" }

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
                            IconButton(onClick = { isPracticing = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Exit Practice")
                            }

                            Text(
                                "Sentence ${currentSentenceIndex + 1} of ${sentenceList.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "$totalMistakesRecorded Mistakes Saved",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (currentSentenceIndex + 1).toFloat() / sentenceList.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Play Audio Button
                                    Button(
                                        onClick = {
                                            ttsHelper.speechRate = speechSpeed
                                            ttsHelper.speak(currentSentence)
                                        },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("button_play_audio_tts")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Listen Sentence",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            "Tap to Listen",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "Listen closely and type what you hear.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Speed Controls
                                Text("Playback Speed: ${speechSpeed}x", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(0.5f, 0.75f, 1.0f, 1.25f).forEach { speed ->
                                        FilterChip(
                                            selected = speechSpeed == speed,
                                            onClick = { speechSpeed = speed },
                                            label = { Text("${speed}x") },
                                            modifier = Modifier.testTag("speed_chip_$speed")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = userTypedInput,
                            onValueChange = { userTypedInput = it },
                            label = { Text("Type what you heard") },
                            placeholder = { Text("Listen audio and type sentence...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("input_typed_sentence"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val diff = DiffEvaluator.evaluateSentence(
                                        original = currentSentence,
                                        typed = userTypedInput,
                                        userId = userId,
                                        sourceTitle = currentTitle
                                    )
                                    currentDiffResult = diff

                                    val allNewMistakes = diff.wrongWords + diff.missingWords + diff.extraWords
                                    if (allNewMistakes.isNotEmpty()) {
                                        totalMistakesRecorded += allNewMistakes.size
                                        onSaveMistakes(allNewMistakes)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("button_check_answer"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Spellcheck, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Check Answer")
                            }
                        }
                    }

                    // Diff Feedback Display
                    currentDiffResult?.let { diff ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (diff.isPerfect) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (diff.isPerfect) "🎉 Perfect Spelling!" else "Word-by-Word Analysis",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (diff.isPerfect) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                                        )

                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("${diff.accuracyPercentage}% Accuracy", modifier = Modifier.padding(4.dp))
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Render Diff Tokens
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        diff.tokens.forEach { token ->
                                            when (token.status) {
                                                DiffStatus.CORRECT -> {
                                                    Surface(
                                                        color = Color(0xFFDCFCE7),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            token.expectedWord,
                                                            color = Color(0xFF15803D),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                                DiffStatus.WRONG -> {
                                                    Surface(
                                                        color = Color(0xFFFEE2E2),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            "${token.typedWord} (expected: ${token.expectedWord})",
                                                            color = Color(0xFFB91C1C),
                                                            textDecoration = TextDecoration.LineThrough,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                                DiffStatus.MISSING -> {
                                                    Surface(
                                                        color = Color(0xFFFFEDD5),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            "[Missing: ${token.expectedWord}]",
                                                            color = Color(0xFFC2410C),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                                DiffStatus.EXTRA -> {
                                                    Surface(
                                                        color = Color(0xFFF3E8FF),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            "[Extra: ${token.typedWord}]",
                                                            color = Color(0xFF7E22CE),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        "Original: ${diff.originalSentence}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    // Next Button
                                    Button(
                                        onClick = {
                                            if (currentSentenceIndex < sentenceList.size - 1) {
                                                currentSentenceIndex++
                                                userTypedInput = ""
                                                currentDiffResult = null
                                            } else {
                                                isPracticing = false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("button_next_sentence")
                                    ) {
                                        Text(if (currentSentenceIndex < sentenceList.size - 1) "Next Sentence →" else "Complete Practice")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: SAVED LIBRARY
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (savedPassages.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Text("No saved practice videos/passages yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    itemsIndexed(savedPassages) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (item.contentType) {
                                                "YOUTUBE" -> Icons.Default.PlayCircle
                                                "GENERATED" -> Icons.Default.AutoAwesome
                                                else -> Icons.Default.Article
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.title, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        item.contentUrlOrText,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            sentenceList = item.sentencesJson.split("|||")
                                            currentTitle = item.title
                                            currentSentenceIndex = 0
                                            userTypedInput = ""
                                            currentDiffResult = null
                                            isPracticing = true
                                            selectedTab = 0
                                        },
                                        modifier = Modifier.testTag("resume_saved_$index")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume Practice")
                                    }

                                    IconButton(
                                        onClick = { onDeletePassage(item.id) },
                                        modifier = Modifier.testTag("delete_saved_$index")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun extractYouTubeId(url: String): String {
    if (url.isBlank()) return ""
    val pattern = Regex("(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/" +
            "|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\/\\s]{11})")
    val match = pattern.find(url)
    return match?.groupValues?.get(1) ?: ""
}
