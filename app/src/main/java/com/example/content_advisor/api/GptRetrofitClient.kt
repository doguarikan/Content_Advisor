package com.example.content_advisor.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GptRetrofitClient {

    private const val BASE_URL = "https://api.openai.com/"

    /**
     * HTTP isteklerini loglamak için interceptor
     * Debug modunda API çağrılarını görmemizi sağlar
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * HTTP client konfigürasyonu
     * Timeout'lar ve interceptor'lar burada ayarlanır
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Bağlantı timeout'u
        .readTimeout(60, TimeUnit.SECONDS)    // Okuma timeout'u
        .writeTimeout(60, TimeUnit.SECONDS)   // Yazma timeout'u
        .build()

    /**
     * Retrofit instance'ı
     * Base URL, HTTP client ve JSON converter'ı konfigüre eder
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()) // JSON <-> Kotlin object dönüşümü
        .build()

    /**
     * GPT API service instance'ı
     * Bu instance ile API çağrıları yapılır
     */
    val gptApiService: GptApiService = retrofit.create(GptApiService::class.java)
}