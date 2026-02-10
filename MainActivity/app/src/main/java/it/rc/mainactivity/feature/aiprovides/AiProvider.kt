package it.rc.mainactivity.feature.aiprovides


interface AiProvider {
    suspend fun processContent(rawText: String, isSummary: Boolean, translateTo: String = "EN, English"): String
}