package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppDestination
import com.example.ui.MainViewModel
import com.example.ui.components.AppHeader
import com.example.ui.listening.ListeningPracticeScreen
import com.example.ui.mistakes.MyMistakesScreen
import com.example.ui.onboarding.OnboardingFlow
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.EnglishPracticeTheme
import com.example.ui.writing.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EnglishPracticeTheme {
                val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
                val currentUser by viewModel.activeUser.collectAsStateWithLifecycle()
                val mistakesList by viewModel.mistakes.collectAsStateWithLifecycle()
                val savedPassages by viewModel.savedContent.collectAsStateWithLifecycle()
                val essayHistory by viewModel.essays.collectAsStateWithLifecycle()
                val listeningHistory by viewModel.listeningResults.collectAsStateWithLifecycle()

                val screenTitle = when (currentDestination) {
                    AppDestination.ONBOARDING -> "Welcome"
                    AppDestination.SOUND_TO_WRITE -> "Sound-to-Write"
                    AppDestination.SPELLING_QUIZ -> "Spelling Quiz Game"
                    AppDestination.QA_PRACTICE -> "Question & Answer"
                    AppDestination.ESSAYS -> "Small Essay Writing"
                    AppDestination.LISTENING -> "Listening Skills"
                    AppDestination.MISTAKES -> "My Mistakes Log"
                    AppDestination.PROFILE -> "Profile Setup"
                    AppDestination.SETTINGS -> "Settings"
                }

                if (currentDestination == AppDestination.ONBOARDING || currentUser == null) {
                    OnboardingFlow(
                        onCompleteOnboarding = { profile ->
                            viewModel.completeOnboarding(profile)
                        }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            AppHeader(
                                title = screenTitle,
                                currentUser = currentUser,
                                onNavigateToProfile = { viewModel.navigateTo(AppDestination.PROFILE) },
                                onNavigateToWritingSoundToWrite = { viewModel.navigateTo(AppDestination.SOUND_TO_WRITE) },
                                onNavigateToWritingQuiz = { viewModel.navigateTo(AppDestination.SPELLING_QUIZ) },
                                onNavigateToWritingQA = { viewModel.navigateTo(AppDestination.QA_PRACTICE) },
                                onNavigateToWritingEssays = { viewModel.navigateTo(AppDestination.ESSAYS) },
                                onNavigateToListening = { viewModel.navigateTo(AppDestination.LISTENING) },
                                onNavigateToMistakes = { viewModel.navigateTo(AppDestination.MISTAKES) },
                                onNavigateToSettings = { viewModel.navigateTo(AppDestination.SETTINGS) },
                                onLogout = { viewModel.logout() }
                            )
                        },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentDestination,
                                label = "ScreenTransition"
                            ) { destination ->
                                when (destination) {
                                    AppDestination.ONBOARDING -> {
                                        // Handled above
                                    }
                                    AppDestination.SOUND_TO_WRITE -> {
                                        SoundToWriteScreen(
                                            userId = currentUser!!.id,
                                            ttsHelper = viewModel.ttsHelper,
                                            savedPassages = savedPassages,
                                            onSavePassage = { viewModel.savePassage(it) },
                                            onDeletePassage = { viewModel.deletePassage(it) },
                                            onSaveMistakes = { viewModel.saveMistakes(it) }
                                        )
                                    }
                                    AppDestination.SPELLING_QUIZ -> {
                                        SpellingQuizScreen(
                                            mistakesList = mistakesList,
                                            ttsHelper = viewModel.ttsHelper,
                                            onMarkMastered = { id, mastered ->
                                                viewModel.markMistakeMastered(id, mastered)
                                            }
                                        )
                                    }
                                    AppDestination.QA_PRACTICE -> {
                                        QuestionAnswerScreen()
                                    }
                                    AppDestination.ESSAYS -> {
                                        EssayScreen(
                                            userId = currentUser!!.id,
                                            savedEssays = essayHistory,
                                            onSaveEssayRecord = { viewModel.saveEssay(it) }
                                        )
                                    }
                                    AppDestination.LISTENING -> {
                                        ListeningPracticeScreen(
                                            userId = currentUser!!.id,
                                            ttsHelper = viewModel.ttsHelper,
                                            listeningHistory = listeningHistory,
                                            onSaveResult = { viewModel.saveListeningResult(it) }
                                        )
                                    }
                                    AppDestination.MISTAKES -> {
                                        MyMistakesScreen(
                                            mistakesList = mistakesList,
                                            onDeleteMistake = { viewModel.deleteMistake(it) },
                                            onStartSpellingQuiz = { viewModel.navigateTo(AppDestination.SPELLING_QUIZ) }
                                        )
                                    }
                                    AppDestination.PROFILE -> {
                                        ProfileScreen(
                                            currentUser = currentUser!!,
                                            onUpdateProfile = { viewModel.saveUserProfile(it) },
                                            onLogout = { viewModel.logout() }
                                        )
                                    }
                                    AppDestination.SETTINGS -> {
                                        SettingsScreen(
                                            currentSpeechRate = viewModel.ttsHelper.speechRate,
                                            onSpeechRateChange = { viewModel.ttsHelper.speechRate = it }
                                        )
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
