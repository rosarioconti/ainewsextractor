package it.rc.mainactivity.feature.aiprovides

interface AiProvider {
    suspend fun processContent(rawText: String, isSummary: Boolean): String
}