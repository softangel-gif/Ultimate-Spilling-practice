package com.example.data

enum class DiffStatus {
    CORRECT,
    WRONG,
    MISSING,
    EXTRA
}

data class WordDiffToken(
    val expectedWord: String,
    val typedWord: String,
    val status: DiffStatus
)

data class SentenceDiffResult(
    val originalSentence: String,
    val typedSentence: String,
    val tokens: List<WordDiffToken>,
    val accuracyPercentage: Int,
    val isPerfect: Boolean,
    val wrongWords: List<MistakeItem>,
    val missingWords: List<MistakeItem>,
    val extraWords: List<MistakeItem>
)

object DiffEvaluator {

    fun evaluateSentence(
        original: String,
        typed: String,
        userId: String,
        sourceTitle: String
    ): SentenceDiffResult {
        val origWords = sanitizeAndTokenize(original)
        val typedWords = sanitizeAndTokenize(typed)

        val tokens = mutableListOf<WordDiffToken>()
        val wrongMistakes = mutableListOf<MistakeItem>()
        val missingMistakes = mutableListOf<MistakeItem>()
        val extraMistakes = mutableListOf<MistakeItem>()

        // Needleman-Wunsch / Levenshtein alignment for word sequence
        val m = origWords.size
        val n = typedWords.size

        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (origWords[i - 1].equals(typedWords[j - 1], ignoreCase = true)) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // Deletion (Missing in typed)
                    dp[i][j - 1] + 1,       // Insertion (Extra in typed)
                    dp[i - 1][j - 1] + cost // Substitution (Wrong word)
                )
            }
        }

        // Traceback
        var i = m
        var j = n
        val revTokens = mutableListOf<WordDiffToken>()

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && origWords[i - 1].equals(typedWords[j - 1], ignoreCase = true)) {
                revTokens.add(WordDiffToken(origWords[i - 1], typedWords[j - 1], DiffStatus.CORRECT))
                i--
                j--
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                // Substitution
                val exp = origWords[i - 1]
                val typ = typedWords[j - 1]
                revTokens.add(WordDiffToken(exp, typ, DiffStatus.WRONG))
                wrongMistakes.add(
                    MistakeItem(
                        userId = userId,
                        word = typ,
                        expectedWord = exp,
                        fullSentence = original,
                        mistakeType = "Wrong Word",
                        sourceTitle = sourceTitle
                    )
                )
                i--
                j--
            } else if (i > 0 && (j == 0 || dp[i][j] == dp[i - 1][j] + 1)) {
                // Deletion -> Missing
                val exp = origWords[i - 1]
                revTokens.add(WordDiffToken(exp, "", DiffStatus.MISSING))
                missingMistakes.add(
                    MistakeItem(
                        userId = userId,
                        word = "—",
                        expectedWord = exp,
                        fullSentence = original,
                        mistakeType = "Missing Word",
                        sourceTitle = sourceTitle
                    )
                )
                i--
            } else {
                // Insertion -> Extra
                val typ = typedWords[j - 1]
                revTokens.add(WordDiffToken("", typ, DiffStatus.EXTRA))
                extraMistakes.add(
                    MistakeItem(
                        userId = userId,
                        word = typ,
                        expectedWord = "None",
                        fullSentence = original,
                        mistakeType = "Extra Word",
                        sourceTitle = sourceTitle
                    )
                )
                j--
            }
        }

        tokens.addAll(revTokens.reversed())

        val totalExpected = origWords.size.coerceAtLeast(1)
        val correctCount = tokens.count { it.status == DiffStatus.CORRECT }
        val accuracy = ((correctCount.toFloat() / totalExpected.toFloat()) * 100).toInt().coerceIn(0, 100)
        val isPerfect = (correctCount == totalExpected && extraMistakes.isEmpty() && wrongMistakes.isEmpty() && missingMistakes.isEmpty())

        return SentenceDiffResult(
            originalSentence = original,
            typedSentence = typed,
            tokens = tokens,
            accuracyPercentage = accuracy,
            isPerfect = isPerfect,
            wrongWords = wrongMistakes,
            missingWords = missingMistakes,
            extraWords = extraMistakes
        )
    }

    private fun sanitizeAndTokenize(text: String): List<String> {
        return text.trim()
            .replace(Regex("[^a-zA-Z0-9'\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
}
