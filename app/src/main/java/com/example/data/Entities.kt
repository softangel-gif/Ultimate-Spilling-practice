package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = "default_user",
    val email: String,
    val nickname: String,
    val avatarIconName: String, // e.g. "avatar_owl", "avatar_fox", "avatar_bear", "avatar_robot", "avatar_rocket", "avatar_star", "avatar_cat", "avatar_dog"
    val selectedSkills: String, // Comma separated: "Writing,Listening"
    val accountType: String, // "Google", "Apple", "Email"
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "mistakes")
data class MistakeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val word: String,
    val expectedWord: String,
    val fullSentence: String,
    val mistakeType: String, // "Wrong Word", "Missing Word", "Extra Word"
    val sourceTitle: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false
)

@Entity(tableName = "saved_content")
data class SavedPracticeContent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val contentUrlOrText: String,
    val contentType: String, // "TEXT" or "YOUTUBE"
    val addedTimestamp: Long = System.currentTimeMillis(),
    val sentencesJson: String // Stored JSON array of sentences
)

@Entity(tableName = "essay_records")
data class EssayRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val promptTitle: String,
    val userEssay: String,
    val wordCount: Int,
    val feedbackGrammar: String,
    val feedbackSuggestions: String,
    val modelAnswer: String,
    val score: Int, // 1-100
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "listening_results")
data class ListeningResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val setTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
