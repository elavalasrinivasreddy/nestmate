package com.nestmate.app.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.nestmate.app.core.common.DataResult

class AIAssistantRepository(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun analyzeListingForScams(description: String): DataResult<String> {
        return try {
            val prompt = "Analyze the following listing description for potential scams or red flags. Explain why if suspicious, or state that it looks safe:\n\n$description"
            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (text != null) {
                DataResult.Success(text)
            } else {
                DataResult.Error("No response from AI")
            }
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "AI Analysis failed", e)
        }
    }

    suspend fun parseSearchIntent(query: String): DataResult<String> {
        return try {
            val prompt = """
                Convert the following natural language query into a JSON string representing structured filters for finding a room or roommate. 
                Example query: "cheap room for veg guy"
                Example JSON output: {"price_max": 500, "dietary_preference": "vegetarian", "gender": "male"}
                
                Query: "$query"
                Output only the JSON string.
            """.trimIndent()
            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (text != null) {
                val json = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                DataResult.Success(json)
            } else {
                DataResult.Error("No response from AI")
            }
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "AI Parsing failed", e)
        }
    }

}
