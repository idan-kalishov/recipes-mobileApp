package com.example.recipes_mobile.model

data class ChatChoice(
    val message: ChatMessage,
    val finish_reason: String,
    val index: Int
)

data class ChatCompletionResponse(
    val id: String,
    val objectType: String,
    val created: Long,
    val choices: List<ChatChoice>,
    val usage: Map<String, Int>
)
