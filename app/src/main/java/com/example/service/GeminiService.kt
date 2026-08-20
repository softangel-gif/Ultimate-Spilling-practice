package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class EssayEvaluation(
    val score: Int,
    val grammarFeedback: String,
    val suggestions: String,
    val modelAnswer: String
)

data class QAEvaluation(
    val isCorrectOrGood: Boolean,
    val feedback: String,
    val modelAnswer: String
)

data class GeneratedPassage(
    val title: String,
    val text: String
)

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun isApiKeyAvailable(): Boolean {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun evaluateEssay(
        prompt: String,
        userEssay: String,
        targetWordCount: Int
    ): EssayEvaluation = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            return@withContext fallbackEssayEvaluation(userEssay, targetWordCount)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonPrompt = """
            You are an expert English language tutor evaluating a student essay.
            Essay Topic/Prompt: "$prompt"
            Student Essay: "$userEssay"
            Target Word Count: ~$targetWordCount words.

            Respond ONLY with a valid raw JSON object with these keys:
            - "score": Integer from 1 to 100 based on grammar, coherence, and relevance.
            - "grammarFeedback": Clear feedback on grammar, spelling, and sentence structure.
            - "suggestions": 2-3 specific suggestions to improve vocabulary or expression.
            - "modelAnswer": A polished model version of the essay (around $targetWordCount words).
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", jsonPrompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseBody)
                val textResponse = jsonRes.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Extract JSON block
                val cleanJson = textResponse
                    .replace(Regex("```json\\s*"), "")
                    .replace(Regex("```\\s*"), "")
                    .trim()

                val parsed = JSONObject(cleanJson)
                EssayEvaluation(
                    score = parsed.optInt("score", 85),
                    grammarFeedback = parsed.optString("grammarFeedback", "Good grammar overall with strong vocabulary."),
                    suggestions = parsed.optString("suggestions", "Try using more varied sentence connectors and transitional phrases."),
                    modelAnswer = parsed.optString("modelAnswer", "Model response: Writing practice improves fluency over time.")
                )
            } else {
                fallbackEssayEvaluation(userEssay, targetWordCount)
            }
        } catch (e: Exception) {
            fallbackEssayEvaluation(userEssay, targetWordCount)
        }
    }

    suspend fun evaluateQA(
        questionPrompt: String,
        userAnswer: String
    ): QAEvaluation = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            return@withContext fallbackQAEvaluation(questionPrompt, userAnswer)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonPrompt = """
            You are an English language teacher evaluating a student's answer to a practice question.
            Question: "$questionPrompt"
            Student Answer: "$userAnswer"

            Respond ONLY with a valid raw JSON object with keys:
            - "isCorrectOrGood": boolean (true if answer is grammatically sound and relevant).
            - "feedback": constructive explanation of feedback.
            - "modelAnswer": sample ideal response.
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", jsonPrompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseBody)
                val textResponse = jsonRes.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = textResponse
                    .replace(Regex("```json\\s*"), "")
                    .replace(Regex("```\\s*"), "")
                    .trim()

                val parsed = JSONObject(cleanJson)
                QAEvaluation(
                    isCorrectOrGood = parsed.optBoolean("isCorrectOrGood", true),
                    feedback = parsed.optString("feedback", "Your answer is clear and well-structured."),
                    modelAnswer = parsed.optString("modelAnswer", "An ideal answer expresses main points clearly using precise verbs.")
                )
            } else {
                fallbackQAEvaluation(questionPrompt, userAnswer)
            }
        } catch (e: Exception) {
            fallbackQAEvaluation(questionPrompt, userAnswer)
        }
    }

    suspend fun generatePracticePassage(topic: String): GeneratedPassage = withContext(Dispatchers.IO) {
        val cleanTopic = topic.ifBlank { "Daily Routine & Learning" }
        if (!isApiKeyAvailable()) {
            return@withContext fallbackGeneratedPassage(cleanTopic)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonPrompt = """
            You are an English language tutor creating listening & typing practice material.
            Generate an engaging, natural 3 to 5 sentence practice passage on the topic: "$cleanTopic".
            
            Respond ONLY with a valid raw JSON object with keys:
            - "title": A short 3-5 word title for this passage.
            - "text": The complete passage consisting of 3 to 5 clear sentences.
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", jsonPrompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseBody)
                val textResponse = jsonRes.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = textResponse
                    .replace(Regex("```json\\s*"), "")
                    .replace(Regex("```\\s*"), "")
                    .trim()

                val parsed = JSONObject(cleanJson)
                GeneratedPassage(
                    title = parsed.optString("title", "$cleanTopic Passage"),
                    text = parsed.optString("text", "Practicing English daily enhances your vocabulary and listening comprehension. Consistent typing exercises help reinforce proper grammar and natural sentence flow.")
                )
            } else {
                fallbackGeneratedPassage(cleanTopic)
            }
        } catch (e: Exception) {
            fallbackGeneratedPassage(cleanTopic)
        }
    }

    private fun fallbackGeneratedPassage(topic: String): GeneratedPassage {
        val passageText = when {
            topic.contains("travel", ignoreCase = true) ->
                "Traveling opens up opportunities to discover unique cultures and try delicious cuisine. Navigating new cities requires effective communication and confidence. Each journey leaves you with memorable experiences and broader perspectives."
            topic.contains("job", ignoreCase = true) || topic.contains("work", ignoreCase = true) || topic.contains("career", ignoreCase = true) ->
                "Preparing for a career milestone demands clear communication and structured thinking. Team collaboration flourishes when ideas are expressed concisely. Developing professional vocabulary opens doors to exciting new opportunities."
            topic.contains("tech", ignoreCase = true) || topic.contains("ai", ignoreCase = true) || topic.contains("future", ignoreCase = true) ->
                "Artificial intelligence and digital tools are transforming modern learning environments. Adaptive technology helps students practice skills at their own pace. Staying curious allows us to embrace innovative ways of problem solving."
            else ->
                "Developing strong English communication skills unlocks exciting possibilities worldwide. Consistent daily practice in listening, typing, and speaking builds confidence step by step. Every new phrase you master brings you closer to your language goals."
        }
        return GeneratedPassage(
            title = "$topic Practice",
            text = passageText
        )
    }

    private fun fallbackEssayEvaluation(userEssay: String, targetWordCount: Int): EssayEvaluation {
        val wordCount = userEssay.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val hasCapitalStart = userEssay.firstOrNull()?.isUpperCase() == true
        val hasPeriodEnd = userEssay.endsWith(".") || userEssay.endsWith("!") || userEssay.endsWith("?")

        var score = 75
        if (wordCount >= targetWordCount * 0.8) score += 10
        if (hasCapitalStart) score += 5
        if (hasPeriodEnd) score += 10

        val grammarFeedback = buildString {
            append("Word count: $wordCount words.")
            if (!hasCapitalStart) append(" Remember to capitalize the first letter of sentences.")
            if (!hasPeriodEnd) append(" End sentences with proper punctuation.")
            if (wordCount < targetWordCount * 0.5) append(" Try expanding your thoughts with more details.")
            else append(" Well-structured sentence lengths and good flow.")
        }

        return EssayEvaluation(
            score = score.coerceIn(1..100),
            grammarFeedback = grammarFeedback,
            suggestions = "1. Use connectors like 'furthermore', 'however', and 'in addition'.\n2. Vary your sentence openers for dynamic rhythm.",
            modelAnswer = "Sample Model Response: Practicing written expression daily builds vocabulary, strengthens grammatical confidence, and enables clear communication in professional and social settings."
        )
    }

    private fun fallbackQAEvaluation(questionPrompt: String, userAnswer: String): QAEvaluation {
        val words = userAnswer.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val isSubstantial = words.size >= 3
        return QAEvaluation(
            isCorrectOrGood = isSubstantial,
            feedback = if (isSubstantial) "Your answer addresses the question directly with good word choice." else "Your response is very short. Try responding in a complete sentence.",
            modelAnswer = "Sample Answer: I believe regular language practice with listening and writing builds confidence quickly."
        )
    }
}
