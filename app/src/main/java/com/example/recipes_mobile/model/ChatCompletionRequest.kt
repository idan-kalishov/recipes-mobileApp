package com.example.recipes_mobile.model

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)