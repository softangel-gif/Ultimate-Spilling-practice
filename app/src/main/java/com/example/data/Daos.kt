package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserProfile)

    @Query("UPDATE user_profiles SET isLoggedIn = 0 WHERE isLoggedIn = 1")
    suspend fun logoutAllUsers()
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes WHERE userId = :userId ORDER BY dateTimestamp DESC")
    fun getMistakesForUser(userId: String): Flow<List<MistakeItem>>

    @Query("SELECT * FROM mistakes WHERE userId = :userId AND isMastered = 0 ORDER BY dateTimestamp DESC")
    fun getUnmasteredMistakesForUser(userId: String): Flow<List<MistakeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistakes(mistakes: List<MistakeItem>)

    @Query("UPDATE mistakes SET isMastered = :isMastered WHERE id = :id")
    suspend fun updateMastery(id: Long, isMastered: Boolean)

    @Query("DELETE FROM mistakes WHERE id = :id")
    suspend fun deleteMistake(id: Long)
}

@Dao
interface SavedContentDao {
    @Query("SELECT * FROM saved_content WHERE userId = :userId ORDER BY addedTimestamp DESC")
    fun getSavedContentForUser(userId: String): Flow<List<SavedPracticeContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedContent(content: SavedPracticeContent)

    @Query("DELETE FROM saved_content WHERE id = :id")
    suspend fun deleteSavedContent(id: Long)
}

@Dao
interface EssayDao {
    @Query("SELECT * FROM essay_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getEssaysForUser(userId: String): Flow<List<EssayRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssay(essay: EssayRecord)
}

@Dao
interface ListeningDao {
    @Query("SELECT * FROM listening_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getListeningResultsForUser(userId: String): Flow<List<ListeningResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListeningResult(result: ListeningResult)
}
