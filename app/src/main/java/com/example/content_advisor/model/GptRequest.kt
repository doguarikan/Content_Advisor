package com.example.content_advisor.model

import com.google.gson.annotations.SerializedName


data class GptRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000,
    val temperature: Double = 0.7
)

