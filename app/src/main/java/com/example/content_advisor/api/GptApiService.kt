package com.example.content_advisor.api

import com.example.content_advisor.model.GptRequest
import com.example.content_advisor.model.GptResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GptApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GptRequest
    ): Response<GptResponse>
}