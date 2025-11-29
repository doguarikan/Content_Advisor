package com.example.content_advisor.repository

import com.example.content_advisor.api.GptRetrofitClient
import com.example.content_advisor.model.GptRequest
import com.example.content_advisor.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GptRepository {

    private val gptApiService = GptRetrofitClient.gptApiService

    /**
     * Kullanıcı mesajını GPT'ye gönderir ve yanıt alır
     *
     * @param userMessage Kullanıcının gönderdiği mesaj
     * @param conversationHistory Önceki konuşma geçmişi
     * @param apiKey OpenAI API anahtarı
     * @return GPT'den gelen yanıt (başarılı) veya hata
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<Message>,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isEntertainmentRelated(userMessage)) {
                return@withContext Result.success(
                    "Sorry, I can only help you with movie and TV series recommendations. " +
                    "Please ask questions about movies, TV shows, actors, directors, or genres. " +
                    "Examples: 'Can you recommend comedy shows?', 'What shows are similar to Breaking Bad?', " +
                    "'What are Christopher Nolan's best movies?'"
                )
            }
            val systemMessage = Message(
                "system",
                "You are CineMate AI, an assistant specialized only in movie and TV series recommendations. " +
                "Only help with: movie/TV show recommendations, actor/director filmographies, " +
                "genre-based suggestions, similar content recommendations, movie/TV show reviews. " +
                "If asked about other topics, politely explain that you can only help with movie/TV content. " +
                "Be friendly with the user."
            )

            val messages = mutableListOf<Message>()
            messages.add(systemMessage)
            messages.addAll(conversationHistory)
            messages.add(Message("user", userMessage))
            val request = GptRequest(
                model = "gpt-3.5-turbo",
                messages = messages,
                maxTokens = 500,
                temperature = 0.7
            )
            val response = gptApiService.getChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            // Response'u kontrol et
            if (response.isSuccessful) {
                val gptResponse = response.body()
                if (gptResponse != null && gptResponse.choices.isNotEmpty()) {
                    // İlk choice'ın content'ini döndür
                    Result.success(gptResponse.choices[0].message.content)
                } else {
                    Result.failure(Exception("Failed to take answer"))
                }
            } else {
                // HTTP hata kodları
                val errorMessage = when (response.code()) {
                    401 -> "API Key Error"
                    429 -> "Please Wait"
                    500 -> "Server Error"
                    else -> "API Error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Network veya diğer hatalar
            Result.failure(e)
        }
    }

    /**
     * Kullanıcı mesajının dizi/film konusuyla ilgili olup olmadığını kontrol eder
     */
    private fun isEntertainmentRelated(message: String): Boolean {
        val lowerMessage = message.lowercase()
        
        // Dizi/film ile ilgili anahtar kelimeler
        val entertainmentKeywords = listOf(
            "film", "dizi", "movie", "series", "tv", "show", "öneri", "öner", "tavsiye", "tavsiye et",
            "izle", "watch", "gör", "see", "oyuncu", "actor", "actress", "yönetmen", "director",
            "tür", "genre", "komedi", "drama", "aksiyon", "romantik", "korku", "thriller", "sci-fi",
            "bilim kurgu", "fantastik", "belgesel", "animasyon", "netflix", "prime", "disney",
            "hbo", "amazon", "streaming", "platform", "popüler", "trend", "yeni", "eski", "klasik",
            "imdb", "puan", "rating", "yorum", "review", "fragman", "trailer", "poster", "afiş",
            "sezon", "bölüm", "episode", "season", "final", "pilot", "spin-off", "remake", "uyarlama",
            "adaptation", "senaryo", "script", "müzik", "soundtrack", "ost", "çekim", "filming",
            "yapım", "production", "dağıtım", "distribution", "festival", "ödül", "award", "oscar",
            "emmy", "golden globe", "başarılı", "başarısız", "hit", "flop", "box office", "gişe",
            "izlenme", "viewership", "reiting", "audience", "kritik", "critic", "eleştirmen",
            "benzer", "similar", "gibi", "like", "aynı", "same", "farklı", "different", "türk",
            "turkish", "yabancı", "foreign", "hollywood", "bollywood", "kore", "korean", "japon",
            "japanese", "çin", "chinese", "avrupa", "european", "bağımsız", "independent",
            "recommend", "recommendation", "suggest", "suggestion", "best", "top", "popular",
            "favorite", "favourite", "similar to", "like", "comedy", "drama", "action", "romance",
            "horror", "fantasy", "documentary", "animation", "animated", "sitcom", "reality",
            "crime", "mystery", "suspense", "adventure", "western", "musical", "biography",
            "historical", "war", "sports", "family", "children", "teen", "adult", "mature"
        )
        
        // Mesajda bu anahtar kelimelerden herhangi biri var mı kontrol et
        return entertainmentKeywords.any { keyword -> lowerMessage.contains(keyword) }
    }
}