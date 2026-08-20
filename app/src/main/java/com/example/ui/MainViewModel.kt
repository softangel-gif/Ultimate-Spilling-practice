package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.TextToSpeechHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppDestination {
    ONBOARDING,
    SOUND_TO_WRITE,
    SPELLING_QUIZ,
    QA_PRACTICE,
    ESSAYS,
    LISTENING,
    MISTAKES,
    PROFILE,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AppRepository
    val ttsHelper: TextToSpeechHelper

    private val _currentDestination = MutableStateFlow(AppDestination.ONBOARDING)
    val currentDestination: StateFlow<AppDestination> = _currentDestination

    val activeUser: StateFlow<UserProfile?>

    @OptIn(ExperimentalCoroutinesApi::class)
    val mistakes: StateFlow<List<MistakeItem>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedContent: StateFlow<List<SavedPracticeContent>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val essays: StateFlow<List<EssayRecord>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val listeningResults: StateFlow<List<ListeningResult>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db)
        ttsHelper = TextToSpeechHelper(application)

        activeUser = repository.activeUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        mistakes = activeUser.flatMapLatest { user ->
            if (user != null) repository.getMistakes(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        savedContent = activeUser.flatMapLatest { user ->
            if (user != null) repository.getSavedContent(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        essays = activeUser.flatMapLatest { user ->
            if (user != null) repository.getEssays(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        listeningResults = activeUser.flatMapLatest { user ->
            if (user != null) repository.getListeningResults(user.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Reactively route to dashboard if user is logged in
        viewModelScope.launch {
            activeUser.collect { user ->
                if (user != null && user.isLoggedIn) {
                    if (_currentDestination.value == AppDestination.ONBOARDING) {
                        _currentDestination.value = AppDestination.SOUND_TO_WRITE
                    }
                } else {
                    _currentDestination.value = AppDestination.ONBOARDING
                }
            }
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun completeOnboarding(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            _currentDestination.value = AppDestination.SOUND_TO_WRITE
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun savePassage(content: SavedPracticeContent) {
        viewModelScope.launch {
            repository.saveContent(content)
        }
    }

    fun deletePassage(id: Long) {
        viewModelScope.launch {
            repository.deleteContent(id)
        }
    }

    fun saveMistakes(newMistakes: List<MistakeItem>) {
        viewModelScope.launch {
            repository.addMistakes(newMistakes)
        }
    }

    fun deleteMistake(id: Long) {
        viewModelScope.launch {
            repository.deleteMistake(id)
        }
    }

    fun markMistakeMastered(id: Long, isMastered: Boolean) {
        viewModelScope.launch {
            repository.updateMistakeMastery(id, isMastered)
        }
    }

    fun saveEssay(essay: EssayRecord) {
        viewModelScope.launch {
            repository.saveEssay(essay)
        }
    }

    fun saveListeningResult(result: ListeningResult) {
        viewModelScope.launch {
            repository.saveListeningResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAllUsers()
            ttsHelper.stop()
            _currentDestination.value = AppDestination.ONBOARDING
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
