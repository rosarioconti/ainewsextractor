package it.rc.mainactivity.feature.aiprovides

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost

class UniversalAiProvider(apiKey: String, val aiType: String) : AiProvider {

    private val openAI = OpenAI(
        OpenAIConfig(
            token = apiKey,
            host =
                when(aiType) {
                    "DeepSeek" -> OpenAIHost("https://api.deepseek.com/v1")
                    "ChatGPT" -> OpenAIHost("https://api.openai.com/v1/")
                    else -> OpenAIHost("https://api.aimlapi.com/v1")
                }
        )
    )

    override suspend fun processContent(rawText: String, isSummary: Boolean, translateTo: String): String {
        val systemPrompt = "You are a web content extractor. Return ONLY valid HTML. No markdown code blocks."
        var userPrompt = if (isSummary) {
            "Summarize this news: extract items and images with max 5 line descriptions. remove all ads and cookies. \n\n rawtext: $rawText"
        } else {
            "Extract all text and images, remove all ads and cookies. \n\n rawtext: $rawText"
        }

        if(translateTo.compareTo(translateTo) != 0){
            userPrompt+= "\n\n and translate to ${translateTo}"
        }

        userPrompt+= "\n\n make sure the image path URL has the full path."

        val request = ChatCompletionRequest(
            model = ModelId(if (aiType == "DeepSeek") "deepseek-chat" else "gpt-4o"),
            messages = listOf(
                ChatMessage(role = ChatRole.System, content = systemPrompt),
                ChatMessage(role = ChatRole.User, content = userPrompt)
            )
        )

        return try {
            val completion = openAI.chatCompletion(request)
            completion.choices.firstOrNull()?.message?.content ?: "Error: No content"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}