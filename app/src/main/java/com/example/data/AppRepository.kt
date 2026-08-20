package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    val activeUser: Flow<UserProfile?> = db.userDao().getActiveUser()

    suspend fun saveUserProfile(userProfile: UserProfile) {
        db.userDao().insertOrUpdateUser(userProfile)
    }

    suspend fun getUserByEmail(email: String): UserProfile? {
        return db.userDao().getUserByEmail(email)
    }

    suspend fun logoutAllUsers() {
        db.userDao().logoutAllUsers()
    }

    fun getMistakes(userId: String): Flow<List<MistakeItem>> {
        return db.mistakeDao().getMistakesForUser(userId)
    }

    fun getUnmasteredMistakes(userId: String): Flow<List<MistakeItem>> {
        return db.mistakeDao().getUnmasteredMistakesForUser(userId)
    }

    suspend fun addMistakes(mistakes: List<MistakeItem>) {
        db.mistakeDao().insertMistakes(mistakes)
    }

    suspend fun updateMistakeMastery(id: Long, isMastered: Boolean) {
        db.mistakeDao().updateMastery(id, isMastered)
    }

    suspend fun deleteMistake(id: Long) {
        db.mistakeDao().deleteMistake(id)
    }

    fun getSavedContent(userId: String): Flow<List<SavedPracticeContent>> {
        return db.savedContentDao().getSavedContentForUser(userId)
    }

    suspend fun saveContent(content: SavedPracticeContent) {
        db.savedContentDao().insertSavedContent(content)
    }

    suspend fun deleteContent(id: Long) {
        db.savedContentDao().deleteSavedContent(id)
    }

    fun getEssays(userId: String): Flow<List<EssayRecord>> {
        return db.essayDao().getEssaysForUser(userId)
    }

    suspend fun saveEssay(essay: EssayRecord) {
        db.essayDao().insertEssay(essay)
    }

    fun getListeningResults(userId: String): Flow<List<ListeningResult>> {
        return db.listeningDao().getListeningResultsForUser(userId)
    }

    suspend fun saveListeningResult(result: ListeningResult) {
        db.listeningDao().insertListeningResult(result)
    }
}
